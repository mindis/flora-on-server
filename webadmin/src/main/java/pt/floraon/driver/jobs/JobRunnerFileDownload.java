package pt.floraon.driver.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;

import static pt.floraon.driver.Constants.dateTimeFormat;

/**
 * Runs the commands meant to be run asynchronously to a file to be downloaded.
 * Add command code in the run() method.
 * @author miguel
 *
 */
public class JobRunnerFileDownload implements JobRunner {
	private OutputStream outputStream;
	private File tmpFile;
	private boolean isClosed,hasError=false;
	private String uuid, desiredFileName, errorMessage;
	private IFloraOn driver;
	private JobFileDownload job;
	private Date date;
	
	public JobRunnerFileDownload(JobFileDownload job, String desiredFileName, IFloraOn driver) throws IOException {
		this.tmpFile = File.createTempFile("floraon_", null);
		this.outputStream = new FileOutputStream(tmpFile);
		this.isClosed = false;
		this.uuid = UUID.randomUUID().toString();
		this.desiredFileName = desiredFileName;
		this.job = job;
		this.driver = driver;
	}

	@Override
	public String getID() {
		return this.uuid;
	}

	public String getFileName() {
		return desiredFileName;
	}

	@Override
	public Boolean isReady() throws FloraOnException {
		if(hasError) throw new FloraOnException("Error occurred during processing: "+this.errorMessage);
		return isClosed;
	}

	@Override
	public String getState() throws FloraOnException {
		if(hasError) throw new FloraOnException("Error occurred during processing: "+this.errorMessage);
		return isClosed ? "Finished" : job.getState();
	}

	@Override
	public boolean isFileDownload() {
		return true;
	}

	@Override
	public String getDateSubmitted() {
		return dateTimeFormat.format(this.date);
	}

	/*
        public InputStream getInputStream() throws FloraOnException, FileNotFoundException {
            if(!isClosed) throw new FloraOnException("Job hasn't finished yet");
            if(hasError) throw new FloraOnException("Error occurred during processing: "+this.errorMessage);
            return new FileInputStream(this.tmpFile);
        }
    */
	public InputStreamReader getInputStreamReader(Charset charset) throws FloraOnException, FileNotFoundException {
		if(!isClosed) throw new FloraOnException("Job hasn't finished yet");
		if(hasError) throw new FloraOnException("Error occurred during processing: "+this.errorMessage);
		return new InputStreamReader(new FileInputStream(this.tmpFile), charset);
	}
	
	public String getFileType() {
		return FilenameUtils.getExtension(desiredFileName);
	}
	
	@Override
	public void run() {
		this.date = new Date();
		try {
			this.job.run(this.driver, this.outputStream);
			isClosed=true;
//			JobSubmitter.jobs.remove(this.uuid);
		} catch (FloraOnException | IOException e) {
			this.hasError=true;
			this.errorMessage=e.getMessage();
			try {
				this.outputStream.close();
				isClosed=true;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
