package com.axway.apim;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "apimanager-export", defaultPhase = LifecyclePhase.DEPLOY)
public class APIManagerExportPlugin extends AbstractAPImanagerMojo {

	@Parameter
	protected String apiName;

	@Parameter
	protected String apiVersion;

	public APIManagerExportPlugin() {

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		logger.info("Export started....");
		try {
			setup(this);
			List<String> version = new ArrayList<>();
			version.add(apiVersion);
			String url = new URL(protocol, host, port, "").toString();
			apiManagerWrapper.exportAPIs(url, username, password, artifactLocation, apiName, version);
		} catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
				| URISyntaxException | APIMException e) {
			logger.error("Export failed : {}", e);
		}
		logger.info("Export Complete....");
	}

}
