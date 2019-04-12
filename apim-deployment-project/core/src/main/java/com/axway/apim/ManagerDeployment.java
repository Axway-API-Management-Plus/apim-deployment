package com.axway.apim;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.model.FrontendAPI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ManagerDeployment extends AbstractDeployment implements Constants {

	private static Logger logger = LoggerFactory.getLogger(ManagerDeployment.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	public ManagerDeployment() {
		// TODO Auto-generated constructor stub
	}

	public FrontendAPI getAPIById(String url, String apiId)
			throws URISyntaxException, ClientProtocolException, IOException {

		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies/" + apiId).build();
		HttpResponse httpResponse = axwayClient.get(uri);
		try {
			FrontendAPI frontendAPI = objectMapper.readValue(httpResponse.getEntity().getContent(), FrontendAPI.class);
			return frontendAPI;
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

	public List<FrontendAPI> getAPIByName(String url, String name)
			throws URISyntaxException, ClientProtocolException, IOException {

		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies").setParameter("field", "name")
				.setParameter("op", "eq").setParameter("value", name).build();
		HttpResponse httpResponse = axwayClient.get(uri);
		try {
			List<FrontendAPI> frontendAPIS = objectMapper.readValue(httpResponse.getEntity().getContent(),
					new TypeReference<List<FrontendAPI>>() {
					});
			return frontendAPIS;
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

	public void unpublishAPI(String url, String frondEndApiId)
			throws URISyntaxException, ClientProtocolException, IOException {

		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies/" + frondEndApiId +"/unpublish").build();
		HttpResponse httpResponse = axwayClient.post(uri, null, null);
		try {
			logger.info("API unpublished Response code {}", httpResponse.getStatusLine().getStatusCode());
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

	public void deleteFrondendAPI(String url, String frondEndApiId)
			throws URISyntaxException, ClientProtocolException, IOException {

		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies/" + frondEndApiId).build();
		logger.info("Deleting FrontEnd API");
		HttpResponse httpResponse = axwayClient.delete(uri);
		try {
			logger.info("Delete Front end API Response Code : {} ", httpResponse.getStatusLine().getStatusCode());
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

	public void deleteBackendAPI(String url, String backendId)
			throws URISyntaxException, ClientProtocolException, IOException {
		logger.info("Deleting BackeEnd API");
		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/apirepo/" + backendId).build();
		HttpResponse httpResponse = axwayClient.delete(uri);
		try {
			logger.info("Delete Back end API Response Code : {}", httpResponse.getStatusLine().getStatusCode());
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

	public String getOrganizationId(String url, String orgName) throws URISyntaxException, ClientProtocolException,
			IOException, UnsupportedOperationException, ServerException {

		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/organizations/").setParameter("field", "name")
				.setParameter("op", "eq").setParameter("value", orgName).build();
		logger.info("Get Organization Detail URL : {}", uri);
		HttpResponse httpResponse = axwayClient.get(uri);
		String orgId = process200Response(httpResponse, "Inalid Organization : " + orgName, "$.[0].id");
		return orgId;
	}

	
	public void publishAPI(String url, String id, String name, String virtualHost)
			throws URISyntaxException, ClientProtocolException, IOException, APIMException {
		
		logger.info("Publishing API");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", name));
		
		if(virtualHost != null){
			logger.info("Setting up virtual host {} for API {}",virtualHost,name);
			params.add(new BasicNameValuePair("vhost", virtualHost));
		}
		
		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies/" + id + "/publish").build();
		HttpResponse response = axwayClient.postForm(uri, params);
		handleResponse(response);
	}

	public void upgradeAPI(String url, String apiId, String upgradeId, boolean deprecate, boolean retire)
			throws URISyntaxException, ClientProtocolException, IOException, APIMException {

		logger.info("Upgrade API");
		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies/upgrade/" + apiId).build();
		List<NameValuePair> nameValuePairs = new ArrayList<>();

		NameValuePair upgradeApiIdParam = new BasicNameValuePair("upgradeApiId", upgradeId);
		NameValuePair deprecateParam = new BasicNameValuePair("deprecate", "false");
		NameValuePair retireParam = new BasicNameValuePair("retire", "false");

		nameValuePairs.add(upgradeApiIdParam);
		nameValuePairs.add(deprecateParam);
		nameValuePairs.add(retireParam);

		HttpResponse response = axwayClient.postForm(uri, nameValuePairs);
		handleResponse(response);
		logger.info("Upgrade API complete");
	}

	private void handleResponse(HttpResponse response) throws APIMException, ParseException, IOException {
		try {

			int status = response.getStatusLine().getStatusCode();
			if (status >= 400) {
				logger.error("Error message from APIGateway : {}", EntityUtils.toString(response.getEntity()));
				throw new APIMException(response.getStatusLine().getReasonPhrase());
			}
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	public String exportAPI(String url, List<FrontendAPI> ids, String filename)
			throws URISyntaxException, ClientProtocolException, IOException, APIMException {
		int status = 0;

		List<NameValuePair> params = new ArrayList<>();
		for (FrontendAPI frontendAPI : ids) {
			NameValuePair nameValuePair = new BasicNameValuePair("id", frontendAPI.getId());
			params.add(nameValuePair);
		}

		params.add(new BasicNameValuePair("filename", filename));
		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies/" + "export").build();
		HttpResponse httpResponse = axwayClient.postForm(uri, params);
		try {
			status = httpResponse.getStatusLine().getStatusCode();

			if (status >= 400) {
				throw new APIMException("Unable to find API");
			}
			String locationURI = httpResponse.getFirstHeader("Location").getValue();
			logger.info("Export URI : {}", locationURI);
			uri = new URIBuilder(locationURI).build();
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}

		httpResponse = axwayClient.get(uri);
		try {
			status = httpResponse.getStatusLine().getStatusCode();
			if (status >= 400) {
				throw new APIMException("Unable to find API");
			}

			String apiContent = EntityUtils.toString(httpResponse.getEntity());
			return apiContent;
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

	public void importAPI(String url, String orgId, String api)
			throws URISyntaxException, ClientProtocolException, IOException, ParseException, APIMException {
		
		logger.info("Importing API");

		URI uri = new URIBuilder(url).setPath(API_BASEPATH + "/proxies/" + "import").build();

		Map<String, String> params = new HashMap<>();
		params.put("organizationId", orgId);
		HttpResponse httpResponse = axwayClient.postMultipart(uri, api, "file", params);
		handleResponse(httpResponse);

	}

}
