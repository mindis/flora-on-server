package pt.floraon.occurrences.arangodb;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import org.apache.commons.collections.iterators.EmptyIterator;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.*;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;

import java.util.Collections;
import java.util.Iterator;
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
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2", authorId.getID(), offset, count)
                    , null, null, Inventory.class);
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
    public boolean discardUploadedTable(INodeKey authorId, String filename) throws FloraOnException {
        User user = driver.getAdministration().getUser(authorId);
        List<String> tmp = user.getUploadedTables();
        if(!tmp.contains(filename)) return false;
        tmp.remove(filename);
        driver.getNodeWorkerDriver().updateDocument(authorId, "uploadedTables", tmp);
        return true;
    }

    @Override
    public int deleteOccurrences(String[] inventoryId, String[] uuid) throws FloraOnException {
        int count = 0;
        for (int i = 0; i < inventoryId.length; i++) {
            try {
                if(uuid[i].trim().equals("")) {
                    driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(inventoryId[i]));
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

}