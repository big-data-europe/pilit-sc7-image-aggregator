package eu.bde.sc7pilot.imageaggregator.retrieve;

public interface RdfStorage {
	
	public boolean storeRdf(String rdfPath) throws Exception;

	public String queryRdf(String query) throws Exception;
}