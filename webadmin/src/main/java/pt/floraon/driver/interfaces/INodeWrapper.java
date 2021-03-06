package pt.floraon.driver.interfaces;

import java.lang.reflect.InvocationTargetException;

import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.entities.GeneralDBEdge;
import pt.floraon.driver.results.GraphUpdateResult;

public interface INodeWrapper {
	/**
	 * Sets in the DB this node as PART_OF another node. Only adds a new relation if it doesn't exist. 
	 * @param parent
	 * @throws FloraOnException
	 */
	int setPART_OF(INodeKey parent) throws FloraOnException;
	/**
	 * Creates a relationship of any type. NOTE: this uses reflection.
	 * @param parent
	 * @param type
	 * @return
	 * @throws FloraOnException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	GraphUpdateResult createRelationshipTo(INodeKey parent,RelTypes type) throws FloraOnException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException;

}
