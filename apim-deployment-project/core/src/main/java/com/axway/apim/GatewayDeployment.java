package com.axway.apim;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GatewayDeployment extends AbstractDeployment {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayDeployment.class);
    public static final String JSON_PATH = "$.result";

    private final AxwayClient axwayClient;

    public GatewayDeployment(AxwayClient axwayClient) {
        this.axwayClient = axwayClient;
    }

    public void downloadFed(String url, String groupName, String instanceName, String fedFileName) throws ServerException {
        try {
            String serviceId = getServiceId(url, groupName, instanceName);
            downloadFed(url, serviceId, fedFileName);
        } catch (UnsupportedOperationException | URISyntaxException | IOException e) {
            throw new ServerException(e);
        }

    }

    public void downloadPolAndEnv(String url, String groupName, String instanceName, String polFilename, String envFilename) throws ServerException {
        try {
            String serviceId = getServiceId(url, groupName, instanceName);
            downloadPol(url, serviceId, polFilename);
            downloadEnv(url, serviceId, envFilename);
        } catch (UnsupportedOperationException | URISyntaxException | IOException e) {
            throw new ServerException(e);
        }
    }

    public String uploadPolicy(String phyGroupName, String attachmentName, String apiGatewayURL, String polFilePath) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {

        LOGGER.info("upload pol start");
        URI uri = new URIBuilder(apiGatewayURL).setPath("/api/deployment/group/configuration/file/policy/" + phyGroupName).build();
        LOGGER.info("Upload Policy URL: {}", uri);
        HttpResponse response = axwayClient.postMultipartFile(uri, polFilePath, attachmentName);
        LOGGER.info("upload pol complete");
        return process200Response(response, "Upload pol failed", JSON_PATH);

    }

    public String uploadEnv(String phyGroupName, String attachmentName, String relatedPolicyArchiveID, String apiGatewayURL, String envFilePath) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
        LOGGER.info("upload env start");
        URI uri = new URIBuilder(apiGatewayURL).setPath("/api/deployment/group/configuration/file/environment/" + phyGroupName).setParameter("relatedPolicyArchiveID", relatedPolicyArchiveID).build();
        LOGGER.info("Env URL : {}", uri);
        HttpResponse response = axwayClient.postMultipartFile(uri, envFilePath, attachmentName);
        LOGGER.info("upload env complete");
        return process200Response(response, "Upload env failed", JSON_PATH);
    }

    public String uploadFed(String phyGroupName, String attachmentName, String apiGatewayURL, String fedFilePath, List<String> servers) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {

        LOGGER.info("Upload Fed start");
        List<NameValuePair> serviceIds = new ArrayList<>();
        for (String server : servers) {
            NameValuePair nameValuePair = new BasicNameValuePair("serviceID", server);
            serviceIds.add(nameValuePair);
        }

        URI uri = new URIBuilder(apiGatewayURL).setPath("/api/deployment/group/configuration/file/" + phyGroupName).addParameters(serviceIds).build();
        LOGGER.info("Fed URL: {}", uri);
        HttpResponse response = axwayClient.postMultipartFile(uri, fedFilePath, attachmentName);
        LOGGER.info("Upload fed complete");
        return process200Response(response, "Upload fed failed", JSON_PATH);
    }

    private String getServiceId(String apiGatewayURL, String groupName, String instanceName) throws URISyntaxException, UnsupportedOperationException, IOException, ServerException {

        URI groupURI;
        String physicalGroupName = getPhysicalGroupName(groupName, apiGatewayURL);
        LOGGER.info("Physical Group name : {}", physicalGroupName);
        String physicalInstanceName;

        if (instanceName == null) {
            groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/" + physicalGroupName).build();
            LOGGER.info("Get Group Details {}", groupURI);
            HttpResponse httpResponse = axwayClient.get(groupURI);
            physicalInstanceName = process200Response(httpResponse, "Instance is not available", "$.result[0].id");

        } else {
            groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/id/" + physicalGroupName).setParameter("serviceName", groupName).build();
            LOGGER.info("Getting Physical Instance name URI {}", groupURI);
            HttpResponse httpResponse = axwayClient.get(groupURI);
            physicalInstanceName = process200Response(httpResponse, "Instance is not available", JSON_PATH);
        }
        LOGGER.info("Physical Instance name {}", physicalInstanceName);
        return physicalInstanceName;
    }

    public List<String> getServerLists(String apiGatewayURL, String instanceName, String physicalGroupName) throws URISyntaxException, IOException, ServerException, ParseException {

        URI groupURI;
        List<String> servers = new ArrayList<>();
        if (instanceName == null) {
            groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/" + physicalGroupName).build();
            LOGGER.info("Get Group Details {}", groupURI);

            HttpResponse httpResponse = axwayClient.get(groupURI);
            int statusCode = httpResponse.getStatus();
            if (statusCode != 200) {
                LOGGER.error("Status code {} Reason {}", statusCode, httpResponse.getReasonText());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Server Response {}", httpResponse.getBody());
                }
                throw new ServerException(httpResponse.getReasonText());
            }
            DocumentContext documentContext = JsonPath.parse(httpResponse.getBody());
            servers = documentContext.read("$.result[*].id");

        } else {
            groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/services/id/" + physicalGroupName).setParameter("serviceName", instanceName).build();
            LOGGER.info("Getting Physical Instance name URI {}", groupURI);
            HttpResponse httpResponse = axwayClient.get(groupURI);
            String physicalInstanceName = process200Response(httpResponse, "Instance is not available", JSON_PATH);
            servers.add(physicalInstanceName);
        }
        return servers;
    }

    public String getPhysicalGroupName(String groupName, String apiGatewayURL) throws URISyntaxException, UnsupportedOperationException, IOException, ServerException {
        URI groupURI = new URIBuilder(apiGatewayURL).setPath("/api/topology/groups/id").setParameter("groupName", groupName).build();
        LOGGER.info("Getting Physical Group name URI {}", groupURI);
        HttpResponse httpResponse = axwayClient.get(groupURI);
        return process200Response(httpResponse, "Group is not available", JSON_PATH);
    }

    private void writeBase64asFile(String filename, String base64Data) throws IOException {
        try (Base64OutputStream base64OutputStream = new Base64OutputStream(new FileOutputStream(filename), false)) {
            base64OutputStream.write(base64Data.getBytes());
        }
    }

    private void downloadPol(String apiGatewayURL, String serviceId, String filename) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {

        URI downloadPolURI = new URIBuilder(apiGatewayURL).setPath("/api/deployment/archive/policy/service/" + serviceId).build();
        LOGGER.info("Downloading pol file : {}", downloadPolURI);
        HttpResponse httpResponse = axwayClient.get(downloadPolURI);
        String pol = process200Response(httpResponse, "Internal error", "$.result.data");
        writeBase64asFile(filename, pol);
        LOGGER.info("Writing pol file to {}", filename);

    }

    private void downloadEnv(String apiGatewayURL, String serviceId, String filename) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
        URI downloadEnvURI = new URIBuilder(apiGatewayURL).setPath("/api/deployment/archive/environment/service/" + serviceId).build();
        LOGGER.info("Downloading env file : {}", downloadEnvURI);
        HttpResponse httpResponse = axwayClient.get(downloadEnvURI);
        String env = process200Response(httpResponse, "Internal error", "$.result.data");
        writeBase64asFile(filename, env);
        LOGGER.info("Writing env file to {}", filename);

    }

    private void downloadFed(String apiGatewayURL, String serviceId, String filename) throws IOException, URISyntaxException, UnsupportedOperationException, ServerException {
        URI downloadFedURI = new URIBuilder(apiGatewayURL).setPath("/api/deployment/archive/service/" + serviceId).build();
        LOGGER.info("Downloading fed file : {}", downloadFedURI);
        HttpResponse httpResponse = axwayClient.get(downloadFedURI);
        String fed = process200Response(httpResponse, "Internal error", "$.result.data");
        LOGGER.info("Writing fed file to {}", filename);
        writeBase64asFile(filename, fed);
    }


    public void deploy(String archiveId, String instanceId, String apiGatewayURL) throws IOException, URISyntaxException, ParseException, ServerException {

        // https://localhost:8090/api/router/service/instance-2/api/configuration?archiveId=480c6bbc-c2ed-4ff2-b649-7bd0b54ddd15
        LOGGER.info("Starting deployment");
        URI uri = new URIBuilder(apiGatewayURL).setPath("/api/router/service/" + instanceId + "/api/configuration").setParameter("archiveId", archiveId).build();
        LOGGER.info("Deploy URL : {}", uri);
        HttpResponse response = axwayClient.put(uri, null, null);
        int status = response.getStatus();
        String reason = response.getReasonText();
        if (status >= 300) {
            LOGGER.error("Status code {} Reason {} ", status, reason);
            LOGGER.info("Response from Server : {}", response.getBody());
            throw new ServerException(reason);
        }
        LOGGER.info("deployment completed");
    }
}
