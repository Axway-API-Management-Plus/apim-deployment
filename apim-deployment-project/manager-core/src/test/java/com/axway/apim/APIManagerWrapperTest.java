package com.axway.apim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.cert.CertificateException;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class APIManagerWrapperTest extends TestCase {

	private APIManagerWrapper apiManagerWrapper = new APIManagerWrapper();

	@org.junit.Test
	public void testExportAPIs() {

		// Dev
		List<String> version = new ArrayList<String>();
		version.add("1.0.0");
		try {
			apiManagerWrapper.exportAPIs("https://10.129.60.57:8075", "apiadmin", "changeme", "d:\\api\\test\\petstore", "petstore", version);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Export Failed ");
		}
	}

	@org.junit.Test
	public void testImport() {
		
		File file = new File("src/test/resources/inbound-certs");
		String backendAuth = "{"
		+	"\"parameters\": {"
		+		"\"apiKey\": \"4249823490238490\","
		+		"\"apiKeyField\": \"KeyId\","
		+		"\"httpLocation\": \"QUERYSTRING_PARAMETER\""
		+	"},"
		+	"\"type\": \"apiKey\""
		+"}";
		
		System.out.println(backendAuth);
		
		try {
			apiManagerWrapper.importAPIs("https://10.129.60.57:8075", "apiadmin", "changeme", "d:\\api\\test\\petstore", "API Development",
					"https://api.test.com",file.getAbsolutePath(), backendAuth);
		} catch (IOException | CertificateException e) {
			e.printStackTrace();
			fail("API import Failed ");
		}
	}
}
