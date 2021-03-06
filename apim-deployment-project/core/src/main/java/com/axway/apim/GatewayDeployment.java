package com.axway.apim;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class GatewayDeployment extends AbstractDeployment{

	private static Logger logger = LoggerFactory.getLogger(GatewayDeployment.class);

	public void downloadFed(String url, String groupName, String instanceName, String fedFileName)
			throws APIMException, ServerException {
		try {
			String serviceId = getServiceId(url, groupName, instanceName);
			downloadFed(url, serviceId, fedFileName);
		} catch (UnsupportedOperationException | URISyntaxException | IOException e) {
			APIMException apimException = new APIMException(e);
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
			throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {

		logger.info("upload pol start");
		URI uri = new URIBuilder(apiGatewayURL)
				.setPath("/api/deployment/group/configuration/file/policy/" + phyGroupName).build();
		logger.info("Upload Policy URL: " + uri.toString());
		HttpResponse response = axwayClient.postMultipartFile(uri, polFilePath, attachmentName);
		logger.info("upload pol complete");
		return process200Response(response,"Upload pol failed","$.result");
	
	}

	public String uploadEnv(String phyGroupName, String attachmentName, String relatedPolicyArchiveID,
			String apiGatewayURL, String envFilePath) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
		logger.info("upload env start");

		URI uri = new URIBuilder(apiGatewayURL)
				.setPath("/api/deployment/group/configuration/file/environment/" + phyGroupName)
				.setParameter("relatedPolicyArchiveID", relatedPolicyArchiveID).build();

		logger.info("Env URL :" + uri.toString());

		HttpResponse response = axwayClient.postMultipartFile(uri, envFilePath, attachmentName);
		logger.info("upload env complete");
		return process200Response(response,"Upload env failed","$.result");
	}

	public String uploadFed(String phyGroupName, String attachmentName, String apiGatewayURL, String fedFilePath,
			List<String> servers) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {

		logger.info("Upload Fed start");

		List<NameValuePair> serviceIds = new ArrayList<>();
		for (String server : servers) {
			NameValuePair nameValuePair = new BasicNameValuePair("serviceID", server);
			serviceIds.add(nameValuePair);
		}

		URI uri = new URIBuilder(apiGatewayURL).setPath("/api/deployment/group/configuration/file/" + phyGroupName)
				.addParameters(serviceIds).build();
		logger.info("Fed URL: " + uri.toString());
		HttpResponse response = axwayClient.postMultipartFile(uri, fedFilePath, attachmentName);
		logger.info("Upload fed complete");
		return process200Response(response,"Upload fed failed","$.result");
	}

	private String getServiceId(String apiGatewayURL, String groupName, String instanceName)
			throws URISyntaxException, UnsupportedOperationException, IOException, ServerException {

		URI groupURI = null;

		String physicalGroupName = getPhycialGroupName(groupName, apiGatewayURL);
		logger.info("Physical Group name : {}", physicalGroupName);
		String physicalInstanceName = null;

		if (instanceName == null) {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/" + physicalGroupName).build();
			logger.info("Get Group Details {}", groupURI);
			HttpResponse httpResponse = axwayClient.get(groupURI);
			physicalInstanceName = process200Response(httpResponse, "Instance is not available", "$.result[0].id");

		} else {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/id/" + physicalGroupName)
					.setParameter("serviceName", groupName).build();
			logger.info("Getting Physical Instance name URI {}", groupURI);
			HttpResponse httpResponse = axwayClient.get(groupURI);
			physicalInstanceName = process200Response(httpResponse, "Instance is not available", "$.result");
		}
		logger.info("Physical Instance name {}", physicalInstanceName);
		return physicalInstanceName;
	}

	public List<String> getServerList(String groupName, String apiGatewayURL, String instanceName,
			String phycialgroupName) throws URISyntaxException, ClientProtocolException, IOException, ServerException {

		URI groupURI = null;
		List<String> servers = new ArrayList<>();
		if (instanceName == null) {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/" + phycialgroupName).build();

			logger.info("Get Group Details {}", groupURI);
			HttpResponse httpResponse = axwayClient.get(groupURI);
			try {
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
			} finally {
				HttpClientUtils.closeQuietly(httpResponse);
			}

		} else {
			groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/id/" + phycialgroupName)
					.setParameter("serviceName", instanceName).build();
			logger.info("Getting Physical Instance name URI {}", groupURI);
			HttpResponse httpResponse = axwayClient.get(groupURI);
			String physicalInstanceName = process200Response(httpResponse, "Instance is not available", "$.result");
			servers.add(physicalInstanceName);
		}
		return servers;
	}

	public String getPhycialGroupName(String groupName, String apiGatewayURL)
			throws URISyntaxException, UnsupportedOperationException, IOException, ServerException {
		URI groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/groups/id")
				.setParameter("groupName", groupName).build();

		logger.info("Getting Physical Group name URI {}", groupURI);
		HttpResponse httpResponse = axwayClient.get(groupURI);
		String physicalGroupName = process200Response(httpResponse, "Group is not available", "$.result");
		return physicalGroupName;
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
		HttpResponse httpResponse = axwayClient.get(downloadPolURI);
		String pol = process200Response(httpResponse, "Internal error", "$.result.data");
		writeBase64asFile(filename, pol);
		logger.info("Writing pol file to {}", filename);

	}

	private void downloadEnv(String apiGatewayURL, String serviceId, String filename)
			throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
		URI downloadEnvURI = new URIBuilder(apiGatewayURL)
				.setPath("/api/deployment/archive/environment/service/" + serviceId).build();
		logger.info("Downloading env file : {}", downloadEnvURI);
		HttpResponse httpResponse = axwayClient.get(downloadEnvURI);
		String env = process200Response(httpResponse, "Internal error", "$.result.data");
		writeBase64asFile(filename, env);
		logger.info("Writing env file to {}", filename);

	}

	private void downloadFed(String apiGatewayURL, String serviceId, String filename)
			throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
		URI downloadFedURI = new URIBuilder(apiGatewayURL).setPath("/api/deployment/archive/service/" + serviceId)
				.build();
		logger.info("Downloading fed file : {}", downloadFedURI);
		HttpResponse httpResponse = axwayClient.get(downloadFedURI);
		String fed = process200Response(httpResponse, "Internal error", "$.result.data");
		logger.info("Writing fed file to {}", filename);
		writeBase64asFile(filename, fed);
	}

	

