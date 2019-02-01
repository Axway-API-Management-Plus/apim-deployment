package com.axway.apim;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "apimanager-deploy", defaultPhase = LifecyclePhase.PACKAGE)
public class APIManagerDeploymentPlugin extends AbstractAPImanagerMojo implements Constants {

	

	public APIManagerDeploymentPlugin() {

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		logger.info("Deployment started....");

		

		logger.info("Deployment Complete....");
	}

}
