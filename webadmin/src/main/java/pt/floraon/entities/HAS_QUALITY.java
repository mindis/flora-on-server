package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;

public class HAS_QUALITY extends GeneralDBEdge {
/*	public HAS_QUALITY() {}
	public HAS_QUALITY(String from,String to) {
		this._from=from;
		this._to=to;
	}*/

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.HAS_QUALITY;
	}

	@Override
	public JsonObject toJson() {
		return super._toJson();
	}

	@Override
	public String toJsonString() {
		return this.toJson().toString();
	}

}
