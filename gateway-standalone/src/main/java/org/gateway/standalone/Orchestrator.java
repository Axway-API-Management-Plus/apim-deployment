package org.gateway.standalone;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.APIMException;
import com.axway.apim.GatewayDeployment;
import com.axway.apim.ServerException;
import com.google.inject.Inject;

public class Orchestrator {

	private static Logger logger = LoggerFactory.getLogger(Orchestrator.class);

	private static String POLICY_ATTACHMENT_NAME = "policyFile";

	private static String ENV_ATTACHMENT_NAME = "environmentFile";

	@Inject
	private GatewayDeployment gatewayDeployment;

	public Orchestrator() {
		// TODO Auto-generated constructor stub
	}

	public void download(String url, String username, String password, String groupName, String instanceName,
			String type, String fedFileName, String polFileName, String envFileName) {
		try {
			gatewayDeployment.init(url, username, password);
			if (type.equalsIgnoreCase("fed")) {
				gatewayDeployment.downloadFed(url, groupName, instanceName, fedFileName);
			} else if (type.equalsIgnoreCase("polenv")) {
				gatewayDeployment.downloadPolAndEnv(url, groupName, instanceName, polFileName, envFileName);
			}
		} catch (APIMException | ServerException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException e) {
			logger.error("Unable to download the deployment package : Reason {}", e);
			System.exit(1);
		}

	}

	public void deploy(String url, String username, String password, String groupName, String instanceName, String type,
			String fedFileName, String polFileName, String envFileName) {
		try {
			gatewayDeployment.init(url, username, password);
			if (type.equalsIgnoreCase("fed")) {
				// gatewayDeployment.downloadFed(url, username, password,
				// groupName, instanceName, fedFileName);
			} else if (type.equalsIgnoreCase("polenv")) {
				String phyGroupName = gatewayDeployment.getPhycialGroupName(groupName, url);
				String relatedPolicyArchiveID = gatewayDeployment.uploadPolicy(phyGroupName, POLICY_ATTACHMENT_NAME,
						url, polFileName);
				String archiveId = gatewayDeployment.uploadEnv(phyGroupName, ENV_ATTACHMENT_NAME,
						relatedPolicyArchiveID, url, envFileName);
				
				List<String> servers = gatewayDeployment.getServerList(groupName, url, instanceName, phyGroupName);
				
				for (String instanceId : servers) {
					logger.info("Deploying to server {} starts",instanceId);
					gatewayDeployment.deploy(archiveId, instanceId, url);
					logger.info("Deploying to server {} complete",instanceId);
				}
				
				
			}
		} catch (ServerException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnsupportedOperationException | URISyntaxException | IOException e) {
			logger.error("Unable to download the deployment package : Reason {}", e);
			System.exit(1);
		}

	}

}
