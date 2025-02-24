package com.axway.apim;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.jar.Manifest;

import static com.axway.apim.Constants.*;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Orchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Orchestrator.class);
    private final GatewayDeployment gatewayDeployment;

    public Orchestrator(GatewayDeployment gatewayDeployment) {
        this.gatewayDeployment = gatewayDeployment;
    }

    public void download(String url, String username, String password, String groupName, String instanceName,
                         String type, String fedFileName, String polFileName, String envFileName, boolean insecure) {
        try {
            gatewayDeployment.init(url, username, password, insecure);
            if (type.equalsIgnoreCase("fed")) {
                gatewayDeployment.downloadFed(url, groupName, instanceName, fedFileName);
            } else if (type.equalsIgnoreCase("polenv")) {
                gatewayDeployment.downloadPolAndEnv(url, groupName, instanceName, polFileName, envFileName);
            }
        } catch (ServerException | KeyManagementException | NoSuchAlgorithmException
                 | KeyStoreException e) {
            LOGGER.error("Unable to download the deployment package", e);
            System.exit(1);
        }

    }

    public void deploy(String url, String username, String password, String groupName, String instanceName, String type,
                       String fedFileName, String polFileName, String envFileName, boolean insecure, String fedDir) {
        try {
            String archiveId = null;
            gatewayDeployment.init(url, username, password, insecure);
            String phyGroupName = gatewayDeployment.getPhysicalGroupName(groupName, url);
            List<String> servers = gatewayDeployment.getServerLists(url, instanceName, phyGroupName);
            if (type.equalsIgnoreCase("fed")) {

                if (fedFileName == null && fedDir != null) {
                    // Handle fed project
                    Archive archive = new Archive();
                    String uuidStr = UUID.randomUUID().toString();
                    fedFileName = uuidStr + ".fed";
                    Manifest manifest = archive.createManifest(uuidStr);
                    archive.createFed(uuidStr, manifest, fedFileName, new File(fedDir));
                }
                archiveId = gatewayDeployment.uploadFed(phyGroupName, FED_ATTACHMENT_NAME, url, fedFileName,
                    servers);

            } else if (type.equalsIgnoreCase("polenv")) {

                String relatedPolicyArchiveID = gatewayDeployment.uploadPolicy(phyGroupName, POLICY_ATTACHMENT_NAME,
                    url, polFileName);
                archiveId = gatewayDeployment.uploadEnv(phyGroupName, ENV_ATTACHMENT_NAME, relatedPolicyArchiveID, url,
                    envFileName);
            }
            LOGGER.info("Deployment Archive id : {}", archiveId);
            for (String instanceId : servers) {
                LOGGER.info("Deploying to server {} starts", instanceId);
                gatewayDeployment.deploy(archiveId, instanceId, url);
                LOGGER.info("Deploying to server {} complete", instanceId);
            }
        } catch (ServerException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                 | UnsupportedOperationException | URISyntaxException | IOException | ParseException e) {
            LOGGER.error("Unable to download the deployment package", e);
            System.exit(1);
        }
    }
}
