package pt.floraon.redlistdata;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.model.*;
import pt.floraon.driver.*;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.dataproviders.FloraOnExternalDataProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by miguel on 05-11-2016.
 */
public class ArangoDBRedListData extends BaseFloraOnDriver implements IRedListData {
    final private ArangoDB dbDriver;
    final protected ArangoDatabase database;
    final private List<ExternalDataProvider> externalDataProviders = new ArrayList<>();

    public ArangoDBRedListData(IFloraOn driver) {
        super(driver);
        dbDriver = (ArangoDB) driver.getDatabaseDriver();
        database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public List<String> getRedListTerritories() {
        List<String> redListTerritories = new ArrayList<>();
        // search for the red list datasets
        // each dataset is a collection with the name redlist_<territory>
        // <teritory> is the short name of the territory
        for(CollectionEntity ce : database.getCollections()) {
            if(ce.getName().startsWith("redlist_")) {
                String terr = ce.getName().substring(8);
                redListTerritories.add(terr);
            }
        }
        return redListTerritories;
    }

    @Override
    public void initializeRedListData(Properties properties) throws FloraOnException {
        try {
            for(String op : getPropertyList(properties, "occurrenceProvider")) {
                externalDataProviders.add(new FloraOnExternalDataProvider(new URL(op)));    // TODO use reflection
            }
        } catch (MalformedURLException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    public List<ExternalDataProvider> getExternalDataProviders() {
        return externalDataProviders;
    }

    @Override
    public RedListDataEntity createRedListDataEntity(String territory, RedListDataEntity rlde) throws DatabaseException {
        try {
            return database.collection("redlist_" + territory).insertDocument(rlde, new DocumentCreateOptions().returnNew(true)).getNew();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public RedListDataEntity getRedListDataEntity(String territory, INodeKey taxEntId) throws DatabaseException {

        try {
            return database.query(AQLRedListQueries.getString("redlistdata.2", territory, taxEntId), null, null, RedListDataEntity.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public List<RedListDataEntity> getAllRedListTaxa(String territory) throws FloraOnException {
        try {
            return database.query(AQLRedListQueries.getString("redlistdata.1", territory), null, null, RedListDataEntity.class).asListRemaining();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void initializeRedListDataForTerritory(String territory) throws FloraOnException {
        final String collectionName = "redlist_" + territory;
        try {
            database.collection(collectionName).getInfo();
        } catch (ArangoDBException e) {
            try {
                database.createCollection(collectionName, new CollectionCreateOptions().type(CollectionType.DOCUMENT));
                database.collection(collectionName).createHashIndex(Arrays.asList("taxEntID"), new HashIndexOptions().unique(true).sparse(false));
            } catch (ArangoDBException e1) {
                throw new FloraOnException(e1.getMessage());
            }
        }

    }

    @Override
    public RedListDataEntity updateRedListDataEntity(String territory, INodeKey id, RedListDataEntity rlde, boolean replace)
            throws DatabaseException {
        DocumentUpdateEntity<RedListDataEntity> out;
        try {
            out = database.collection("redlist_" + territory).updateDocument(id.getDBKey(), rlde
                    , new DocumentUpdateOptions().serializeNull(replace).keepNull(false).returnNew(true).waitForSync(true));
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
        return out.getNew();
    }
}
