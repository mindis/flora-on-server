package pt.floraon.driver.jobs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;

import pt.floraon.driver.FloraOnException;
import pt.floraon.server.FloraOnServlet;

@WebServlet("/job/*")
public class JobServlet extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		ListIterator<String> partIt=this.getPathIterator();
		while(!partIt.next().equals("job"));
		if(!partIt.hasNext()) {
			success(new Gson().toJsonTree(JobSubmitter.getJobList()));
			return;
		}

		JobRunner job = JobSubmitter.getJob(partIt.next());
		if(job == null) throw new FloraOnException("Job not found");
		if(getParameterAsString("query") != null || !job.isFileDownload()) {
			JsonObject resp = new JsonObject();
			resp.addProperty("ready", job.isReady());
			resp.addProperty("msg", job.getState());
			success(resp);
			return;
		}

		if(job.isFileDownload()) {
			JobRunnerFileDownload jobFD = (JobRunnerFileDownload) job;
			if(!job.isReady()) {
				error("Job is not ready");
				return;
			}
			InputStreamReader jobInput = jobFD.getInputStreamReader(StandardCharsets.UTF_8);
			switch (jobFD.getFileType().toLowerCase()) {
				case "html":
				case "htm":
					response.setContentType("text/html; charset=utf-8");
					break;
				case "csv":
					//response.setContentType("text/csv; charset=Windows-1252");
					response.setContentType("text/csv; charset=utf-8");
					response.addHeader("Content-Disposition", "attachment;Filename=\"" + jobFD.getFileName() + "\"");
					break;
			}
			response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
			//IOUtils.copy(jobInput, response.getOutputStream());
			IOUtils.copy(jobInput, response.getWriter());
		} else {

		}
	}
}
