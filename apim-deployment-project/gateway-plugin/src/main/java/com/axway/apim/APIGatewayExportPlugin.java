package com.axway.apim;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Mojo(name = "apigateway-export", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class APIGatewayExportPlugin extends AbstractGatewayMojo {

    @Override
    public void execute() throws MojoFailureException {

        logger.info("Download started....");
        setup(this);
        try {
            String url = new URL(protocol, host, port, "").toString();
            gatewayDeployment.init(url, username, password, insecure);
            if (type.equalsIgnoreCase("fed")) {
                gatewayDeployment.downloadFed(url, groupName, instanceName, fedFilePath);
            } else if (type.equalsIgnoreCase("polenv")) {
                gatewayDeployment.downloadPolAndEnv(url, groupName, instanceName, polFilePath, envFilePath);
            }
        } catch (ServerException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                 | MalformedURLException e) {
            logger.error("Unable to download the deployment package", e);
            throw new MojoFailureException(e.getMessage());
        }
        logger.info("Download Complete....");
    }

}
