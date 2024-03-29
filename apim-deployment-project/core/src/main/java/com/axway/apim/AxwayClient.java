package com.axway.apim;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class AxwayClient {

	private PoolingHttpClientConnectionManager cm;
	private HttpClientContext localContext;
	private CredentialsProvider credsProvider;
	private HttpHost target;

	private static HttpHost proxy = null;

	private CloseableHttpClient httpClient;

	static {
		String prxoyHost = System.getProperty("proxyHost");
		String prxoyPort = System.getProperty("proxyPort");
		String proxyProtocol = System.getProperty("proxyProtocol");

		if (prxoyHost != null && prxoyPort != null && proxyProtocol != null) {
			proxy = new HttpHost(prxoyHost, Integer.parseInt(prxoyPort), proxyProtocol);
		}
	}

	public void createConnection(String apiManagerURL, String username, String password)
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//
		URI uri = URI.create(apiManagerURL);
//		SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, new TrustSelfSignedStrategy())
//				.build();
//		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
//		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
//				.register(uri.getScheme(), sslsf).build();
//
//		cm = new PoolingHttpClientConnectionManager(registry);
//		// Increase max total connection to 200
//		cm.setMaxTotal(2);
//		// Increase default max connection per route to 20
//		cm.setDefaultMaxPerRoute(2);
//		// Increase max connections for localhost:80 to 50
		credsProvider = new BasicCredentialsProvider();
//
		target = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
		credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
				new UsernamePasswordCredentials(username, password));

		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(target, basicAuth);

		// Add AuthCache to the execution context
		localContext = HttpClientContext.create();
		localContext.setAuthCache(authCache);

//		cm.setMaxPerRoute(new HttpRoute(target), 2);
//
//		httpClient = HttpClientBuilder.create().disableRedirectHandling().setConnectionManager(cm)
//				.setDefaultCredentialsProvider(credsProvider).setSSLSocketFactory(sslsf).build();


		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, (x509CertChain, authType) -> true);
		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(),  NoopHostnameVerifier.INSTANCE);
		httpClient = HttpClientBuilder.create().disableRedirectHandling().setSSLSocketFactory(sslConnectionSocketFactory).setDefaultCredentialsProvider(credsProvider)
				.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
	}

	public HttpResponse postMultipartFile(URI uri, String filePath, String attachmentName)
			throws ClientProtocolException, IOException {

		File file = new File(filePath);
		FileBody fileBody = new FileBody(file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
		HttpEntity entity = MultipartEntityBuilder.create().addPart(attachmentName, fileBody).build();
		HttpPost httpPost = new HttpPost(uri);
		if (proxy != null) {
			RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
			httpPost.setConfig(requestConfig);
		}
		httpPost.setEntity(entity);
		CloseableHttpResponse response = httpClient.execute(httpPost, localContext);
		return response;
	}

	public HttpResponse put(URI uri, String contentType, String request) throws IOException {

		HttpPut httpput = new HttpPut(uri);
		if (request != null) {
			StringEntity stringEntity = new StringEntity(request);
			httpput.setEntity(stringEntity);
		}
		if (contentType != null)
			httpput.setHeader("Content-type", contentType);

		if (proxy != null) {
			RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
			httpput.setConfig(requestConfig);
		}

		CloseableHttpResponse response = httpClient.execute(httpput, localContext);
		return response;
	}

	public HttpResponse get(URI uri) throws  IOException {

		HttpGet get = new HttpGet(uri);
		if (proxy != null) {
			RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
			get.setConfig(requestConfig);
		}

		CloseableHttpResponse response = httpClient.execute(target, get, localContext);
		return response;

	}

}
