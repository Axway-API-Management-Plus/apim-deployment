package com.axway.apim;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "apigateway-export", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class APIGatewayExportPlugin extends AbstractGatewayMojo implements Constants {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		logger.info("Download started....");
		setup(this);
		try {
			String url = new URL(protocol, host, port, "").toString();
			gatewayDeployment.init(url, username, password);
			if (type.equalsIgnoreCase("fed")) {
				gatewayDeployment.downloadFed(url, groupName, instanceName, fedFilePath);
			} else if (type.equalsIgnoreCase("polenv")) {
				gatewayDeployment.downloadPolAndEnv(url, groupName, instanceName, polFilePath, envFilePath);
			}
		} catch (APIMException | ServerException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
				| MalformedURLException e) {
			e.printStackTrace();
			logger.error("Unable to download the deployment package : Reason {}", e);
			throw new MojoFailureException(e.getMessage());
		}
		logger.info("Download Complete....");
	}

}
