package eu.bde.sc7pilot.imageaggregator;

import java.util.List;
import javax.ws.rs.NotAuthorizedException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import eu.bde.sc7pilot.imageaggregator.model.Image;

/**
 *
 * @author efi
 */

public class SearchClient {

    private final static String URL = "https://scihub.copernicus.eu/apihub/search";
    private final static String PARAMETER_QUERY = "q";
    private final static String PARAMETER_ROWS = "rows";
    private final static String PARAMETER_START = "start";
    
    private final HttpClient httpClient;

    public SearchClient(String username, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }

    private HttpResponse getResponse(String query, int startIndex, int endIndex) {
        try {
            URIBuilder builder = new URIBuilder(URL);
            builder.setParameter(PARAMETER_QUERY, query);
            builder.setParameter(PARAMETER_ROWS, String.valueOf(endIndex));
            builder.setParameter(PARAMETER_START, String.valueOf(startIndex));
            System.out.println("BUILDER-URI:");
            System.out.println(builder.build());
            System.out.println("END OF BUILDER-URI\n");
            HttpGet request = new HttpGet(builder.build());
            
            return httpClient.execute(request);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    //parser to turn the service's xml response into an object of our Image class 
    private List<Image> parseResponse(HttpResponse response) {
        if (response == null) {
            return null;
        }
        
        SAXParserFactory factory;
        SearchRespHandler handler = null;
        factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            SAXParser parser = factory.newSAXParser();
            handler = new SearchRespHandler();
            parser.parse(response.getEntity().getContent(), handler);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return handler.getProdList();
    }
     
    public List<Image> search(String query, int startIndex, int endIndex) {
        HttpResponse response = getResponse(query, startIndex, endIndex);
        System.out.println("RESPONSE:");
        System.out.println(response.toString());
        System.out.println("END OF RESPONSE:\n");
        if(response.getStatusLine().getStatusCode() == 401)
        	throw new NotAuthorizedException(response);
        return parseResponse(response);
    }
}
