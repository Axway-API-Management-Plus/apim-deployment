package com.axway.apim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.security.cert.CertificateException;
import javax.security.cert.CertificateExpiredException;
import javax.security.cert.CertificateNotYetValidException;
import javax.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import com.axway.apim.model.AuthType;
import com.axway.apim.model.AuthenticationProfile;
import com.axway.apim.model.CACert;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.vordel.controller.apimgr.client.ApiManagerClient;
import com.vordel.controller.apimgr.client.ApiManagerClientSession;
import com.vordel.controller.apimgr.client.VirtualizedApisClient;
import com.vordel.controller.apimgr.model.VirtualizedApi;
import com.vordel.controller.apimgr.model.VirtualizedApi.State;
import com.vordel.deploy.orchestrator.Hat;

public class APIManagerWrapper {

	public void exportAPIs(String url, String username, String password, String location, String apiName,
			List<String> version) throws IOException {

		ApiManagerClient.Builder builder = new ApiManagerClient.Builder();

		/* return the client */
		ApiManagerClient apiManagerClient = buildApiManager(builder, URI.create(url + "/api/portal/v1.3"));
		ApiManagerClientSession session = apiManagerClient.getSession();

		session.login(username, password);

		VirtualizedApisClient virtualizedApisClient = new VirtualizedApisClient(session);

		Iterable<VirtualizedApi> viIterable = virtualizedApisClient.getVirtualizedAPIsByName(apiName);

		List<String> virtualizedApis = new ArrayList<String>();

		for (VirtualizedApi virtualizedApi : viIterable) {

			if (virtualizedApi.getState().equals(State.PUBLISHED) && version.contains(virtualizedApi.getVersion())) {
				String id = virtualizedApi.getId();
				virtualizedApis.add(id);
			}

		}

		if (virtualizedApis.isEmpty()) {
			System.out.println("No API to Export ");
			System.exit(0);
		}

		APIExportClient apiExportClient = new APIExportClient(session);

		String response = apiExportClient.exportVirtualizedAPI(virtualizedApis, "test.data");
		FileWriter fileWriter = null;
		try {
			File file = new File(location);
			file.mkdirs();
			fileWriter = new FileWriter(new File(file, "api-export.dat"));
			fileWriter.write(response);
			fileWriter.flush();
		} finally {
			if (fileWriter != null)
				fileWriter.close();
			session.logout();
		}
		

	}

	public void importAPIs(String url, String username, String password, String location, String orgName,
			String backendURL, String outboundCertFolder, String backendAuthJson) throws IOException, CertificateException {
		// String[] arguments = { "--target", "https://10.129.60.57:8075",
		// "--username", "apiadmin", "--password",
		// "changeme", "D:\\API\\Customers\\highmark\\highmark-promotion" };

		InputStream inputStream = null;
		FileWriter fileWriter = null;
		OutputStream outputStream = null;

		File file = new File(location);
		File apiFilename = getAPIArtifact(file);
		try {
			inputStream = new FileInputStream(apiFilename);
			DocumentContext documentContext = JsonPath.parse(inputStream);

			if (backendURL != null) {

				try {
					new URL(backendURL);
				} catch (MalformedURLException e) {
					throw e;
				}
			}
			documentContext.set("$.frontend.apis[0].serviceProfiles._default.basePath", backendURL);
			
			if(outboundCertFolder != null){
				 
				List<CACert> list = getCerts(new File(outboundCertFolder));
				documentContext.set("$.frontend.apis[0].caCerts", list);
			}
			
			if(backendAuthJson != null){
				
				DocumentContext backendAuthContext = JsonPath.parse(backendAuthJson);
				
				String type = backendAuthContext.read("$.type",String.class);
				
				Properties properties = backendAuthContext.read("$.parameters", Properties.class);
				
				AuthenticationProfile authenticationProfile = new AuthenticationProfile();
				
				authenticationProfile.setType(AuthType.valueOf(type));
				authenticationProfile.setParameters(properties);
				
				List<AuthenticationProfile> authenticationProfiles = new ArrayList<>();
				authenticationProfiles.add(authenticationProfile);
				
				documentContext.set("$.frontend.apis[0].authenticationProfiles",authenticationProfiles);
			}
			String content = documentContext.jsonString();
			
			fileWriter = new FileWriter(apiFilename);
			fileWriter.write(content);
			fileWriter.flush();
			Properties properties = new Properties();
			properties.put("organization.apipromotion.import", orgName);
			properties.put("organization.target", orgName);
			properties.put("api.conflict.upgrade", "true");
			properties.put("application.conflict.upgrade", "false");
			properties.put("application.apikey.upgrade", "false");
			properties.put("application.oauthclient.upgrade", "false");
			properties.put("application.oauthresource.upgrade", "false");
			properties.put("api.publish.virtualhost", "");
			properties.put("api.unpublished.remove", "true");

			outputStream = new FileOutputStream(new File(file, "promotion.properties"));

			properties.store(outputStream, "promotion settings");
			outputStream.close();

			String[] arguments = { "--target", url, "--username", username, "--password", password, location };
			Hat.main(arguments);
		} finally {
			if (inputStream != null)
				inputStream.close();

			if (fileWriter != null)
				fileWriter.close();

			if (outputStream != null)
				outputStream.close();
		}
	}

	private File getAPIArtifact(File file) {
		File apiFilename = null;
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			String filename = files[i].getName();
			String extension = FilenameUtils.getExtension(filename);
			if (extension.equals("dat")) {
				apiFilename = files[i];
				break;
			}
		}
		return apiFilename;
	}

	private List<CACert> getCerts(File certDir) throws FileNotFoundException, CertificateException {

		List<CACert> caCerts = new ArrayList<>();

		File[] files = certDir.listFiles();
		for (File file : files) {
			CACert caCert = new CACert();
			InputStream inputStream = new FileInputStream(file);
			X509Certificate certificate = X509Certificate.getInstance(inputStream);
			String issuer = certificate.getIssuerDN().getName();
			String dn = certificate.getSubjectDN().getName();
			Date validFrom = certificate.getNotBefore();
			Date validTo = certificate.getNotAfter();
			int version = certificate.getVersion();
			byte[] encodedData = certificate.getEncoded();
			PublicKey publicKey = certificate.getPublicKey();
			String algorithm = publicKey.getAlgorithm();
			caCert.setCertBlob(Base64.getEncoder().encodeToString(certificate.getEncoded()));
			caCert.setName(dn);
			caCert.setAlias(dn);
			caCert.setSubject(dn);
			caCert.setIssuer(issuer);
			caCert.setVersion(version);
			caCert.setNotValidBefore(validFrom.getTime());
			caCert.setNotValidAfter(validTo.getTime());
			caCert.setSignatureAlgorithm(algorithm);

			String sha1Fingerprint = Hex.encodeHexString(DigestUtils.sha1(encodedData));
			String md5Fingerprint = Hex.encodeHexString(DigestUtils.md5(encodedData));

			caCert.setSha1Fingerprint(sha1Fingerprint);
			caCert.setMd5Fingerprint(md5Fingerprint);

			caCerts.add(caCert);

			try {
				certificate.checkValidity();
			} catch (CertificateExpiredException e) {
				caCert.setExpired(true);
			} catch (CertificateNotYetValidException e) {
				caCert.setNotYetValid(true);
			}

			caCert.setOutbound(true);
		}

		return caCerts;

	}

	private static ApiManagerClient buildApiManager(ApiManagerClient.Builder builder, URI target) {
		builder.setTrustAny(true);
		builder.setTargetURI(target);
		return builder.build();
	}
}
