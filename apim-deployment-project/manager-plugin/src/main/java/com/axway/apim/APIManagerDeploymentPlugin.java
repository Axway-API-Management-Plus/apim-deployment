package com.axway.apim;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.security.cert.CertificateException;

import org.apache.http.ParseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.axway.apim.model.ManagerInput;

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

	@Parameter
	protected String virtualHost;

	@Parameter
	protected boolean apiConflictUpgrade;

	@Parameter
	protected boolean apiUnpublishedRemove;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		logger.info("Deployment started....");

		try {
			setup(this);
			String url = new URL(protocol, host, port, "").toString();

			ManagerInput managerInput = new ManagerInput();
			managerInput.setUrl(url);
			managerInput.setUsername(username);
			managerInput.setPassword(password);
			managerInput.setOrgName(orgName);
			managerInput.setBackendURL(backendURL);
			managerInput.setLocation(artifactLocation);
			managerInput.setVirtualHost(virtualHost);
			managerInput.setOutboundCertFolder(outboundCertDir);
			managerInput.setBackendAuthJson(backendAuthJson);
			managerInput.setApiConflictUpgrade(apiConflictUpgrade);
			managerInput.setApiUnpublishedRemove(apiUnpublishedRemove);

			apiManagerWrapper.importAPIs(managerInput);
		} catch (IOException | CertificateException | KeyManagementException | UnsupportedOperationException
				| NoSuchAlgorithmException | KeyStoreException | ParseException | URISyntaxException | ServerException
				| APIMException e) {
			logger.error("Deployment failed : {}", e);
		}

		logger.info("Deployment Complete....");
	}

}
