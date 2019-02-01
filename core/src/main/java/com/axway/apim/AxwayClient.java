package com.axway.apim;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

public class AxwayClient {

	private PoolingHttpClientConnectionManager cm;
	private HttpClientContext localContext;
	private CredentialsProvider credsProvider;
	private HttpHost target;

	public void createConnection(String apiManagerURL, String username, String password)
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

		URI uri = URI.create(apiManagerURL);
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());

		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
				.register(uri.getScheme(), sslsf).build();

		cm = new PoolingHttpClientConnectionManager(r);
		// Increase max total connection to 200
		cm.setMaxTotal(2);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(2);
		// Increase max connections for localhost:80 to 50
		credsProvider = new BasicCredentialsProvider();

		target = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
		credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
				new UsernamePasswordCredentials(username, password));

		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(target, basicAuth);

		// Add AuthCache to the execution context
		localContext = HttpClientContext.create();
		localContext.setAuthCache(authCache);

		cm.setMaxPerRoute(new HttpRoute(target), 2);

	}

	public HttpResponse postRequest(List<NameValuePair> formparams, String url)
			throws ClientProtocolException, IOException {

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);

		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
				.setDefaultCredentialsProvider(credsProvider).build();

		HttpPost httppost = new HttpPost(url);
		httppost.setEntity(entity);
		CloseableHttpResponse response = httpClient.execute(httppost, localContext);
		return response;
	}

	public HttpResponse postRequest(String request, String url, String contentType)
			throws ClientProtocolException, IOException {

		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
				.setDefaultCredentialsProvider(credsProvider).build();

		HttpPost httppost = new HttpPost(url);
		StringEntity stringEntity = new StringEntity(request);
		httppost.setEntity(stringEntity);
		httppost.setHeader("Content-type", contentType);

		CloseableHttpResponse response = httpClient.execute(httppost, localContext);
		return response;
	}

	public HttpResponse postMultipart(URI uri, String filePath, String attachmentName)
			throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
				.setDefaultCredentialsProvider(credsProvider).build();

		File file = new File(filePath);
		FileBody fileBody = new FileBody(file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
		HttpEntity entity = MultipartEntityBuilder.create().addPart(attachmentName, fileBody).build();
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = httpClient.execute(httpPost, localContext);
		return response;
	}

	public HttpResponse putRequest(String request, URI uri, String contentType)
			throws ClientProtocolException, IOException {

		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
				.setDefaultCredentialsProvider(credsProvider).build();

		HttpPut httppost = new HttpPut(uri);
		if (request != null) {
			StringEntity stringEntity = new StringEntity(request);
			httppost.setEntity(stringEntity);
		}
		if(contentType != null)
			httppost.setHeader("Content-type", contentType);

		CloseableHttpResponse response = httpClient.execute(httppost, localContext);
		return response;
	}

	public HttpResponse getRequest(URI uri) throws ClientProtocolException, IOException {

		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
				.setDefaultCredentialsProvider(credsProvider).build();
		HttpGet get = new HttpGet(uri);
		CloseableHttpResponse response = httpClient.execute(target, get, localContext);
		return response;
	}

}
