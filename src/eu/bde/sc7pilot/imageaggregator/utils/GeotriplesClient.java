package eu.bde.sc7pilot.imageaggregator.utils;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.bde.sc7pilot.imageaggregator.model.Change;
import eu.bde.sc7pilot.imageaggregator.webconfig.ObjectMapperContextResolver;

public class GeotriplesClient {
	private String host;
	private String port;
	public GeotriplesClient(String host,String port) {
		this.host=host;
		this.port=port;
	}
	public void saveChanges(List<Change> changes) {
		Client client = ClientBuilder.newClient(new ClientConfig()).register(ObjectMapperContextResolver.class)  // No need to register this provider if no special configuration is required.
		        .register(JacksonFeature.class);
		ObjectMapper objectMapper=new ObjectMapper();
		objectMapper.registerModule(new JodaModule());
		objectMapper.registerModule(new JtsModule());
		objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
		try {
			String res=objectMapper.writeValueAsString(changes);
			System.out.println(res);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URI uri = UriBuilder.fromUri(host+":"+port+"/geotriples/changes").build();
		System.out.println(uri.toString());
		WebTarget target = client.target(uri);
		Invocation.Builder invocationBuilder =
				target.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.post(Entity.entity(changes,MediaType.APPLICATION_JSON),
				Response.class);
		System.out.println(response.getStatus()+" "+response.readEntity(String.class));
	}
}
