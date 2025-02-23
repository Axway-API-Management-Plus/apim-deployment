package com.axway.apim;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractDeployment {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDeployment.class);

    public void init(String apiGatewayURL, String username, String password, boolean insecure)
        throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        AxwayClient axwayClient = AxwayClient.getInstance();
        axwayClient.createConnection(apiGatewayURL, username, password, insecure);
    }

    protected String process200Response(HttpResponse response, String customErrorMessage, String jsonPath)
        throws UnsupportedOperationException, ServerException {
        try {
            int statusCode = response.getStatus();

            if (statusCode != 200) {
                LOGGER.error("{} Status code {} Reason {}", customErrorMessage, statusCode, response.getReasonText());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Server Response {}", response.getBody());
                }
                throw new ServerException(customErrorMessage);
            }
            DocumentContext documentContext = JsonPath.parse(response.getBody());
            return documentContext.read(jsonPath, String.class);
        } catch (PathNotFoundException e) {
            throw new ServerException(customErrorMessage, e);
        }
    }

}
