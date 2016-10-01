package pt.floraon.entities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
/**
 * Represents either a node or an edge in the graph.
 * All graph entities must extend this class.
 * @author miguel
 *
 */
public abstract class DBEntity {
	protected String _id = null, _key = null;
	
	/**
	 * 
	 * @return The document handle
	 */
	public String getID() {
		return this._id;
	}

	public String getIDURLEncoded() {
		try {
			return URLEncoder.encode(this._id, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setID(String id) {
		this._id=id;
	}

	public void setKey(String key) {
		this._key=key;
	}

	public DBEntity() {
	}
	
	public DBEntity(DBEntity n) {
		this._id=n._id;
		this._key=n._key;
	}

	/**
	 * Gets the collection, i.e. the canonical class name.
	 * @return
	 */
	public abstract String getTypeAsString();

	/**
	 * Serializes this entity without any processing.
	 * @return
	 */
	protected JsonObject _toJson() {
		Gson gson = new Gson();
		JsonObject out = gson.toJsonTree(this).getAsJsonObject();
		out.addProperty("type", this.getTypeAsString());
		return out;
	}

	protected String _toJsonString() {
		return this._toJson().toString();
	}
	
	public abstract JsonObject toJson();
	public abstract String toJsonString();
}
