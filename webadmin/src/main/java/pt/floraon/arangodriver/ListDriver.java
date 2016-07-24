package pt.floraon.arangodriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.util.GraphVerticesOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.Constants;
import pt.floraon.driver.BaseFloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.IListDriver;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.results.TaxEntAndNativeStatusResult;
import pt.floraon.results.SimpleTaxEntResult;

public class ListDriver extends BaseFloraOnDriver implements IListDriver {
	protected ArangoDriver dbDriver;
	public ListDriver(FloraOn driver) {
		super(driver);
		this.dbDriver=(ArangoDriver) driver.getArangoDriver();
	}

	public List<ChecklistEntry> getCheckList() {
		// TODO the query is very slow!
		List<ChecklistEntry> chklst=new ArrayList<ChecklistEntry>();
        @SuppressWarnings("rawtypes")
		CursorResult<List> vertexCursor;
        @SuppressWarnings("rawtypes")
        Iterator<List> vertexIterator;
    	GraphVerticesOptions gvo=new GraphVerticesOptions();
    	List<String> vcr=new ArrayList<String>();
    	vcr.add("taxent"); //$NON-NLS-1$
    	gvo.setVertexCollectionRestriction(vcr);
    	String query=String.format(
			AQLQueries.getString("ListDriver.1") //$NON-NLS-1$
   			, Constants.TAXONOMICGRAPHNAME,RelTypes.PART_OF.toString(),Constants.CHECKLISTFIELDS);

    	try {
    		// traverse all leaf nodes outwards
    		vertexCursor=dbDriver.executeAqlQuery(query, null, null, List.class);
			vertexIterator = vertexCursor.iterator();
			
			ChecklistEntry chk;
			while (vertexIterator.hasNext()) {
				@SuppressWarnings("unchecked")
				List<LinkedTreeMap<String,Object>> entry1 = vertexIterator.next();
				chk=new ChecklistEntry();
				for(LinkedTreeMap<String,Object> tev:entry1) {
					TaxEnt te=new TaxEnt(tev);
					if(te.isSpeciesOrInferior()) {
						if(chk.canonicalName==null) {
							chk.taxon=te.getFullName();
							chk.canonicalName=te.getName();
						}
					}
					switch(te.getRank()) {
					case GENUS:
						chk.genus=te.getName();
						break;
					case FAMILY:
						chk.family=te.getName();
						break;
					case ORDER:
						chk.order=te.getName();
						break;
					default:
						break;
					}
				}
				chklst.add(chk);
			}
		} catch (ArangoException | FloraOnException e) {
			e.printStackTrace();
		}
    	return chklst;
	}
	
