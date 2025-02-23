package com.axway.apim;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.HostnameVerifier;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class AxwayClient {


    private CloseableHttpClient httpClient;
    private UsernamePasswordCredentials usernamePasswordCredentials;
    private HttpHost targetHost;
    private static AxwayClient instance;

    private AxwayClient() {

    }

    public static AxwayClient getInstance() {
        if (instance == null) {
            instance = new AxwayClient();
        }
        return instance;
    }

    public void createConnection(String apiManagerURL, String username, String password, boolean insecure)
        throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String proxyHost = System.getProperty("proxyHost");
        String proxyPort = System.getProperty("proxyPort");
        String proxyProtocol = System.getProperty("proxyProtocol");
        URI uri = URI.create(apiManagerURL);
        targetHost = new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
        SSLContextBuilder builder = new SSLContextBuilder();
        HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
        if (insecure) {
            // Accept all certificates
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            builder.loadTrustMaterial(null, acceptingTrustStrategy);
            // Disable hostname verification
            hostnameVerifier = new NoopHostnameVerifier();
        }
        usernamePasswordCredentials = new UsernamePasswordCredentials(username, password.toCharArray());
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(Timeout.ofMilliseconds(60000))
            .setSocketTimeout(Timeout.ofMilliseconds(60000)).build();
        TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(builder.build(), hostnameVerifier);
        HttpClientConnectionManager httpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsSocketStrategy(tlsStrategy)
            .setDefaultTlsConfig(TlsConfig.custom()
                .setHandshakeTimeout(Timeout.ofSeconds(30))
                .setSupportedProtocols(TLS.V_1_3, TLS.V_1_2)
                .build())
            .setDefaultConnectionConfig(connectionConfig)
            .build();

        HttpClientBuilder httpClientBuilder = HttpClients.custom().disableRedirectHandling()
            .setConnectionManager(httpClientConnectionManager);
        if (proxyHost != null && proxyPort != null && proxyProtocol != null) {
            HttpHost proxy = new HttpHost(proxyProtocol, proxyHost, Integer.parseInt(proxyPort));
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);
        }

        httpClient = httpClientBuilder.build();

    }

    public HttpResponse postMultipartFile(URI uri, String filePath, String attachmentName)
        throws IOException {

        File file = new File(filePath);
        FileBody fileBody = new FileBody(file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
        HttpEntity entity = MultipartEntityBuilder.create().addPart(attachmentName, fileBody).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(entity);
        return httpClient.execute(httpPost, getContext(), this::handleResponse);
    }

    public HttpResponse put(URI uri, String contentType, String request) throws IOException {

        HttpPut httpput = new HttpPut(uri);
        if (request != null) {
            StringEntity stringEntity = new StringEntity(request);
            httpput.setEntity(stringEntity);
        }
        if (contentType != null)
            httpput.setHeader("Content-type", contentType);

        return httpClient.execute(httpput, getContext(), this::handleResponse);
    }

    public HttpResponse get(URI uri) throws IOException {

        HttpGet get = new HttpGet(uri);
        return httpClient.execute(get, getContext(), this::handleResponse);
    }

    public HttpClientContext getContext() {
        return ContextBuilder.create()
            .preemptiveBasicAuth(targetHost, usernamePasswordCredentials)
            .build();
    }


    public HttpResponse handleResponse(org.apache.hc.core5.http.ClassicHttpResponse response) throws IOException, ParseException {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus(response.getCode());
        httpResponse.setReasonText(response.getReasonPhrase());
        httpResponse.setBody(EntityUtils.toString(response.getEntity()));
        return httpResponse;
    }
}
