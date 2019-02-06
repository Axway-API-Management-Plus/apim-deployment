package com.axway.apim;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.security.cert.CertificateException;

import org.apache.http.ParseException;

import com.axway.apim.model.ManagerInput;
import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class APIManagerWrapperTest extends TestCase {

	private APIManagerWrapper apiManagerWrapper = new APIManagerWrapper();

	public void setUp() throws Exception {
		Injector injector = Guice.createInjector(new DeploymentModule());
		injector.injectMembers(apiManagerWrapper);

	}

	@org.junit.Test
	public void testExportAPIs() {

		// Dev
		List<String> version = new ArrayList<String>();
		version.add("1.0.0");
		try {
			apiManagerWrapper.exportAPIs("https://10.129.60.57:8075", "apiadmin", "changeme",
					"d:\\api\\test\\petstore\\petstore.json", "petstore", version);
		} catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
				| URISyntaxException | APIMException e) {
			e.printStackTrace();
			fail("Export Failed ");
		}
	}

	@org.junit.Test
	public void testConflictImport() {

		File file = new File("src/test/resources/inbound-certs");
		String backendAuth = "{" + "\"parameters\": {" + "\"apiKey\": \"4249823490238490\","
				+ "\"apiKeyField\": \"KeyId\"," + "\"httpLocation\": \"QUERYSTRING_PARAMETER\"" + "},"
				+ "\"type\": \"apiKey\"" + "}";

		System.out.println(backendAuth);

		ManagerInput managerInput = new ManagerInput();
		managerInput.setUrl("https://10.129.60.57:8075");
		managerInput.setUsername("apiadmin");
		managerInput.setPassword("changeme");
		managerInput.setOrgName("API Development");
		managerInput.setBackendURL("https://api.test.com");
		managerInput.setLocation("d:\\api\\test\\petstore\\petstore.json");
		managerInput.setVirtualHost("api.axway.com");
		managerInput.setOutboundCertFolder(file.getAbsolutePath());
		managerInput.setBackendAuthJson(backendAuth);

		try {
			apiManagerWrapper.importAPIs(managerInput);
		} catch (IOException | CertificateException | UnsupportedOperationException | URISyntaxException
				| ServerException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
				| ParseException | APIMException e) {
			fail("API import Failed ");
		}

	}

	@org.junit.Test
	public void testOverrideImport() {

		File file = new File("src/test/resources/inbound-certs");
		String backendAuth = "{" + "\"parameters\": {" + "\"apiKey\": \"4249823490238490\","
				+ "\"apiKeyField\": \"KeyId\"," + "\"httpLocation\": \"QUERYSTRING_PARAMETER\"" + "},"
				+ "\"type\": \"apiKey\"" + "}";

		System.out.println(backendAuth);

		ManagerInput managerInput = new ManagerInput();
		managerInput.setUrl("https://10.129.60.57:8075");
		managerInput.setUsername("apiadmin");
		managerInput.setPassword("changeme");
		managerInput.setOrgName("API Development");
		managerInput.setBackendURL("https://api.test.com");
		managerInput.setLocation("d:\\api\\test\\petstore\\petstore.json");
		managerInput.setVirtualHost("api.axway.com");
		managerInput.setOutboundCertFolder(file.getAbsolutePath());
		managerInput.setBackendAuthJson(backendAuth);
		managerInput.setApiConflictUpgrade(true);

		try {
			apiManagerWrapper.importAPIs(managerInput);
		} catch (IOException | CertificateException | UnsupportedOperationException | URISyntaxException
				| ServerException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
				| ParseException | APIMException e) {
			e.printStackTrace();
			fail("API import Failed ");
		}
	}
}
