package com.axway.apim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.model.APIType;
import com.axway.apim.model.AuthType;
import com.axway.apim.model.AuthenticationProfile;
import com.axway.apim.model.CACert;
import com.axway.apim.model.FrondendAPI;
import com.axway.apim.model.ManagerInput;
import com.google.inject.Inject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class APIManagerWrapper implements Constants {

	private static Logger logger = LoggerFactory.getLogger(APIManagerWrapper.class);

	@Inject
	private ManagerDeployment managerDeployment;

	private APIType getFrontendAPIs(String url, String name, List<String> version, String path)
			throws ClientProtocolException, URISyntaxException, IOException {
		APIType apiType = new APIType();

		List<FrondendAPI> publishedAPIsByNameAndVersionAndPath = new ArrayList<>();
		List<FrondendAPI> publishedAPIsByNameAndVersion = new ArrayList<>();
		List<FrondendAPI> publishedAPIsByName = new ArrayList<>();
		List<FrondendAPI> allAPIByName = new ArrayList<>();

		List<FrondendAPI> frondendAPIs = managerDeployment.getAPIByName(url, name);

		for (FrondendAPI frondendAPI : frondendAPIs) {

			allAPIByName.add(frondendAPI);

			if (frondendAPI.getState().equals(PUBLISHED)) {
				publishedAPIsByName.add(frondendAPI);

				if (version.contains(frondendAPI.getVersion())) {
					publishedAPIsByNameAndVersion.add(frondendAPI);
					if (path != null && frondendAPI.getPath().equals(path)) {
						publishedAPIsByNameAndVersionAndPath.add(frondendAPI);
					}
				}

			}
		}

		apiType.setAllAPIByName(allAPIByName);
		apiType.setPublishedAPIsByName(publishedAPIsByName);
		apiType.setPublishedAPIsByNameAndVersion(publishedAPIsByNameAndVersion);
		apiType.setPublishedAPIsByNameAndVersionAndPath(publishedAPIsByNameAndVersionAndPath);
		return apiType;
	}

	private void upgradeAPI(String url, FrondendAPI frondendAPI, List<FrondendAPI> allAPIByName,
			List<FrondendAPI> updatedAllAPIByName, String virtualHost, boolean apiUnpublishedRemove, boolean deprecate,
			boolean retire) throws ClientProtocolException, URISyntaxException, IOException, APIMException {

		String id = frondendAPI.getId();
		String backendAPIId = frondendAPI.getApiId();
		updatedAllAPIByName.removeAll(allAPIByName);
		FrondendAPI newFrondendAPI = updatedAllAPIByName.get(0); // New API
																	// id
		String newId = newFrondendAPI.getId();
		managerDeployment.publishVirtualizedAPI(url, newId, newFrondendAPI.getName(), virtualHost);
		managerDeployment.upgradeAPI(url, newId, id, deprecate, retire);
		managerDeployment.unpublishAPI(url, id);
		if (apiUnpublishedRemove) {
			managerDeployment.deleteFrondendAPI(url, id);
			managerDeployment.deleteBackendAPI(url, backendAPIId);
		}
	}

	public void exportAPIs(String url, String username, String password, String location, String apiName,
			List<String> version) throws IOException, KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, URISyntaxException, APIMException {

		managerDeployment.init(url, username, password);
		List<FrondendAPI> virtualizedApis = getFrontendAPIs(url, apiName, version, null)
				.getPublishedAPIsByNameAndVersion();

		if (virtualizedApis.isEmpty()) {
			logger.error("No API to Export ");
			System.exit(1);
		}

		String response = managerDeployment.exportAPI(url, virtualizedApis, "api-export.dat");
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(location);
			fileWriter.write(response);
			fileWriter.flush();
		} finally {
			if (fileWriter != null)
				fileWriter.close();
		}

	}

	public void importAPIs(ManagerInput input) throws IOException, CertificateException, UnsupportedOperationException,
			URISyntaxException, ServerException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
			ParseException, APIMException {

		String url = input.getUrl();
		String username = input.getUsername();
		String password = input.getPassword();
		String location = input.getLocation();
		String orgName = input.getOrgName();
		String backendURL = input.getBackendURL();
		String outboundCertFolder = input.getOutboundCertFolder();
		String backendAuthJson = input.getBackendAuthJson();
		String virtualHost = input.getVirtualHost();
		boolean apiConflictUpgrade = input.isApiConflictUpgrade();
		boolean apiUnpublishedRemove = input.isApiUnpublishedRemove();
		InputStream inputStream = null;

		File apiFilename = new File(location);
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

			if (outboundCertFolder != null) {

				List<CACert> list = getCerts(new File(outboundCertFolder));
				documentContext.set("$.frontend.apis[0].caCerts", list);
			}

			if (backendAuthJson != null) {

				DocumentContext backendAuthContext = JsonPath.parse(backendAuthJson);
				String type = backendAuthContext.read("$.type", String.class);
				Properties properties = backendAuthContext.read("$.parameters", Properties.class);
				AuthenticationProfile authenticationProfile = new AuthenticationProfile();
				authenticationProfile.setType(AuthType.valueOf(type));
				authenticationProfile.setParameters(properties);
				List<AuthenticationProfile> authenticationProfiles = new ArrayList<>();
				authenticationProfiles.add(authenticationProfile);
				documentContext.set("$.frontend.apis[0].authenticationProfiles", authenticationProfiles);
			}

			String content = documentContext.jsonString();
			String apiName = documentContext.read("$.frontend.apis[0].name");
			String apiVersion = documentContext.read("$.frontend.apis[0].version");
			String path = documentContext.read("$.frontend.apis[0].path");

			List<String> version = new ArrayList<>();
			version.add(apiVersion);
			managerDeployment.init(url, username, password);
			APIType apiType = getFrontendAPIs(url, apiName, version, path);

			List<FrondendAPI> publishedAPIsByNameAndVersionAndPath = apiType.getPublishedAPIsByNameAndVersionAndPath();
			List<FrondendAPI> publishedAPIsByName = apiType.getPublishedAPIsByName();
			List<FrondendAPI> allAPIByName = apiType.getAllAPIByName();

			String orgId = managerDeployment.getOrganizationId(url, orgName);

			if (!publishedAPIsByName.isEmpty()) {

				if (!publishedAPIsByNameAndVersionAndPath.isEmpty()) {
					if (!apiConflictUpgrade) {
						logger.error(" API {} with version {} and path {} is already in published state", apiName,
								apiVersion, path);
						logger.error("set apiConflictUpgrade to true to override the API");
						throw new APIMException("API Conflict");
					}

					logger.info("Updating the exiting API {} with version {} and path {} ", apiName, apiVersion, path);

					FrondendAPI frondendAPI = publishedAPIsByNameAndVersionAndPath.get(0);
					managerDeployment.importAPI(url, orgId, content);
					List<FrondendAPI> updatedVirtualizedApis = getFrontendAPIs(url, apiName, version, null)
							.getAllAPIByName();
					upgradeAPI(url, frondendAPI, allAPIByName, updatedVirtualizedApis, virtualHost, apiUnpublishedRemove,
							false, false);

				}

			} else {
				logger.info(" API {} with version {} and path {} is not in published state", apiName, apiVersion, path);

				managerDeployment.importAPI(url, orgId, content);
				List<FrondendAPI> updatedVirtualizedApis = getFrontendAPIs(url, apiName, version, null)
						.getAllAPIByName();
				FrondendAPI frondendAPI = publishedAPIsByName.get(0);
				upgradeAPI(url, frondendAPI, allAPIByName, updatedVirtualizedApis, virtualHost, apiUnpublishedRemove,
						false, false);
			}

			if (allAPIByName.isEmpty()) {
				logger.info(" API {} with version {} and path {} is not available", apiName, apiVersion, path);
				managerDeployment.importAPI(url, orgId, content);
				List<FrondendAPI> updatedVirtualizedApis = getFrontendAPIs(url, apiName, version, null)
						.getAllAPIByName();
				FrondendAPI frondendAPI = updatedVirtualizedApis.get(0);
				String newId = frondendAPI.getId();
				managerDeployment.publishVirtualizedAPI(url, newId, frondendAPI.getName(), virtualHost);
			}
		} finally {
			if (inputStream != null)
				inputStream.close();

		}
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

}
