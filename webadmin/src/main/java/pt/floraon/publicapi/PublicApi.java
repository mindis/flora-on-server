package pt.floraon.publicapi;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.OccurrenceProcessor;
import pt.floraon.redlistdata.RedListAdminPages;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 15-07-2017.
 */
@WebServlet("/api/*")
public class PublicApi extends FloraOnServlet {
    static Pattern svgURL = Pattern.compile("^[a-zçA-Z]+_[a-zç-]+_(?<id>[0-9]+).svg$");
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        PrintWriter wr;
        ListIterator<String> path;
        try {
            path = thisRequest.getPathIteratorAfter("api");
        } catch (FloraOnException e) {
            thisRequest.error("Missing parameters.");
            return;
        }

        User user = thisRequest.getUser();

        switch(path.next()) {
            case "svgmap":
                INodeKey key;
                Integer squareSize;
                Integer borderWidth;
                boolean viewAll;
                Matcher m;

                if(path.hasNext() && (m = svgURL.matcher(path.next())).find()) {
                    key = driver.asNodeKey("taxent/" + m.group("id"));
                    squareSize = 10000;
                    borderWidth = 2;
                    viewAll = false;
                } else {
                    key = thisRequest.getParameterAsKey("taxon");
                    squareSize = thisRequest.getParameterAsInteger("size", 10000);
                    borderWidth = thisRequest.getParameterAsInteger("border", 2);
                    viewAll = "all".equals(thisRequest.getParameterAsString("view"));
                }


                if(squareSize < 10000 && !user.canVIEW_OCCURRENCES()) {
                    thisRequest.response.sendError(HttpServletResponse.SC_FORBIDDEN, "No public access for this precision.");
                    return;
                }

                if(key == null) return;
                TaxEnt te2 = driver.getNodeWorkerDriver().getDocument(key, TaxEnt.class);
                if(te2 == null) return;
                List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                for(SimpleOccurrenceDataProvider edp : sodps)
                    edp.executeOccurrenceQuery(te2);

                thisRequest.response.setContentType("image/svg+xml; charset=utf-8");
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.setCacheHeaders(60 * 10);
//                thisRequest.response.addHeader("Access-Control-Allow-Origin", "*");

                PolygonTheme protectedAreas = null;

                if(thisRequest.getParameterAsBoolean("pa", false))
                    protectedAreas = new PolygonTheme(RedListAdminPages.class.getResourceAsStream("SNAC.geojson"), "SITE_NAME");

                wr = thisRequest.response.getWriter();
                PolygonTheme cP = new PolygonTheme(pt.floraon.redlistdata.OccurrenceProcessor.class.getResourceAsStream("PT_buffer.geojson"), null);
                OccurrenceProcessor op1 = new OccurrenceProcessor(
                        sodps, protectedAreas, squareSize
                        , cP, viewAll ? null : 1991, null, viewAll);
                op1.exportSVG(new PrintWriter(wr), true, false
                        , thisRequest.getParameterAsBoolean("basemap", false)
                        , true
                        , borderWidth
                        , thisRequest.getParameterAsBoolean("shadow", true));
                wr.flush();
                break;
        }
    }
}
