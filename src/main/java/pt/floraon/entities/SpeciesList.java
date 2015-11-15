package pt.floraon.entities;

import java.io.IOException;
import java.util.Iterator;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.AllRelTypes;
import pt.floraon.server.Constants.NodeTypes;

public class SpeciesList extends SpeciesListVertex implements VertexWrapper {
	private VertexEntity<SpeciesListVertex> vertexEntity=null;
	private FloraOnGraph graph;
	private Boolean dirty=false;

	public SpeciesList(Float latitude, Float longitude, Integer year, Integer month, Integer day,Integer precision) {
		super(latitude, longitude, year, month, day, precision);
	}

	public SpeciesList(FloraOnGraph graph,Float latitude, Float longitude, Integer year, Integer month, Integer day,Integer precision) throws ArangoException {
		super(latitude, longitude, year, month, day, precision);
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.specieslist.toString(), new SpeciesListVertex(this), false);
		super._id=this.vertexEntity.getDocumentHandle();
	}
	
	public SpeciesList(FloraOnGraph graph,Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision,Integer area,String comment,Boolean complete) throws ArangoException {
		super(latitude, longitude, year, month, day,precision,area,comment,complete);
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.specieslist.toString(), new SpeciesListVertex(this), false);
		super._id=this.vertexEntity.getDocumentHandle();
	}

	public SpeciesList(SpeciesListVertex slv) {
		super(slv);
	}
	
	public SpeciesList(FloraOnGraph fog,SpeciesListVertex slv) {
		super(slv);
		this.graph=fog;
	}

	public void setPrecision(Integer precision) {
		this.precision=precision;
		this.dirty=true;
	}
	
	public Integer getPrecision() {
		return this.precision;
	}

	@Override
	public String toString() {
		return "Species list at "+this.location[0]+"º "+this.location[1]+"º on "+this.year+"/"+this.month+"/"+this.day;
	}
	/**
	 * Create a new OBSERVED_BY between this species list and an author. If it exists already, nothing happens.
	 * @param aut The {@link Author}
	 * @param isMainAuthor Whether this is the main author (whose name comes first) or a secondary author (order arbitrary)
	 * @return 1 if relation was created, 0 if not
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setObservedBy(Author aut,Boolean isMainAuthor) throws IOException, ArangoException {
// TODO if it is main observer, can only be one!
		if(this._id==null) throw new IOException("Species list not attached to DB");
		String query=String.format("FOR au IN author FILTER au.idAut==%2$d UPSERT {_from:'%1$s',_to:au._id} INSERT {_from:'%1$s',_to:au._id,main:%3$b} UPDATE {} IN OBSERVED_BY RETURN OLD ? 0 : 1",this.getID(),aut.idAut,isMainAuthor);
//		System.out.println(query);
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
	}

	/**
	 * Create a new OBSERVED_BY between this species list and an author. If it exists already, nothing happens.
	 * @param idaut The ID of the author
	 * @param isMainAuthor
	 * @return
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setObservedBy(int idaut,Boolean isMainAuthor) throws IOException, ArangoException {
		if(this._id==null || this.graph==null) throw new IOException("Species list not attached to DB");
		String query=String.format("FOR au IN author FILTER au.idAut==%2$d UPSERT {_from:'%1$s',_to:au._id} INSERT {_from:'%1$s',_to:au._id,main:%3$b} UPDATE {} IN OBSERVED_BY RETURN OLD ? 0 : 1",this.getID(),idaut,isMainAuthor);
//		System.out.println(query);
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
	}

	/**
	 * Gets all the species of this species list
	 * @return
	 */
	public Iterator<TaxEntVertex> getSpecies() {
		// TODO
		return null;
	}

	@Override
	public void saveToDB() {
		// TODO Auto-generated method stub
		
	}
}
