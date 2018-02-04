package pt.floraon.driver.interfaces;

import java.util.List;

import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.CSVFileProcessor;
import pt.floraon.redlistdata.entities.RedListSettings;
import pt.floraon.taxonomy.entities.Territory;

/**
 * The constructor of a IFloraOn must either initialize a fresh new database, or check if there's one already functional.
 */
public interface IFloraOn {
	Object getDatabaseDriver();
	Object getDatabase();
	INodeWorker getNodeWorkerDriver();
	IQuery getQueryDriver();
	IListDriver getListDriver();
	CSVFileProcessor getCSVFileProcessor();
	IRedListDataDriver getRedListData();
	IOccurrenceDriver getOccurrenceDriver();
	IOccurrenceReportDriver getOccurrenceReportDriver();
	IAdministration getAdministration();
	INodeWrapper wrapNode(INodeKey node) throws FloraOnException;
	ITaxEntWrapper wrapTaxEnt(INodeKey node) throws FloraOnException;
	IAttributeWrapper wrapAttribute(INodeKey node) throws FloraOnException;
	List<Territory> getChecklistTerritories();
	INodeKey asNodeKey(String id) throws FloraOnException;
    void reloadSettings();
    RedListSettings getRedListSettings(String territory);
    boolean hasFailed();
    String getErrorMessage();
}
