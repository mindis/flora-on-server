package pt.floraon.authentication;

import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by miguel on 16-04-2017.
 */
@WebServlet("/adminpage/*")
public class AdminPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        String what;


        thisRequest.request.setAttribute("what", what = thisRequest.getParameterAsString("w", "main"));

        if(thisRequest.getUser().isAdministrator()) {
            // fetch unmatched occurrences and try to match interactively
            InventoryList il = driver.getOccurrenceDriver().matchTaxEntNames(
                    driver.getOccurrenceDriver().getUnmatchedOccurrencesOfMaintainer(null)
                    , false, true);

            thisRequest.request.setAttribute("nomatchquestions", il.getQuestions());
            thisRequest.request.setAttribute("matchwarnings", il.getVerboseWarnings());

/*
        Iterator<Inventory> umo = driver.getOccurrenceDriver().getUnmatchedOccurrences();
        Set<String> unmNames = new HashSet<>();
        int count = 0;
        while(umo.hasNext()) {
            unmNames.add(umo.next().getUnmatchedOccurrences().get(0).getVerbTaxon());
            count++;
        }
        thisRequest.request.setAttribute("unmatchedNames", unmNames);
        thisRequest.request.setAttribute("unmatchedNumber", count);
*/

//        driver.getOccurrenceDriver().getUnmatchedOccurrences().next().getUnmatchedOccurrences().get(0).getVerbTaxon()

/*
        switch (what) {

        }
*/
        } else {
            for(TaxonPrivileges tp : thisRequest.getUser().getTaxonPrivileges()) {
                if(tp.getPrivileges().contains(Privileges.DOWNLOAD_OCCURRENCES)) {
                    thisRequest.request.setAttribute("showDownload", true);
                }
            }
        }

        thisRequest.request.getRequestDispatcher("/main-admin.jsp").forward(thisRequest.request, thisRequest.response);
    }
}