package pt.floraon.entities;

import pt.floraon.driver.DatabaseException;

public class Attribute extends NamedDBNode {
	protected String shortName,description;
	
	public Attribute(String name,String shortName,String description) throws DatabaseException {
		super(name);
		this.shortName=shortName;
		this.description=description;
	}
	/*
	public AttributeVertex(Attribute at) {
		super(at.baseNode);
		this.name=at.baseNode.name;
		this.shortName=at.baseNode.shortName;
		this.description=at.baseNode.description;
	}	*/
	
	public Attribute(Attribute at) throws DatabaseException {
		super(at);
		this.shortName=at.shortName;
		this.description=at.description;
	}	
}
