package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.results.ResultProcessor;
import pt.floraon.driver.results.SimpleTaxEntResult;

public class Suggestions extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String query = getParameterAsString("q");
		if(query == null) return;
		Integer limit = getParameterAsInteger("limit", null);

		switch(getParameterAsString("what", "taxon")) {
			case "taxon":
				ResultProcessor<SimpleTaxEntResult> rp1 = new ResultProcessor<SimpleTaxEntResult>(driver.getQueryDriver().findTaxonSuggestions(query, limit));
				response.setContentType("text/html");
				response.getWriter().println(rp1.toHTMLList("suggestions"));
				break;

			case "user":
				Iterator<User> it = driver.getQueryDriver().findUserSuggestions(query, limit);
				User u;
				response.setContentType("text/html");
				PrintWriter resp = response.getWriter();
				resp.print("<ul class=\"suggestions\">");
				while(it.hasNext()) {
					u = it.next();
					resp.print("<li data-key=\"" + u.getIDURLEncoded() + "\">");
					resp.print(u.getName());
					resp.print("</li>");
				}
				resp.print("</ul>");
				break;
		}

	}

}
