package com.axway.apim;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "apimanager-download", defaultPhase = LifecyclePhase.DEPLOY)
public class APIManagerDownloadPlugin extends AbstractAPImanagerMojo implements Constants {

	public APIManagerDownloadPlugin() {

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		logger.info("Download started....");

		logger.info("Download Complete....");
	}

}