	@Override
    public GraphUpdateResult getAllTerritoriesGraph(TerritoryTypes territoryType) throws FloraOnException {
    	String query;
    	if(territoryType!=null)
    		query=String.format(AQLQueries.getString("ListDriver.2"),NodeTypes.territory.toString(), territoryType.toString()); //$NON-NLS-1$
    	else
    		query=String.format(AQLQueries.getString("ListDriver.3"),NodeTypes.territory.toString()); //$NON-NLS-1$
    	String[] ids=new String[0];
    	try {
			ids=dbDriver.executeAqlQuery(query, null, null, String.class).asList().toArray(ids);
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return driver.getNodeWorkerDriver().getRelationshipsBetween(ids, new Facets[] {Facets.TAXONOMY});
    }

	@Override
    public Iterator<Territory> getChecklistTerritories() throws FloraOnException {
    	String query;
		query=String.format(AQLQueries.getString("ListDriver.4"),NodeTypes.territory.toString()); //$NON-NLS-1$
    	try {
			return dbDriver.executeAqlQuery(query, null, null, Territory.class).iterator();
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    }

	@Override
    public Iterator<Territory> getAllTerritories(TerritoryTypes territoryType) throws FloraOnException {
    	String query;
    	if(territoryType!=null)
    		query=String.format(AQLQueries.getString("ListDriver.5"),NodeTypes.territory.toString(), territoryType.toString()); //$NON-NLS-1$
    	else
    		query=String.format(AQLQueries.getString("ListDriver.6"),NodeTypes.territory.toString()); //$NON-NLS-1$
    	try {
			return dbDriver.executeAqlQuery(query, null, null, Territory.class).iterator();
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    }


	@Override
	public <T extends SimpleTaxEntResult> Iterator<T> getAllSpeciesOrInferior(boolean onlyLeafNodes, Class<T> T, Boolean onlyCurrent, String territory, String filter, Integer offset, Integer count) throws FloraOnException {
		String query = null;
		boolean withLimit=false;
		if(!(offset==null && count==null)) {
			if(offset==null) offset=0;
			if(count==null) count=50;
			withLimit=true;
		}
		if(territory==null) {
			query=AQLQueries.getString("ListDriver.7" //$NON-NLS-1$
				, onlyLeafNodes ? "&& npar==0" : "" //$NON-NLS-1$ //$NON-NLS-2$
				, NodeTypes.taxent.toString()
				, withLimit ? "LIMIT "+offset+","+count : "" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				, onlyCurrent ? "&& thistaxon.current" : "" //$NON-NLS-1$ //$NON-NLS-2$
				, filter == null ? "" : "FILTER LIKE(thistaxon.name, '%%" + filter + "%%', true) " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				, onlyCurrent ? "FILTER v.current==true" : "" //$NON-NLS-1$ //$NON-NLS-2$
			);
		} else {
			if(onlyLeafNodes) System.out.println("Warning: possibly omitting taxa from the checklist."); //$NON-NLS-1$
//FIXME must traverse territories downwards in thr 1st line! Wait for ArangoDB 2.8
/*			query=String.format(	// FIXME update with the above
				"FOR te IN territory FILTER te.shortName=='%3$s' FOR v IN (FOR v1 IN NEIGHBORS(territory, EXISTS_IN, te, 'inbound') RETURN DOCUMENT(v1)) "
				+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) "
				+ "%1$s SORT v.name %4$s "
				+ "RETURN MERGE(KEEP(v,'_id','name','author','current'), {leaf: npar==0, territories: "
				+ "(LET terr=SLICE(TRAVERSAL(taxent, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}),1) "
				+ "LET upstr=(FOR t IN terr LET tt=TRAVERSAL(territory, PART_OF, t.vertex, 'outbound') LET bs=t.path.edges[0].nativeStatus "
				+ "RETURN {baseStatus: ZIP([t.vertex.shortName], [bs]), upstreamStatus: ZIP(SLICE(tt[*].vertex.shortName,1), (FOR i IN 2..LENGTH(tt) RETURN bs == 'ENDEMIC' ? 'ENDEMIC' : 'EXISTING')) }) "
				+ "LET bs=LENGTH(upstr) == 1 ? upstr[0].baseStatus : APPLY('MERGE',upstr[*].baseStatus) LET an=UNIQUE(FLATTEN(FOR up IN upstr RETURN ATTRIBUTES(up.upstreamStatus))) LET anc=LENGTH(an)>0 ? MINUS(an, ATTRIBUTES(bs)) : [] "
				+ "LET inferr=(FOR ut IN anc LET tmp=REMOVE_VALUE(UNIQUE(FOR up IN upstr RETURN TRANSLATE(ut,up.upstreamStatus,null)),null) RETURN ZIP([ut], [LENGTH(tmp) == 1 ? tmp[0] : POSITION(tmp,'ENDEMIC',false) ? 'ENDEMIC' : 'EXISTING']) )"
				+ "RETURN MERGE(bs, LENGTH(inferr) == 1 ? inferr[0] : APPLY('MERGE',inferr) ) "
				+ ")[0]})"
				, onlyLeafNodes ? " FILTER npar==0" : "", NodeTypes.taxent.toString(), territory, withLimit ? "LIMIT "+offset+","+count : "");*/
/*					"FOR t IN territory FILTER t.shortName=='%3$s' FOR v IN FLATTEN(FOR v1 IN GRAPH_TRAVERSAL('taxgraph', t, 'inbound', {filterVertices: [{isSpeciesOrInf: true}], vertexFilterMethod:'exclude'}) RETURN v1[*].vertex) "
				+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) "
				+ "%1$s LET terr=TRAVERSAL(%2$s, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}) SORT v.name RETURN {_id:v._id,name:v.name,author:v.author,leaf:npar==0, current:v.current"
				+ ", territories:(LET d=SLICE(terr,1) RETURN ZIP(d[*].vertex.shortName, d[*].path.edges[0].nativeStatus))[0]}"	//DOCUMENT(terr)[*].shortName
				, onlyLeafNodes ? " FILTER npar==0" : "", NodeTypes.taxent.toString(), territory);*/
		}
		//System.out.println(query);
    	CursorResult<T> vertexCursor;
		try {
			vertexCursor = dbDriver.executeAqlQuery(query, null, null, T);
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return vertexCursor.iterator();
	}

	@Override
	public Iterator<TaxEnt> getAllOfRank(TaxonRanks rank) throws FloraOnException {
		String query=String.format(AQLQueries.getString("ListDriver.21") //$NON-NLS-1$
			,NodeTypes.taxent.toString(),rank.getValue());
    	CursorResult<TaxEnt> vertexCursor;
		try {
			vertexCursor = dbDriver.executeAqlQuery(query, null, null, TaxEnt.class);
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return vertexCursor.iterator();			
	}
	
	@Override
	public GraphUpdateResult getAllCharacters() {
			String query=AQLQueries.getString("ListDriver.22", NodeTypes.character.toString());
			String res;
			try {
				res = dbDriver.executeAqlQueryJSON(query, null, null);
			} catch (ArangoException e) {
				System.err.println(e.getErrorMessage());
				return GraphUpdateResult.emptyResult();
			}
			// NOTE: server responses are always an array, but here we always have one element, so we remove the []
			return (res==null || res.equals("[]")) ? GraphUpdateResult.emptyResult() : new GraphUpdateResult(res.substring(1, res.length()-1)); //$NON-NLS-1$
		}

	@Override
	public JsonObject getTaxonInfo(INodeKey key) throws FloraOnException {
		String query=AQLQueries.getString("ListDriver.24a", key.toString());

		TaxEntAndNativeStatusResult result;
		try {
			result = dbDriver.executeAqlQuery(query, null, null, TaxEntAndNativeStatusResult.class).getUniqueResult();
			if(result == null)
				throw new FloraOnException("Taxon "+key+" not found.");
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return result.toJson().getAsJsonObject();
	}

	@Override
	public JsonElement getTaxonInfo(String taxonName, boolean onlyCurrent) throws FloraOnException {
		JsonArray out = new JsonArray();
		String query=AQLQueries.getString("ListDriver.24b", taxonName, onlyCurrent ? "&& thistaxon.current" : "");
		Iterator<TaxEntAndNativeStatusResult> cursorResult;
		try {
			cursorResult = dbDriver.executeAqlQuery(query, null, null, TaxEntAndNativeStatusResult.class).iterator();
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
		
		while(cursorResult.hasNext()) {
			out.add(cursorResult.next().toJson());
		}
    	return out;
	}

	@Override
	public JsonObject getTaxonInfo(int oldId) throws FloraOnException {
		String query=AQLQueries.getString("ListDriver.24c", oldId);

		TaxEntAndNativeStatusResult result;
		try {
			result = dbDriver.executeAqlQuery(query, null, null, TaxEntAndNativeStatusResult.class).getUniqueResult();
			if(result == null)
				throw new FloraOnException("Taxon with oldId "+oldId+" not found.");
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return result.toJson().getAsJsonObject();
	}
}