//	private String processResponse(HttpResponse response) throws UnsupportedOperationException, IOException {
//		HttpEntity entity = response.getEntity();
//		int status = response.getStatusLine().getStatusCode();
//		if (status == 200) {
//			DocumentContext documentContext = JsonPath.parse(entity.getContent());
//			String result = documentContext.read("$.result", String.class);
//			return result;
//		} else {
//			logger.error("Status code " + status + "Reason " + response.getStatusLine().getReasonPhrase());
//			String responseStr = EntityUtils.toString(entity);
//			logger.error("Response from Server :" + responseStr);
//			return null;
//		}
//
//	}

	public void deploy(String archiveId, String instanceId, String apiGatewayURL)
			throws IOException, URISyntaxException, APIMException {

		// https://localhost:8090/api/router/service/instance-2/api/configuration?archiveId=480c6bbc-c2ed-4ff2-b649-7bd0b54ddd15
		logger.info("Deployment start");
		URI uri = new URIBuilder(apiGatewayURL).setPath("/api/router/service/" + instanceId + "/api/configuration")
				.setParameter("archiveId", archiveId).build();
		logger.info("Deploy URL :" + uri.toString());
		HttpResponse response = axwayClient.put(uri,null, null);
		try{
			int status = response.getStatusLine().getStatusCode();
			String reason = response.getStatusLine().getReasonPhrase();
			if (status >= 300) {
				logger.error("Status code {} Reason {} ", status, reason);
				throw new APIMException(reason);
			}
			String responseStr = EntityUtils.toString(response.getEntity());
			logger.info("Response from Server :" + responseStr);
			logger.info("deployment complete");
		}finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

}
