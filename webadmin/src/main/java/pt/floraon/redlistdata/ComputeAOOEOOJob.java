package pt.floraon.redlistdata;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.jobs.JobFileDownload;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * For a given red list dataset, download a table with the taxa, AOO, EOO, etc.
 * Created by miguel on 13-03-2017.
 */
public class ComputeAOOEOOJob implements JobFileDownload {
    private String territory;
    private PolygonTheme clippingPolygon;
    private Integer minimumYear, sizeOfSquare;
    private int curSpeciesI = 0, total;
    private String curSpeciesName = "";
    private Set<String> filterTags;

    ComputeAOOEOOJob(String territory, PolygonTheme clippingPolygon, Integer minimumYear, Integer sizeOfSquare, Set<String> filterTags) {
        this.territory = territory;
        this.clippingPolygon = clippingPolygon;
        this.minimumYear = minimumYear;
        this.sizeOfSquare = sizeOfSquare;
        this.filterTags = filterTags;
    }

    @Override
    public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException {
        OccurrenceProcessor op;
        List<RedListDataEntity> it = driver.getRedListData().getAllRedListData(territory, false);
        total = it.size();
        CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(out), CSVFormat.TDF);

        csvp.print("TaxEnt ID");
        csvp.print("Taxon");
        csvp.print("AOO (km2)");
        csvp.print("EOO (km2)");
        csvp.print("Real EOO (km2)");
        csvp.print("Number of sites");
        csvp.println();

        for(RedListDataEntity rlde : it) {
            curSpeciesI++;
            curSpeciesName = rlde.getTaxEnt().getName();
            if(filterTags != null && Collections.disjoint(filterTags, Arrays.asList(rlde.getTags()))) continue;

            for (ExternalDataProvider edp : driver.getRedListData().getExternalDataProviders()) {
                edp.executeOccurrenceQuery(rlde.getTaxEnt().getOldId());
            }

            op = new OccurrenceProcessor(driver.getRedListData().getExternalDataProviders(), null
                    , sizeOfSquare, clippingPolygon, minimumYear, null);

            csvp.print(rlde.getTaxEnt().getID());
            csvp.print(rlde.getTaxEnt().getName());
            csvp.print(op.getAOO());
            csvp.print(op.getEOO());
            csvp.print(op.getRealEOO());
            csvp.print(op.getNLocations());
            csvp.println();

        }
        csvp.close();
    }

    @Override
    public String getState() {
        return "Processing species " + curSpeciesI + " of " + total + " (" + curSpeciesName + ")";
    }
}
