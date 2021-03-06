package pt.floraon.occurrences.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.entities.GeneralDBEdge;

public class OBSERVED_BY extends GeneralDBEdge {
	protected Boolean isMainObserver;

	public OBSERVED_BY() {
		super();
	}

	public OBSERVED_BY(String from, String to) {
		super(from, to);
	}

	public OBSERVED_BY(Boolean isMainObserver) {
		this();
		this.isMainObserver=isMainObserver;
	}
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.OBSERVED_BY;
	}

}
