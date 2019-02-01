package com.axway.apim;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "apigateway-deploy", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class APIGatewayDeploymentPlugin extends AbstractGatewayMojo implements Constants {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		logger.info("Deployment started....");

		try {
			String archiveId = null;
			setup(this);
			String url = new URL(protocol, host, port, "").toString();
			gatewayDeployment.init(url, username, password);
			String phyGroupName = gatewayDeployment.getPhycialGroupName(groupName, url);
			List<String> servers = gatewayDeployment.getServerList(groupName, url, instanceName, phyGroupName);
			if (type.equalsIgnoreCase("fed")) {
				archiveId = gatewayDeployment.uploadFed(phyGroupName, FED_ATTACHMENT_NAME, url, fedFilePath, servers);
			} else if (type.equalsIgnoreCase("polenv")) {
				String relatedPolicyArchiveID = gatewayDeployment.uploadPolicy(phyGroupName, POLICY_ATTACHMENT_NAME,
						url, polFilePath);
				archiveId = gatewayDeployment.uploadEnv(phyGroupName, ENV_ATTACHMENT_NAME, relatedPolicyArchiveID, url,
						envFilePath);
			}
			logger.info("Deployment Archive id : " + archiveId);
			for (String instanceId : servers) {
				logger.info("Deploying to server " + instanceId + " starts");
				gatewayDeployment.deploy(archiveId, instanceId, url);
				logger.info("Deploying to server " + instanceId + " complete");
			}
		} catch (ServerException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
				| UnsupportedOperationException | URISyntaxException | IOException e) {
			logger.error("Unable to download the deployment package : Reason {}", e);
			System.exit(1);
		}

		logger.info("Deployment Complete....");
	}

}
