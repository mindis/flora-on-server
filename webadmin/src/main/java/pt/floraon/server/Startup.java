package pt.floraon.server;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import pt.floraon.arangodriver.FloraOnArangoDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;

/**
 * Runs on webapp servlet startup
 * @author miguel
 *
 */
public class Startup implements ServletContextListener {
	//public static FloraOnInt FloraOnDriver;

	public void contextInitialized(ServletContextEvent sce) {
		IFloraOn FloraOnDriver;
		File dir = new File(sce.getServletContext().getRealPath("/")).getParentFile();
		Properties properties = new Properties();
		InputStream propStream;

		Locale.setDefault(Locale.forLanguageTag("pt"));

		try {
			propStream = new FileInputStream(new File(dir.getAbsolutePath() + "/floraon.properties"));
			properties.load(propStream);
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("ERROR: "+e.getMessage());
			FloraOnDriver = new FloraOnArangoDriver(e.getMessage());
			sce.getServletContext().setAttribute("driver", FloraOnDriver);
			return;
		}

		try {
			FloraOnDriver = new FloraOnArangoDriver("flora", properties);
			FloraOnDriver.getRedListData().initializeRedListData(properties);
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("ERROR: "+e.getMessage());
			FloraOnDriver = new FloraOnArangoDriver(e.getMessage());
		}
		sce.getServletContext().setAttribute("driver", FloraOnDriver);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
