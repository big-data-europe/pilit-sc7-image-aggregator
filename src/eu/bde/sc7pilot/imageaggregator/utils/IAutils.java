package eu.bde.sc7pilot.imageaggregator.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import eu.bde.sc7pilot.imageaggregator.model.Image;

public class IAutils {
	
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
    
    public static void  downloadDem(Geometry selectedArea, String preferredFileName) {
    	//Extending a bit the selected area for being sure.
    	System.out.println("Selected Polygon:\t" + selectedArea);
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

}
