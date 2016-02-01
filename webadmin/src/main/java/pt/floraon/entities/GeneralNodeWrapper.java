package pt.floraon.entities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.arangodb.ArangoException;

import pt.floraon.driver.ArangoKey;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.results.GraphUpdateResult;

public abstract class GeneralNodeWrapper {
	protected FloraOnDriver graph;
	protected Boolean dirty;
	protected GeneralDBNode baseNode;
	
	/**
	 * Saves this node to database
	 * @throws IOException
	 * @throws ArangoException
	 */
	public abstract void commit() throws FloraOnException, ArangoException;

	public String getID() {
		return baseNode._id;
	}
	
	public ArangoKey getArangoKey() throws ArangoException {
		return ArangoKey.fromString(baseNode._id);
	}
	
	public GeneralDBNode getNode() {
		return this.baseNode;
	}

	/**
	 * Creates a relationship of any type
	 * @param parent
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws ArangoException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public GraphUpdateResult createRelationshipTo(GeneralDBNode parent,RelTypes type) throws IOException, ArangoException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(baseNode._id==null) throw new IOException("Node "+baseNode._id+" not attached to DB");

		// checks whether there is already a relation of this type between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,parent._id,type.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			if(type.getDirectionality().equals(Constants.Directionality.UNIDIRECTIONAL)) {
				return GraphUpdateResult.fromHandles(this.graph, new String[] {
					this.graph.driver.createEdge(type.toString(), type.getEdge(), baseNode._id, parent._id, false, false).getDocumentHandle()
					,baseNode._id,parent._id
				});
			} else {	// in bidirectional links we add two links so that we don't have to worry abound directionality in queries
				return GraphUpdateResult.fromHandles(this.graph, new String[] {
					this.graph.driver.createEdge(type.toString(), type.getEdge(), baseNode._id, parent._id, false, false).getDocumentHandle()
					,this.graph.driver.createEdge(type.toString(), type.getEdge(), parent._id, baseNode._id, false, false).getDocumentHandle()
					,baseNode._id,parent._id
				});
			}
		} else return GraphUpdateResult.emptyResult();
	}
	
	/**
	 * Sets in the DB this node as PART_OF another node. Only adds a new relation if it doesn't exist. 
	 * @param parent
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setPART_OF(GeneralDBNode parent) throws ArangoException, FloraOnException {
		if(baseNode._id==null) throw new FloraOnException("Node "+baseNode._id+" not attached to DB");

		// checks whether there is already a PART_OF relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,parent._id,RelTypes.PART_OF.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			this.graph.driver.createEdge(RelTypes.PART_OF.toString(), new PART_OF(true), baseNode._id, parent._id, false, false);
			return 1;
		} else return 0;
	}

	/**
	 * Sets in the DB this node as PART_OF another node. Only adds a new relation if it doesn't exist.
	 * @param parent
	 * @return
	 * @throws ArangoException
	 * @throws FloraOnException
	 */
	public int setPART_OF(ArangoKey parent) throws ArangoException, FloraOnException {
		if(baseNode._id==null) throw new FloraOnException("Node "+baseNode._id+" not attached to DB");

		// checks whether there is already a PART_OF relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,parent.toString(),RelTypes.PART_OF.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			this.graph.driver.createEdge(RelTypes.PART_OF.toString(), new PART_OF(true), baseNode._id, parent.toString(), false, false);
			return 1;
		} else return 0;
	}

}