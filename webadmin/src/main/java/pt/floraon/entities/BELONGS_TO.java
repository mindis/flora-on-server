package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;

public class BELONGS_TO extends GeneralDBEdge {

	@Override
	public RelTypes getType() {
		return RelTypes.BELONGS_TO;
	}

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
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
