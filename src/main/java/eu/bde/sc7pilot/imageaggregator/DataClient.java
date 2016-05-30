package eu.bde.sc7pilot.imageaggregator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author efi
 */

public class DataClient {

    private static final Logger LOGGER = Logger.getLogger(DataClient.class.getName());
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String ENTITY_SET_NAME = "Products";
    private static final String HTTP_HEADER_ACCEPT = "Accept";
    private static final String ID_PREFIX = "('";
    private static final String ID_SUFFIX = "')";
    private static final String SEPARATOR = "/";
    private static final String SERVICE_URL = "https://scihub.copernicus.eu/apihub/odata/v1";
    private static final String USED_FORMAT = DataClient.APPLICATION_OCTET_STREAM;
    private static final String URL_SUFFIX = "$value";
    
    private final HttpClient httpClient;

    public DataClient(String username, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }

    private String createUri(String serviceUri, String entitySetName, String id) {
        final StringBuilder absolutUri = new StringBuilder(serviceUri).append(SEPARATOR).append(entitySetName);
        if (id != null) {
            absolutUri.append(ID_PREFIX).append(id).append(ID_SUFFIX).append(SEPARATOR).append(URL_SUFFIX);
        }
        System.out.println(absolutUri.toString());
        return "https://scihub.copernicus.eu/apihub/odata/v1/Products('f66b7312-2dda-4c8a-a1f9-3f5b96e0ffce')/$value";
    }
    
    public void downloadAndSaveById(String id, String targetFile) throws IOException{
        OutputStream target = new FileOutputStream(targetFile);
        String absolutUri = createUri(SERVICE_URL, ENTITY_SET_NAME, id);
        InputStream content = null;
        try {
            content = execute(absolutUri, USED_FORMAT);
            IOUtils.copy(content, target);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        target.close();
    }
    
    private InputStream execute(String relativeUri, String contentType) throws IOException {
        HttpGet request = new HttpGet(relativeUri);
        request.addHeader(HTTP_HEADER_ACCEPT, contentType);
        HttpResponse response = httpClient.execute(request);
        InputStream content = response.getEntity().getContent();

        return content;
    }
}
