package pt.floraon.occurrences.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.*;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.IOccurrenceDriver;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.TaxonomicChange;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by miguel on 24-03-2017.
 */
public class OccurrenceArangoDriver extends GOccurrenceDriver implements IOccurrenceDriver {
    private ArangoDatabase database;

    public OccurrenceArangoDriver(IFloraOn driver) {
        super(driver);
        this.database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public void createInventory(Inventory inventory) {
        database.collection(Constants.NodeTypes.inventory.toString()).insertDocument(inventory);
    }

    @Override
    public Iterator<Inventory> getOccurrencesOfTaxon(INodeKey taxEntId) throws DatabaseException {
        // TODO: this should return the OBSERVED_IN graph links, not the unmatched
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.1", taxEntId.getID())
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getUnmatchedOccurrences() throws DatabaseException {
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.6")
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getUnmatchedOccurrencesOfMaintainerCount(INodeKey userId) throws DatabaseException {
        String query = userId == null ? "occurrencequery.6a.nouser.count" : "occurrencequery.6a.count";
        try {
            return database.query(
                    AQLOccurrenceQueries.getString(query, userId == null ? null : userId.toString())
                    , null, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getUnmatchedOccurrencesOfMaintainer(INodeKey userId) throws DatabaseException {
        String query = (userId == null ? "occurrencequery.6b" : "occurrencequery.6a");
        try {
            return database.query(
                    AQLOccurrenceQueries.getString(query, userId == null ? null : userId.toString())
                    , null, new AqlQueryOptions().ttl(120), Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getOccurrencesByUuid(INodeKey authorId, String[] uuid) throws DatabaseException {
        // TODO: this should return the OBSERVED_IN graph links, not the unmatched
        if(uuid == null || uuid.length == 0) return Collections.emptyIterator();
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2b", authorId.getID()
                            , "[\"" + StringUtils.implode("\",\"", uuid) + "\"]")
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    @Override
    public Iterator<Inventory> getOccurrencesOfObserver(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(authorId == null) return getOccurrencesOfMaintainer(null, offset, count);
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("observer", authorId.toString());
        bindVars.put("off", offset == null ? 0 : offset);
        bindVars.put("cou", count == null ? 999999 : count);

        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2")
                    , bindVars, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getOccurrencesOfObserverWithinDates(INodeKey authorId, Date from, Date to, Integer offset, Integer count) throws DatabaseException {
        if(authorId == null) return getOccurrencesOfMaintainer(null, offset, count);
        DateFormat df = Constants.dateFormatYMD.get();
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("observer", authorId.toString());
        bindVars.put("off", offset == null ? 0 : offset);
        bindVars.put("cou", count == null ? 999999 : count);
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));

        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2.date")
                    , bindVars, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getOccurrencesOfMaintainer(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        String query = authorId == null ?
                AQLOccurrenceQueries.getString("occurrencequery.4.nouser", null, offset, count)
                : AQLOccurrenceQueries.getString("occurrencequery.4", authorId.getID(), offset, count);
        try {
            return database.query(query, null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getOccurrencesOfMaintainerCount(INodeKey authorId) throws DatabaseException {
        String query = authorId == null ?
                AQLOccurrenceQueries.getString("occurrencequery.4b.nouser")
                : AQLOccurrenceQueries.getString("occurrencequery.4b", authorId.getID());
        try {
            return database.query(query, null, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getInventoriesOfObserver(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2a", authorId.getID(), offset, count)
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getInventoriesOfMaintainer(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        String query = authorId == null ?
                AQLOccurrenceQueries.getString("occurrencequery.4a.nouser", null, offset, count)
                : AQLOccurrenceQueries.getString("occurrencequery.4a", authorId.getID(), offset, count);

        try {
            return database.query(query, null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> findInventoriesByFilter(String filter, INodeKey userId, Integer offset, Integer count) throws FloraOnException {
        Map<String, Object> bindVars = new HashMap<>();
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        bindVars.put("query", "%" + filter + "%");
        bindVars.put("offset", offset);
        bindVars.put("count", count);
        if(userId != null)
            bindVars.put("user", userId.toString());

        try {
            return database.query(
                    AQLOccurrenceQueries.getString(userId == null ? "occurrencequery.9a" : "occurrencequery.9")
                    , bindVars, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getInventoriesOfMaintainerCount(INodeKey authorId) throws DatabaseException {
        String query = authorId == null ? AQLOccurrenceQueries.getString("occurrencequery.4a.nouser.count")
                : AQLOccurrenceQueries.getString("occurrencequery.4a.count", authorId.getID());
        try {
            return database.query(query, null, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getInventoriesByIds(String[] inventoryIds) throws DatabaseException {
        Map<String, Object> bp = new HashMap<>();
        bp.put("ids", inventoryIds);
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
        ArangoCursor<Inventory> c;
        try {
             c = database.query(AQLOccurrenceQueries.getString("occurrencequery.5")
                    , bp, new AqlQueryOptions().count(true), Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
        return c;
    }

    @Override
    public boolean discardUploadedTable(INodeKey authorId, String filename) throws FloraOnException {
        User user = driver.getAdministration().getUser(authorId);
        List<String> tmp = user.getUploadedTables();
        if(!tmp.contains(filename)) return false;
        tmp.remove(filename);
        driver.getNodeWorkerDriver().updateDocument(authorId, "uploadedTables", tmp);
        return true;
    }

    @Override
    public int deleteInventoriesOrOccurrences(String[] inventoryId, String[] uuid) throws FloraOnException {
        int count = 0;
        for (int i = 0; i < inventoryId.length; i++) {
            try {
                if(StringUtils.isArrayEmpty(uuid) || uuid[i].trim().equals("")) {
//                    driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(inventoryId[i]));  // FIXME: check for connected links
                    driver.getNodeWorkerDriver().deleteVertexOrEdge(driver.asNodeKey(inventoryId[i]));
                } else {
                    Inventory inv = database.query(
                            AQLOccurrenceQueries.getString("occurrencequery.3", inventoryId[i], uuid[i])
                            , null, null, Inventory.class).next();
                    // if the inventory became empty, delete the inventory
                    if(inv._getOccurrences().size() == 0)
                        driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(inventoryId[i]));
                }
            } catch (ArangoDBException e) {
                e.printStackTrace();
                continue;
            }
            count++;
        }
        return count;
    }

    @Override
    public Inventory updateInventory(Inventory newinv) throws FloraOnException {
/*
        if(inv._getOccurrences().size() == 0)
            return driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(inv.getID()), inv, false, Inventory.class);
        else {
*/
// TODO HERE changed coords
            Inventory original = driver.getNodeWorkerDriver().getNode(driver.asNodeKey(newinv.getID()), Inventory.class);
            Map<UUID, OBSERVED_IN> origMap = new HashMap<>();
            Set<UUID> alreadUpdated = new HashSet<>();
            for(OBSERVED_IN occ : original._getOccurrences())
                origMap.put(occ.getUuid(), occ);

            Map<UUID, OBSERVED_IN> updMap = new HashMap<>(origMap);

            for(OBSERVED_IN eachNewOcc : newinv._getOccurrences()) {
                if(origMap.containsKey(eachNewOcc.getUuid())) {    // the original inventory contains this occurrence
                    if(alreadUpdated.contains(eachNewOcc.getUuid())) { // this occurrence was already updated, so add new and copy
                        UUID newUuid = UUID.randomUUID();
                        OBSERVED_IN newOcc;
                        try {
                            newOcc = BeanUtils.updateBean(OBSERVED_IN.class, null, origMap.get(eachNewOcc.getUuid()), eachNewOcc);
                            newOcc.setUuid(newUuid);
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new FloraOnException(e.getMessage());
                        }
                        updMap.put(newUuid, newOcc);
                    } else {    // update existing occurrence
                        try {
                            OBSERVED_IN tmpori = origMap.get(eachNewOcc.getUuid());
//                            System.out.println(original.getLatitude()+", "+original.getLongitude()+", "+tmpori.getObservationLatitude()+", "+tmpori.getObservationLongitude()+", "+eachNewOcc.getObservationLatitude()+", "+eachNewOcc.getObservationLongitude());
                            if(tmpori.getObservationLatitude() == null || tmpori.getObservationLongitude() == null) {
                                if(original.getLatitude() != null && original.getLongitude() != null
                                        && eachNewOcc.getObservationLatitude() != null && eachNewOcc.getObservationLongitude() != null) {
                                    eachNewOcc.setCoordinatesChanged(true);
                                }
                            } else {
                                if (eachNewOcc.getObservationLatitude() != null && eachNewOcc.getObservationLongitude() != null)
                                    eachNewOcc.setCoordinatesChanged(true);
                            }
                            updMap.put(eachNewOcc.getUuid()
                                    , BeanUtils.updateBean(OBSERVED_IN.class, null, tmpori, eachNewOcc));
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new FloraOnException(e.getMessage());
                        }
                    }
                } else  // this occurrence is new
                    updMap.put(eachNewOcc.getUuid(), eachNewOcc);
                alreadUpdated.add(eachNewOcc.getUuid());
            }

            newinv.setUnmatchedOccurrences(new ArrayList<>(updMap.values()));
            return driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(newinv.getID()), newinv, false, Inventory.class);
       // }
    }

    @Override
    public void replaceTaxEntMatch(Map<String, TaxonomicChange> changes) throws FloraOnException {
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gs.toJson(changes));
        Map<String, Object> bindVars;
        String tmp;

        for(Map.Entry<String, TaxonomicChange> change : changes.entrySet()) {
            bindVars = new HashMap<>();
            bindVars.put("user", change.getValue().getUserId());
            bindVars.put("uuids", change.getValue().getUuids());
            if(change.getValue().getTargetTaxEntId().equals("NM"))
                tmp = "";
            else if(change.getValue().getTargetTaxEntId().equals("NA")) {
                TaxEnt te = driver.getNodeWorkerDriver().createTaxEntFromTaxEnt(TaxEnt.parse(change.getKey()));
                tmp = te.getID();
            } else
                tmp = change.getValue().getTargetTaxEntId();
            bindVars.put("replace", tmp);

            try {
                database.query(
                        AQLOccurrenceQueries.getString(change.getValue().getUserId() == null ? "occurrencequery.7a" : "occurrencequery.7")
                        , bindVars, null, null);
            } catch (ArangoDBException e) {
                e.printStackTrace();
                throw new DatabaseException(e.getMessage());
            }
        }
    }

    @Override
    public Iterator<Inventory> findOccurrencesByFilter(String filter, INodeKey userId, Integer offset, Integer count) throws FloraOnException {
        Map<String, Object> bindVars = new HashMap<>();
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        bindVars.put("query", "%" + filter + "%");
        bindVars.put("offset", offset);
        bindVars.put("count", count);
        if(userId != null)
            bindVars.put("user", userId.toString());

        try {
            return database.query(
                    AQLOccurrenceQueries.getString(userId == null ? "occurrencequery.8a" : "occurrencequery.8")
                    , bindVars, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int findOccurrencesByFilterCount(String filter, INodeKey userId) throws FloraOnException {
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("query", "%" + filter + "%");
        if(userId != null)
            bindVars.put("user", userId.toString());

        try {
            return database.query(
                    AQLOccurrenceQueries.getString(userId == null ? "occurrencequery.8a.count" : "occurrencequery.8.count")
                    , bindVars, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}