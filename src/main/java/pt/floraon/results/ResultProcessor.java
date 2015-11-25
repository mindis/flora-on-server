package pt.floraon.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.entity.EntityFactory;
import com.google.gson.JsonElement;

import dnl.utils.text.table.TextTable;
/**
 * Utility class to handle query responses
 * @author miguel
 *
 * @param <T> A class implementing {@link ResultItem}
 */
public class ResultProcessor<T extends ResultItem> {
	 /*private final Class<T> type;

	public ResultProcessor(Class<T> type) {
		this.type = type;
	}*/

	public String toCSVTable(Iterator<T> it) {
		StringBuilder sb=new StringBuilder(); 
		T tmp;
    	while (it.hasNext()) {
    		tmp=it.next();
    		sb.append(tmp.toCSVLine()).append("\n");
    	}
    	return sb.toString();
	}

	public String toHTMLTable(Iterator<T> it) {
		StringBuilder sb=new StringBuilder(); 
		T tmp;
		sb.append("<table>");
    	while (it.hasNext()) {
    		tmp=it.next();
    		sb.append(tmp.toHTMLLine());
    	}
    	sb.append("</table>");
    	return sb.toString();
	}
	
	public TextTable toPrettyTable(Iterator<T> it) {
		List<String[]> tmp=new ArrayList<String[]>();
    	while (it.hasNext()) {
    		tmp.add(it.next().toStringArray());
    	}
    	return new TextTable(new String[] {"Count","Key","RelTypes","Name","Matches"},tmp.toArray(new String[0][0]));
	}

    public String toJSONString(Iterator<T> it) {
    	List<T> out=new ArrayList<T>();
		while(it.hasNext()) {
			out.add(it.next());
		}
    	return EntityFactory.toJsonString(out);
    }

    public JsonElement toJSONElement(Iterator<T> it) {
    	List<T> out=new ArrayList<T>();
		while(it.hasNext()) {
			out.add(it.next());
		}
    	return EntityFactory.toJsonElement(out, false);
    }

	public String toDWC() {
		// TODO export DWC string
		return null;
	}
}
