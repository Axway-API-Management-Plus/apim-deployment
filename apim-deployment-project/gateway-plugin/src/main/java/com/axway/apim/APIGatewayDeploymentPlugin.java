package com.axway.apim;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.hc.core5.http.ParseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import static com.axway.apim.Constants.*;

@Mojo(name = "apigateway-deploy", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class APIGatewayDeploymentPlugin extends AbstractGatewayMojo {

    @Override
    public void execute() throws MojoExecutionException {

        logger.info("Deployment started....");

        try {
            String archiveId = null;
            setup(this);
            String url = new URL(protocol, host, port, "").toString();
            gatewayDeployment.init(url, username, password, insecure);
            String phyGroupName = gatewayDeployment.getPhysicalGroupName(groupName, url);
            List<String> servers = gatewayDeployment.getServerLists(url, instanceName, phyGroupName);
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
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                 | UnsupportedOperationException | URISyntaxException | IOException | ParseException |
                 ServerException e) {
            logger.error("Unable to download the deployment package : Reason {}", e);
            throw new MojoExecutionException(e.getMessage());
        }

        logger.info("Deployment Complete....");
    }

}
