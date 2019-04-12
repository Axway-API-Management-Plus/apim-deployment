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
import com.axway.apim.model.FrontendAPI;
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

		List<FrontendAPI> publishedAPIsByNameAndVersionAndPath = new ArrayList<>();
		List<FrontendAPI> publishedAPIsByNameAndVersion = new ArrayList<>();
		List<FrontendAPI> publishedAPIsByName = new ArrayList<>();
		List<FrontendAPI> allAPIByName = new ArrayList<>();

		List<FrontendAPI> frontdendAPIs = managerDeployment.getAPIByName(url, name);

		for (FrontendAPI frontendAPI : frontdendAPIs) {

			allAPIByName.add(frontendAPI);

			if (frontendAPI.getState().equals(PUBLISHED)) {
				publishedAPIsByName.add(frontendAPI);

				if (version.contains(frontendAPI.getVersion())) {
					publishedAPIsByNameAndVersion.add(frontendAPI);
					if (path != null && frontendAPI.getPath().equals(path)) {
						publishedAPIsByNameAndVersionAndPath.add(frontendAPI);
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

	private void upgradeAPI(String url, FrontendAPI frontendAPI, List<FrontendAPI> allAPIByName,
							List<FrontendAPI> updatedAllAPIByName, String virtualHost, boolean apiUnpublishedRemove, boolean deprecate,
							boolean retire) throws ClientProtocolException, URISyntaxException, IOException, APIMException {

		String id = frontendAPI.getId();
		String backendAPIId = frontendAPI.getApiId();
		updatedAllAPIByName.removeAll(allAPIByName);
		FrontendAPI newFrontendAPI = updatedAllAPIByName.get(0); // New API
																	// id
		String newId = newFrontendAPI.getId();
		managerDeployment.publishAPI(url, newId, newFrontendAPI.getName(), virtualHost);
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
		List<FrontendAPI> virtualizedApis = getFrontendAPIs(url, apiName, version, null)
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
			if(backendURL != null) {
				documentContext.set("$.frontend.apis[0].serviceProfiles._default.basePath", backendURL);
			}

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
			
			String apiState = documentContext.read("$.frontend.apis[0].state");

			logger.info("API state {}", apiState);
			
			if(!apiState.equals(PUBLISHED)){
				throw new APIMException("API artifact "+apiName +" is not in published state, exiting...");
			}

			List<String> version = new ArrayList<>();
			version.add(apiVersion);
			managerDeployment.init(url, username, password);
			APIType apiType = getFrontendAPIs(url, apiName, version, path);

			List<FrontendAPI> publishedAPIsByNameAndVersionAndPath = apiType.getPublishedAPIsByNameAndVersionAndPath();
			List<FrontendAPI> publishedAPIsByName = apiType.getPublishedAPIsByName();
			List<FrontendAPI> allAPIByName = apiType.getAllAPIByName();

			String orgId = managerDeployment.getOrganizationId(url, orgName);

			if (!publishedAPIsByName.isEmpty()) {

				if (!publishedAPIsByNameAndVersionAndPath.isEmpty()) {
					//publishing API with same version
					if (!apiConflictUpgrade) {
						logger.error(" API {} with version {} and path {} is already in published state", apiName,
								apiVersion, path);
						logger.error("set apiConflictUpgrade to true to override the API");
						throw new APIMException("API Conflict");
					}

					logger.info("Updating the exiting API {} with version {} and path {} ", apiName, apiVersion, path);

					FrontendAPI frontendAPI = publishedAPIsByNameAndVersionAndPath.get(0);
					managerDeployment.importAPI(url, orgId, content);
					List<FrontendAPI> updatedVirtualizedApis = getFrontendAPIs(url, apiName, version, null)
							.getAllAPIByName();
					updatedVirtualizedApis.removeAll(allAPIByName);
					upgradeAPI(url, frontendAPI, allAPIByName, updatedVirtualizedApis, virtualHost, apiUnpublishedRemove,
							false, false);

				}else{
					//publishing API with different version
					logger.info(" API {} is in published state in target server", apiName);
					FrontendAPI frontendAPI = publishedAPIsByName.get(0);
					managerDeployment.importAPI(url, orgId, content);
					List<FrontendAPI> updatedVirtualizedApis = getFrontendAPIs(url, apiName, version, null)
							.getAllAPIByName();
					updatedVirtualizedApis.removeAll(allAPIByName);
					logger.info("Upgrading access from  API {} and version to API {} and version", frontendAPI.getName(), frontendAPI.getVersion(), apiName, apiVersion);
					//As api version is different not deleting API
					upgradeAPI(url, frontendAPI, allAPIByName, updatedVirtualizedApis, virtualHost, false,
							false, false);
				}

			} else {
				logger.info(" API {} with version {} and path {} is not available in targer server", apiName, apiVersion, path);
				managerDeployment.importAPI(url, orgId, content);
				List<FrontendAPI> updatedVirtualizedApis = getFrontendAPIs(url, apiName, version, null)
						.getAllAPIByName();
				updatedVirtualizedApis.removeAll(allAPIByName);
				FrontendAPI frontendAPI = updatedVirtualizedApis.get(0);
				String newId = frontendAPI.getId();
				managerDeployment.publishAPI(url, newId, frontendAPI.getName(), virtualHost);
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
