package eu.bde.sc7pilot.imageaggregator.retrieve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.rio.RDFFormat;

import eu.earthobservatory.org.StrabonEndpoint.client.SPARQLEndpoint;

public class StrabonEndpoint implements RdfStorage {
	
	private SPARQLEndpoint endpoint;
	private URL data;
	
	public StrabonEndpoint(String host,String username,String password,int port, String strabonPath) throws MalformedURLException {
		endpoint = new SPARQLEndpoint(host, port, strabonPath);
		endpoint.setUser(username);
		endpoint.setPassword(password);
	}
	
	@Override
	public boolean storeRdf(String rdfPath) throws Exception {
		data = new URL(rdfPath);
		Boolean response = endpoint.store(data, RDFFormat.NTRIPLES , null);
		return response;
	}

	@Override
	public String queryRdf(String query) throws Exception {
		//EndpointResult response = endpoint.query(query, stSPARQLQueryResultFormat.XML);
		//return response.getResponse();
				
		// create a post method to execute
//		HttpPost method = new HttpPost("http://" + Constants.strabonHost + ":" + Constants.strabonPort + "/" + "SemaGrow/sparql");
		HttpPost method = new HttpPost("http://luna.di.uoa.gr:8080/mapRegistry/Query");
		System.out.println(Constants.strabonHost + ":" + Constants.strabonPort + "/" + "SemaGrow/sparql");
		// set the query parameter
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("query", query));
		//System.out.println("3");
		UrlEncodedFormEntity encodedEntity = new UrlEncodedFormEntity(params, Charset.forName("UTF-8"));
		//System.out.println("4");
		method.setEntity(encodedEntity);
		//System.out.println("5");
		// set the content type
		method.setHeader("Content-Type", "application/x-www-form-urlencoded");
		//System.out.println("6");
		// set the accept format
		method.addHeader("Accept", stSPARQLQueryResultFormat.XML.getDefaultMIMEType());
		//System.out.println("7");
		System.out.println("before hc");
		HttpClient hc = new DefaultHttpClient();
		//HttpClient hc = HttpClientBuilder.create().build();
		System.out.println("after hc");
		try {
			// response that will be filled next
			//System.out.println("8");
			String responseBody = "";
			// execute the method
			HttpResponse response = hc.execute(method);
			//System.out.println("9");
			int statusCode = response.getStatusLine().getStatusCode();
			//System.out.println("10");
			System.out.println(statusCode);
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			HttpEntity entity = response.getEntity();
			//System.out.println("11");
			if (entity != null) {
				InputStream instream = entity.getContent();
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
					StringBuffer strBuf = new StringBuffer();
					// do something useful with the response
					String nextLine;
					while ((nextLine = reader.readLine()) != null) {
						strBuf.append(nextLine + "\n");
						System.out.println(nextLine);
						}
						// remove last newline character
					if (strBuf.length() > 0) {
						strBuf.setLength(strBuf.length() - 1);
						}
						responseBody = strBuf.toString();
				}
				catch (IOException ex) {
					// In case of an IOException the connection will be released
					// back to the connection manager automatically
					throw ex;
				}
				catch (RuntimeException ex) {
					// In case of an unexpected exception you may want to abort
					// the HTTP request in order to shut down the underlying
					// connection and release it back to the connection manager.
					method.abort();
					throw ex;
				}
				finally {
					// Closing the input stream will trigger connection release
					instream.close();
				}
			}
					 
		return responseBody;
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			// release the connection.
			method.releaseConnection();
		}
	}
	
}
