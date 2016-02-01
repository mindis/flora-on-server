package pt.floraon.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arangodb.ArangoException;
import com.arangodb.entity.EntityFactory;

import pt.floraon.driver.ArangoKey;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.GeneralNodeWrapperImpl;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.GraphUpdateResult;

@MultipartConfig
public class NodeWorker extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {
		doFloraOnPost(request, response);
	}

	@Override
	public void doFloraOnPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ArangoException, FloraOnException {
		String part;
		String rank, current, name, author, annot, id, shortName, description, from, to;
		
		if(!isAuthenticated(request)) {
			error("You must login to do this operation!");
			return;
		}
		
		ListIterator<String> partIt=this.getPathIterator(request);
		while(!(part=partIt.next()).equals("update"));
		part=partIt.next();

		switch(part) {
		case "delete":
			id=getParameter(request, "id");
			if(id==null || id.trim().length()<1) throw new FloraOnException("You must provide a document handle as id");
			success(EntityFactory.toJsonElement(graph.dbNodeWorker.deleteNode(ArangoKey.fromString(id)),false));
			return;

		case "deleteleaf":
			id=getParameter(request, "id");
			if(id==null || id.trim().length()<1) throw new FloraOnException("You must provide a document handle as id");
			success(EntityFactory.toJsonElement(graph.dbNodeWorker.deleteLeafNode(ArangoKey.fromString(id)),false));
			return;

		case "setsynonym":
			from=getParameter(request, "from");
			to=getParameter(request, "to");
			graph.dbNodeWorker.getTaxEnt(ArangoKey.fromString(from)).setSynonymOf(graph.dbNodeWorker.getTaxEntVertex(ArangoKey.fromString(to)));
			success("Ok");
			return;
			
		case "detachsynonym":
			from=getParameter(request, "from");
			to=getParameter(request, "to");
			graph.dbNodeWorker.detachSynonym(ArangoKey.fromString(from), ArangoKey.fromString(to));
			success("Ok");
			return;
			
		case "add":
			if(!partIt.hasNext()) {
				error("Choose the node type: taxent");
				return;
			}
			switch(partIt.next()) {
			case "link":
				id=request.getParameter("from");
				String id2=request.getParameter("to");
				String type=request.getParameter("type");
				if(id==null || id.trim().length()<1 || id2==null || id2.trim().length()<1 || type==null) {
					error("You must provide relationship type and two document handles 'from' and 'to'");
					return;
				}

				GeneralNodeWrapperImpl n1=graph.dbNodeWorker.getNodeWrapper(id);
				GeneralNodeWrapperImpl n2=graph.dbNodeWorker.getNodeWrapper(id2);
				if(n1==null) {
					error("Node "+id+" not found.");
					return;
				}
				if(n2==null) {
					error("Node "+id2+" not found.");
					return;
				}
				try {
					success(n1.createRelationshipTo(n2.getNode(), RelTypes.valueOf(type.toUpperCase())).toJsonObject());
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
				return;
				
			case "inferiortaxent":	// this adds a bond taxent child of the given parent and ensures it is taxonomically valid
				name=getParameter(request, "name");
				author=getParameter(request, "author");
				rank=getParameter(request, "rank");
				annot=getParameter(request, "annot");
				id=getParameter(request, "parent");
				current=getParameter(request, "current");
				success(graph.dbNodeWorker.createTaxEntChild(
						ArangoKey.fromString(id), name, author
						, TaxonRanks.getRankFromValue(Integer.parseInt(rank))
						, annot, current==null ? null : Integer.parseInt(current)==1
					).toString());
				return;

			case "territory":
				name=getParameter(request, "name");
				shortName=getParameter(request, "shortName");
				rank=getParameter(request, "rank");
				annot=getParameter(request, "annot");

				success(GraphUpdateResult.fromHandle(graph, Territory.newFromName(graph, name, shortName, rank==null ? TerritoryTypes.COUNTRY : TerritoryTypes.valueOf(rank), annot, (ArangoKey)null).getID()).toJsonObject());
				return;
				
			case "taxent":	// this only adds a free taxent
				name=getParameter(request, "name");
				author=getParameter(request, "author");
				rank=getParameter(request, "rank");
		    	success(GraphUpdateResult.fromHandle(
		    			graph
		    			, TaxEnt.newFromName(graph,name,author,TaxonRanks.getRankFromValue(Integer.parseInt(rank)),null,true).getID() ).toJsonObject()
					);
				//success(output, graph.dbNodeWorker.createTaxEntNode(name, author, TaxonRanks.getRankFromValue(Integer.parseInt(rank)), null, true).toJsonObject(), includeHeaders);
				return;
				
			case "attribute":
				name=getParameter(request, "name");
				shortName=getParameter(request, "shortName");
				description=getParameter(request, "description");
				success(graph.dbNodeWorker.createAttributeNode(name, shortName, description).toJsonObject());
				return;
			
			case "character":
				name=getParameter(request, "name");
				shortName=getParameter(request, "shortName");
				description=getParameter(request, "description");
				success(graph.dbNodeWorker.createCharacterNode(name, shortName, description).toJsonObject());
				return;
				
			default:
				error("Invalid node type");
				return;
			}
			
		case "update":
			if(!partIt.hasNext()) {
				error("Choose the node type: taxent");
				return;
			}
			switch(partIt.next()) {
			case "links":
				id=request.getParameter("id");
				current=request.getParameter("current");
				success(graph.dbNodeWorker.updateDocument(id, "current", Integer.parseInt(current)==1).toJsonObject());
				return;

			case "taxent":
				rank = getParameter(request, "rank");
				current = getParameter(request, "current");
				success(graph.dbNodeWorker.updateTaxEntNode(
						TaxEnt.newFromHandle(graph, getParameter(request, "id"))
						, getParameter(request, "name")
						, rank==null ? null : TaxonRanks.getRankFromValue(Integer.parseInt(rank))
						, current==null ? null : Integer.parseInt(current)==1
						, getParameter(request, "author"), getParameter(request, "comment")).toJsonObject()
					);
				return;
				
			case "territory":
				name=getParameter(request, "name");
				shortName=getParameter(request, "shortName");
				rank=getParameter(request, "type");
				annot=getParameter(request, "theme");
				if( (current=getParameter(request, "checklist"))==null ) current="true";
				success(graph.dbNodeWorker.updateTerritoryNode(
						ArangoKey.fromString(getParameter(request, "id"))
						, name
						, shortName
						, TerritoryTypes.valueOf(rank)
						, annot
						, Boolean.parseBoolean(current)
						).toJsonObject()
					);
				return;

			default:
				error("Invalid node type");
				return;
			}
		}
	}
}