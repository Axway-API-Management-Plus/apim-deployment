package com.axway.apim;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class GatewayDeployment {

	private static Logger logger = LoggerFactory.getLogger(GatewayDeployment.class);

	@Inject
	private AxwayClient axwayClient;

	public void init(String apiGatewayURL, String username, String password)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		axwayClient.createConnection(apiGatewayURL, username, password);
	}

	public void downloadFed(String url, String groupName, String instanceName, String fedFileName)
			throws APIMException, ServerException {
		try {
			String serviceId = getServiceId(url, groupName, instanceName);
			downloadFed(url, serviceId, fedFileName);
		} catch (UnsupportedOperationException | URISyntaxException | IOException e) {
			APIMException apimException = new APIMException();
			throw apimException;
		}

	}

	public void downloadPolAndEnv(String url, String groupName, String instanceName, String polFilename,
			String envFilename) throws APIMException, ServerException {
		try {
			String serviceId = getServiceId(url, groupName, instanceName);
			downloadPol(url, serviceId, polFilename);
			downloadEnv(url, serviceId, envFilename);
		} catch (UnsupportedOperationException | URISyntaxException | IOException e) {
			APIMException apimException = new APIMException();
			throw apimException;
		}
	}

	public String uploadPolicy(String phyGroupName, String attachmentName, String apiGatewayURL, String polFilePath)
			throws IOException, URISyntaxException {

		logger.info("upload pol start");
		URI uri = new URIBuilder(apiGatewayURL)
				.setPath("/api/deployment/group/configuration/file/policy/" + phyGroupName).build();
		logger.info("Policy URL: " + uri.toString());
		HttpResponse response = axwayClient.postMultipart(uri, polFilePath, attachmentName);
		logger.info("upload pol complete");
		return processResponse(response);
	}

	public String uploadEnv(String phyGroupName, String attachmentName, String relatedPolicyArchiveID,
			String apiGatewayURL, String envFilePath) throws IOException, URISyntaxException {
		logger.info("upload env start");

		URI uri = new URIBuilder(apiGatewayURL)
				.setPath("/api/deployment/group/configuration/file/environment/" + phyGroupName)
				.setParameter("relatedPolicyArchiveID", relatedPolicyArchiveID).build();

		logger.info("Env URL :" + uri.toString());

		HttpResponse response = axwayClient.postMultipart(uri, envFilePath, attachmentName);
		logger.info("upload env complete");
		return processResponse(response);
	}

	public String uploadFed(String phyGroupName, String attachmentName, String apiGatewayURL, String fedFilePath,
			List<String> servers) throws IOException, URISyntaxException {

		logger.info("Upload Fed start");
		
		
		List<NameValuePair> serviceIds = new ArrayList<>();
		for (String server : servers) {
			NameValuePair nameValuePair = new BasicNameValuePair("serviceID", server);
			serviceIds.add(nameValuePair);
		}
		
		URI uri = new URIBuilder(apiGatewayURL).setPath("/api/deployment/group/configuration/file/" + phyGroupName).addParameters(serviceIds)
				.build();
		logger.info("Fed URL: " + uri.toString());
		HttpResponse response = axwayClient.postMultipart(uri, fedFilePath, attachmentName);
		logger.info("Upload fed complete");
		return processResponse(response);
	}

	private String getServiceId(String apiGatewayURL, String groupName, String instanceName)
			throws URISyntaxException, UnsupportedOperationException, IOException, ServerException {

		URI groupURI = null;

		String phycialgroupName = getPhycialGroupName(groupName, apiGatewayURL);
		logger.info("Physical Group name : {}", phycialgroupName);
		String phycialInstanceName = null;

		if (instanceName == null) {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/" + phycialgroupName).build();

			logger.info("Get Group Details {}", groupURI);
			phycialInstanceName = process200Response(groupURI, "Instance is not available", "$.result[0].id");

		} else {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/id/" + phycialgroupName)
					.setParameter("serviceName", groupName).build();
			logger.info("Getting Physical Instance name URI {}", groupURI);
			phycialInstanceName = process200Response(groupURI, "Instance is not available", "$.result");
		}
		logger.info("Phycial Instance name {}", phycialInstanceName);
		return phycialInstanceName;
	}

	public List<String> getServerList(String groupName, String apiGatewayURL, String instanceName,
			String phycialgroupName) throws URISyntaxException, ClientProtocolException, IOException, ServerException {

		URI groupURI = null;
		List<String> servers = new ArrayList<>();
		if (instanceName == null) {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/" + phycialgroupName).build();

			logger.info("Get Group Details {}", groupURI);
			HttpResponse httpResponse = axwayClient.getRequest(groupURI);
			StatusLine statusLine = httpResponse.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode != 200) {
				String errorMsg = "Status code " + statusCode + " Reason " + statusLine.getReasonPhrase();
				logger.error(errorMsg);
				logger.error("Server Response {}", EntityUtils.toString(httpResponse.getEntity()));
				throw new ServerException(errorMsg);
			}
			DocumentContext documentContext = JsonPath.parse(httpResponse.getEntity().getContent());
			servers = documentContext.read("$.result[*].id");

		} else {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/id/" + phycialgroupName)
					.setParameter("serviceName", groupName).build();
			logger.info("Getting Physical Instance name URI {}", groupURI);
			String phycialInstanceName = process200Response(groupURI, "Instance is not available", "$.result");
			servers.add(phycialInstanceName);
		}
		return servers;
	}

	public String getPhycialGroupName(String groupName, String apiGatewayURL)
			throws URISyntaxException, UnsupportedOperationException, IOException, ServerException {
		URI groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/groups/id")
				.setParameter("groupName", groupName).build();

		logger.info("Getting Physical Group name URI {}", groupURI);

		String phycialgroupName = process200Response(groupURI, "Group is not available", "$.result");
		logger.info("Physical Group name : {}", phycialgroupName);
		return phycialgroupName;
	}

	private void writeBase64asFile(String filename, String base64Data) throws IOException {
		Base64OutputStream base64OutputStream = null;
		try {
			base64OutputStream = new Base64OutputStream(new FileOutputStream(filename), false);
			base64OutputStream.write(base64Data.getBytes());
			base64OutputStream.flush();

		} finally {
			if (base64OutputStream != null) {
				base64OutputStream.close();
			}
		}

	}

	private void downloadPol(String apiGatewayURL, String serviceId, String filename)
			throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {

		URI downloadPolURI = new URIBuilder(apiGatewayURL)
				.setPath("/api/deployment/archive/policy/service/" + serviceId).build();
		logger.info("Downloading pol file : {}", downloadPolURI);
		String pol = process200Response(downloadPolURI, "Internal error", "$.result.data");
		writeBase64asFile(filename, pol);
		logger.info("Wrting pol file to {}", filename);

	}

	private void downloadEnv(String apiGatewayURL, String serviceId, String filename)
			throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
		URI downloadEnvURI = new URIBuilder(apiGatewayURL)
				.setPath("/api/deployment/archive/environment/service/" + serviceId).build();
		logger.info("Downloading env file : {}", downloadEnvURI);
		String env = process200Response(downloadEnvURI, "Internal error", "$.result.data");
		writeBase64asFile(filename, env);
		logger.info("Wrting env file to {}", filename);

	}

	private void downloadFed(String apiGatewayURL, String serviceId, String filename)
			throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
		URI downloadFedURI = new URIBuilder(apiGatewayURL).setPath("/api/deployment/archive/service/" + serviceId)
				.build();
		logger.info("Downloading fed file : {}", downloadFedURI);
		String fed = process200Response(downloadFedURI, "Internal error", "$.result.data");
		logger.info("Wrting fed file to {}", filename);
		writeBase64asFile(filename, fed);
	}

	private String process200Response(URI uri, String customErrorMessage, String jsonPath)
			throws UnsupportedOperationException, IOException, ServerException {

		HttpResponse httpResponse = axwayClient.getRequest(uri);
		StatusLine statusLine = httpResponse.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode != 200) {
			String errorMsg = "Status code " + statusCode + " Reason " + statusLine.getReasonPhrase();
			logger.error(errorMsg);
			logger.error("Server Response {}", EntityUtils.toString(httpResponse.getEntity()));
			throw new ServerException(errorMsg);
		}
		DocumentContext documentContext = JsonPath.parse(httpResponse.getEntity().getContent());
		String result = documentContext.read(jsonPath, String.class);
		return result;

	}

	private String processResponse(HttpResponse response) throws UnsupportedOperationException, IOException {
		HttpEntity entity = response.getEntity();
		int status = response.getStatusLine().getStatusCode();
		if (status == 200) {
			DocumentContext documentContext = JsonPath.parse(entity.getContent());
			String result = documentContext.read("$.result", String.class);
			return result;
		} else {
			logger.error("Status code " + status + "Reason " + response.getStatusLine().getReasonPhrase());
			String responseStr = EntityUtils.toString(entity);
			logger.error("Response from Server :" + responseStr);
			return null;
		}

	}

	public void deploy(String archiveId, String instanceId, String apiGatewayURL)
			throws IOException, URISyntaxException {

		// https://localhost:8090/api/router/service/instance-2/api/configuration?archiveId=480c6bbc-c2ed-4ff2-b649-7bd0b54ddd15
		logger.info("deployment of pol and env start");
		URI uri = new URIBuilder(apiGatewayURL).setPath("/api/router/service/" + instanceId + "/api/configuration")
				.setParameter("archiveId", archiveId).build();
		logger.info("Deploy URL :" + uri.toString());
		HttpResponse response = axwayClient.putRequest(null, uri, null);
		int status = response.getStatusLine().getStatusCode();
		if (status >= 300) {
			logger.error("Status code " + status + "Reason " + response.getStatusLine().getReasonPhrase());
		}
		String responseStr = EntityUtils.toString(response.getEntity());
		logger.info("Response from Server :" + responseStr);
		logger.info("deployment complete");
	}

}
