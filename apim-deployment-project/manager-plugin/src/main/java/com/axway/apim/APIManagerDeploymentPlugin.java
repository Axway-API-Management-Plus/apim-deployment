package com.axway.apim;

import java.io.IOException;
import java.net.URL;

import javax.security.cert.CertificateException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "apimanager-deploy", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class APIManagerDeploymentPlugin extends AbstractAPImanagerMojo {

	@Parameter
	protected String orgName;

	@Parameter
	protected String backendAuthJson;

	@Parameter
	protected String backendURL;

	@Parameter
	protected String outboundCertDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		logger.info("Deployment started....");

		try {
			String url = new URL(protocol, host, port, "").toString();
			apiManagerWrapper.importAPIs(url, username, password, artifactLocation, orgName, backendURL,
					outboundCertDir, backendAuthJson);
		} catch (IOException | CertificateException e) {
			logger.error("Deployment failed : {}", e);
		}

		logger.info("Deployment Complete....");
	}

}
