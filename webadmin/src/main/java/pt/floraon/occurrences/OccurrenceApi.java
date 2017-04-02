package pt.floraon.occurrences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jline.internal.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 23-03-2017.
 */
@MultipartConfig
@WebServlet("/occurrences/api/*")
public class OccurrenceApi extends FloraOnServlet {
    @Override
    public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = getPathIteratorAfter("api");
        Gson gs = new GsonBuilder().setPrettyPrinting().create();
        String fileName;
        User user = getUser();
        if(user.isGuest()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        switch (path.next()) {
            case "savetable":
                fileName = getParameterAsString("file");
                errorIfAnyNull(fileName);

                boolean main = getParameterAsBoolean("mainobserver", false);
                user = refreshUser();
                if(!user.getUploadedTables().contains(fileName)) throw new FloraOnException("File not found.");

                Log.info("Reading " + fileName);
                InventoryList invList;
                try {
                    invList = Common.readInventoryListFromFile(fileName);
                } catch (ClassNotFoundException e) {
                    throw new FloraOnException(e.getMessage());
                }

                for(Inventory inv : invList) {
                    if(main) {
                        if(StringUtils.isArrayEmpty(inv.getObservers()))
                            inv.setObservers(new String[] {getUser().getID()});
                    }

                    driver.getOccurrenceDriver().createInventory(inv);
                }

                driver.getOccurrenceDriver().discardUploadedTable(driver.asNodeKey(getUser().getID()), fileName);
                success("Ok");

                break;

            case "discardtable":
                fileName = getParameterAsString("file");
                errorIfAnyNull(fileName);
                success(driver.getOccurrenceDriver().discardUploadedTable(driver.asNodeKey(getUser().getID()), fileName) ? "Ok" : "Nothing deleted.");
                break;

            case "addoccurrences":
                // NOTE: this expects HTML field names of the form xxxxxx_yyyyyy_latitude, xxxxxx_yyyyyy_taxa, etc. (6 uid digits for grouping, other 6 ofr table rows)
                Pattern ids = Pattern.compile("^(?<id1>[a-zA-Z0-9]{6})_((?<id2>[a-zA-Z0-9]{6})_)?(?<name>[a-zA-Z0-9]+)$");
                Enumeration<String> en = request.getParameterNames();
                Multimap<String, String> grp = ArrayListMultimap.create();
                Multimap<String, String> map = ArrayListMultimap.create();
                Multimap<String, String> invfields = ArrayListMultimap.create();
                while(en.hasMoreElements()) {
                    String name = en.nextElement();
                    Matcher mat = ids.matcher(name);
                    if(!mat.find()) continue;
                    String id1 = mat.group("id1");
                    String id2 = mat.group("id2");
                    if(id2 != null) {
                        grp.put(id1, id2);
                        map.put(id2, name);
                    } else
                        invfields.put(id1, name);

                }

                InventoryList inventories = new InventoryList();
                Inventory inv;
                OccurrenceParser op = new OccurrenceParser(driver);
                for(String invid : invfields.keySet()) {    // each inventory
                    inventories.add(inv = new Inventory());
                    Map<String, String> keyValues = new HashMap<>();

                    for(String name : invfields.get(invid)) {   // set the inventory fields
                        String field = name.substring(7);
                        keyValues.put(field, getParameterAsString(name));
                    }
                    op.parseFields(keyValues, inv);     // feed in inventory fields

                    for(String id2 : new HashSet<>(grp.get(invid))) {    // go through all occurrence fields this inventory
                        System.out.println("ID2: " + id2);
                        Inventory tmp = new Inventory();
                        keyValues = new HashMap<>();
                        for(String name : map.get(id2)) {   // this is one occurrence (one table row)
                            String field = name.substring(14);
                            System.out.println(name + ": "+getParameterAsString(name));
                            keyValues.put(field, getParameterAsString(name));
                        }
                        op.parseFields(keyValues, tmp);
                        inv.getUnmatchedOccurrences().addAll(tmp.getUnmatchedOccurrences());
                    }
                    if(inv.getUnmatchedOccurrences().size() == 0)
                        inv.getUnmatchedOccurrences().add(new newOBSERVED_IN());
                    System.out.println(gs.toJson(inv));
                }

/*

                for(String id1 : grp.keySet()) {    // each inventory
                    inventories.add(inv = new Inventory());
                    for(String id2 : grp.get(id1)) {    // each occurrence
                        Map<String, String> keyValues = new HashMap<>();
                        for(String name : map.get(id2)) {
                            String field = name.substring(14);
                            keyValues.put(field, getParameterAsString(name));
                        }
                        op.parseFields(keyValues, inv);
                        System.out.println(gs.toJson(inv));
                    }
                }
*/

/*

                for(Map.Entry<String, Collection<String>> grpent : grp.asMap().entrySet()) {
                    for(String id2 : grpent.getValue()) {
//                    for (Map.Entry<String, Collection<String>> ent : map.get(grpent.getValue())) {
                        // new table line means new inventory
                        inventories.add(inv = new Inventory());
                        int i = 0;
                        boolean finished = false;
                        while (true) {
                            Map<String, String> keyValues = new HashMap<>();
                            for (String name : ent.getValue()) {
                                String field = name.substring(7);
                                if (i >= request.getParameterValues(name).length) {
                                    finished = true;
                                    break;
                                }
                                keyValues.put(field, request.getParameterValues(name)[i]);
                            }
                            op.parseFields(keyValues, inv);
                            if (finished) break;
                            i++;
                        }
                    }
                }
*/

                driver.getOccurrenceDriver().matchTaxEntNames(inventories);
                System.out.println(gs.toJson(inventories));

                int count = 0;
                for(Inventory inv1 : inventories) {
                    if(StringUtils.isArrayEmpty(inv1.getObservers()))
                        inv1.setObservers(new String[] {getUser().getID()});

                    driver.getOccurrenceDriver().createInventory(inv1);
                    count++;
                }
                success(count + " inventories saved.");
                break;

            case "deleteoccurrences":
                driver.getOccurrenceDriver().deleteOccurrences(request.getParameterValues("inventoryId")
                        , request.getParameterValues("occurrenceUuid"));
                success("Ok");
                break;

            case "mergeoccurrences":
                // merges occurrences into one inventory. this discards the coordinates of the merged inventory and assigns coordinates
                // to each observation separately
                Iterator<Inventory> it = driver.getOccurrenceDriver().getOccurrencesByUuid(driver.asNodeKey(getUser().getID())
                        , request.getParameterValues("occurrenceUuid"));

                List<Inventory> tmp = Lists.newArrayList(it);
                if(tmp.size() == 0) {
                    success("Ok");
                    break;
                }

                for(Inventory tmp1 : tmp) {
                    for(newOBSERVED_IN occ : tmp1._getOccurrences()) {
                        if(occ.getLatitude() == null)
                            occ.setLatitude(tmp1.getLatitude());
                        if(occ.getLongitude() == null)
                            occ.setLongitude(tmp1.getLongitude());
                    }
                }

                Inventory merged;
                try {
                    // we ignore the field that holds the occurrences and those that are replicated in the OBSERVED_BY
                    merged = BeanUtils.mergeBeans(Inventory.class
                            , Arrays.asList("unmatchedOccurrences", "latitude", "longitude", "ID", "key", "databaseId", "code"), tmp.toArray(new Inventory[tmp.size()]));
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                    e.printStackTrace();
                    throw new FloraOnException(e.getMessage());
                } catch (FloraOnException e) {
                    throw new FloraOnException(Messages.getString("error.3"));
                }

                // assemble all species found in these inventories into the merged one, and set the codes from respective inventories
                List<newOBSERVED_IN> occ = new ArrayList<>();
                newOBSERVED_IN tmpo;
                for (Inventory inventory : tmp) {
                    if(inventory._getOccurrences().size() == 0) {
                        tmpo = new newOBSERVED_IN();
                        tmpo.setGpsCode(inventory.getCode());
                        occ.add(tmpo);
                    } else {
                        for (newOBSERVED_IN noi : inventory._getOccurrences()) {
                            if (noi.getGpsCode() == null) noi.setGpsCode(inventory.getCode());
                        }
                        occ.addAll(inventory._getOccurrences());
                    }
                }
                merged.setUnmatchedOccurrences(occ);
                System.out.println(gs.toJson(merged));
                driver.getOccurrenceDriver().createInventory(merged);

                for(Inventory tmp1 : tmp) {
                    driver.getOccurrenceDriver().deleteOccurrences(
                            new String[] {tmp1.getID()}, new String[] {tmp1._getOccurrences().size() == 0 ? ""
                                    : tmp1._getOccurrences().get(0).getUuid().toString()});
                }
/*

                Set<String> toDelete = new HashSet<>();
                for(Inventory tmp1 : tmp)
                    toDelete.add(tmp1.getID());

                for(String td : toDelete)
                    driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(td));
*/

                success("Ok");
                break;
        }
    }
}