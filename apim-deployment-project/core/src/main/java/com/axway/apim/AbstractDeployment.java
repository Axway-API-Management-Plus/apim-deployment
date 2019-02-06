package com.axway.apim;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public abstract class AbstractDeployment {
	private static Logger logger = LoggerFactory.getLogger(AbstractDeployment.class);

	@Inject
	protected AxwayClient axwayClient;

	public void init(String apiGatewayURL, String username, String password)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		axwayClient.createConnection(apiGatewayURL, username, password);
	}

	protected String process200Response(HttpResponse response, String customErrorMessage, String jsonPath)
			throws UnsupportedOperationException, IOException, ServerException {
		try {
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode != 200) {
				String errorMsg = customErrorMessage + " Status code " + statusCode + " Reason "
						+ statusLine.getReasonPhrase();
				logger.error(errorMsg);
				logger.error("Server Response {}", EntityUtils.toString(response.getEntity()));
				throw new ServerException(errorMsg);
			}
			DocumentContext documentContext = JsonPath.parse(response.getEntity().getContent());
			try {
				return documentContext.read(jsonPath, String.class);
			} catch (PathNotFoundException e) {
				throw new ServerException(customErrorMessage,e);
			}
		} finally {
			HttpClientUtils.closeQuietly(response);
		}

	}

}
