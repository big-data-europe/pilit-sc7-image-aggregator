package eu.bde.sc7pilot.imageaggregator.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import eu.bde.sc7pilot.imageaggregator.model.Image;

public class IAutils {
	
//	public static void main(String[] args) throws ParseException {
//		WKTReader wkt = new WKTReader();
//		Geometry mygeom = wkt.read("POLYGON ((22.38713264465332 39.61145432244845, 22.38713264465332 39.656271025411606, 22.45081901550293 39.656271025411606, 22.45081901550293 39.61145432244845, 22.38713264465332 39.61145432244845))");
//		Envelope myenv = mygeom.getEnvelopeInternal();
//		System.out.println(mygeom.getArea());
//		double minX = myenv.getMinX();
//		double maxX = myenv.getMaxX();
//		double minY = myenv.getMinY();
//		double maxY = myenv.getMaxY();
//		System.out.println("MinX = " + minX);
//		System.out.println("MaxX = " + maxX);
//		System.out.println("MinY = " + minY);
//		System.out.println("MaxY = " + maxY);
//		double areaKM2 = ((maxY - minY) * 111) * ((maxX - minX) * 85.4);
//		System.out.println(areaKM2 + "km2");
//		Coordinate[] allCoords = mygeom.getCoordinates();
//		for (int i = 0; i < allCoords.length; i++)
//			System.out.println(allCoords[i]);
//	}
	
	/*
     * This method submits the shell script that runs the TerrainCorrection C++ code.
     * it is needed to extract the boundaries of the selected area in order to give them
     * to the sh and then as arguments to the C++ TC code.
     */
	public static void applyTerrainCorrection(String shAbsFilepath, String imgFilePath, String demFilePath, String resultFilePath, Geometry selectedArea) {
		System.out.println("Selected Polygon:\t" + selectedArea);
    	Envelope areasEnvelope = selectedArea.getEnvelopeInternal();
		double minX = areasEnvelope.getMinX();
		double minY = areasEnvelope.getMinY();
		double maxX = areasEnvelope.getMaxX();
		double maxY = areasEnvelope.getMaxY();
		
		//code to submit the shell script that downloads the dem according to mins and maxes
		runShellScript(shAbsFilepath, imgFilePath, demFilePath, resultFilePath, Double.toString(minX), Double.toString(minY), Double.toString(maxX), Double.toString(maxY));
	}
	
    /*
     * The areaWithinImage checks if the (user's) selectedArea is within the area the image covers.
     * We need all the products and images to include the selected area!!!
     */
    public static Boolean areaWithinImages(Geometry selectedArea, List<Image> imagesForDownl) {
    	System.out.println("\nSelected area:\t" + selectedArea.toString());
    	boolean legit = false;
    	for (int i = 0; i < imagesForDownl.size(); i++) {
    		if (selectedArea.within(imagesForDownl.get(i).getWKTGeometry())); {
    			System.out.println("Area covered by img:\t" + imagesForDownl.get(i).getWKTGeometry().toString());
    			legit = true;
    		}
    	}
    	System.out.println("I am returning:\t" + legit);
    	return legit;
    }
    
    public static void infoImages(List<Image> images) {
		for(int i = 0; i < images.size(); i ++){
			System.out.println("\n\tINFO FOR IMAGE: " + i);
			System.out.println(images.get(i).getName());
			System.out.println(images.get(i).getId());
			System.out.println(images.get(i).getDate());
		}
    }
    
	/* This method calculates an a bit larger polygon than the given
	 * and runs the shell script which contains python's elevation module.
	 * The method is made exclusively for this shell script,
	 * that also contains a "mv" command to move the downloaded dem 
	 * to the preferred (see last argument) directory.
	 */
    public static String downloadDem(String shAbsFilepath, Geometry selectedArea, String preferredFileName, String targetDir) {
    	//Extending a bit the selected area for being sure.
    	System.out.println("Selected Polygon:\t" + selectedArea.toString());
    	float d = (float) 0.005;
    	Envelope areasEnvelope = selectedArea.getEnvelopeInternal();
		double demMinX = areasEnvelope.getMinX() - d;
		double demMinY = areasEnvelope.getMinY() - d;
		double demMaxX = areasEnvelope.getMaxX() + d;
		double demMaxY = areasEnvelope.getMaxY() + d;
		
		//Only for viewing the extended polygon:
		GeometryFactory geomFact = new GeometryFactory();
		System.out.println("Extended Selected Polygon for dem:\t" + geomFact.toGeometry(new Envelope(demMinX, demMaxX, demMinY, demMaxY)));
		
		//code to submit the shell script that downloads the dem according to mins and maxes
		runShellScript(shAbsFilepath, preferredFileName, Double.toString(demMinX), Double.toString(demMinY), Double.toString(demMaxX), Double.toString(demMaxY), targetDir);
		
		return targetDir + File.separator + preferredFileName;
    }
    
	/* This method submits shell script to the running machine.
	 * The first of the arguments must be the file-path of the shell script.
	 * It returns the output of the shell script. 
	 */
	public static String runShellScript(String ...scriptArgs) {
		ProcessBuilder pb = new ProcessBuilder(scriptArgs);
		pb.redirectErrorStream(true);
		StringWriter writer = new StringWriter();
		Process p;
		try {
			p = pb.start();
			IOUtils.copy(p.getInputStream(), writer, "UTF-8");
			System.out.println(writer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}
	
//	public static Double coordToSIDistance()
	
	// Convert this to Java... and maybe it will be 
//	function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
//		  var R = 6371; // Radius of the earth in km
//		  var dLat = deg2rad(lat2-lat1);  // deg2rad below
//		  var dLon = deg2rad(lon2-lon1); 
//		  var a = 
//		    Math.sin(dLat/2) * Math.sin(dLat/2) +
//		    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
//		    Math.sin(dLon/2) * Math.sin(dLon/2)
//		    ; 
//		  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
//		  var d = R * c; // Distance in km
//		  return d;
//		}
//
//		function deg2rad(deg) {
//		  return deg * (Math.PI/180)
//		}

}
