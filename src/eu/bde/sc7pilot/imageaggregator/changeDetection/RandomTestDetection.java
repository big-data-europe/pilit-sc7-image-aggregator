package eu.bde.sc7pilot.imageaggregator.changeDetection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

import eu.bde.sc7pilot.imageaggregator.model.Area;
import eu.bde.sc7pilot.imageaggregator.model.Change;
import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import eu.bde.sc7pilot.imageaggregator.utils.IdRetrieval;

public class RandomTestDetection implements ChangeDetection {

	@Override
	public List<Change> detectChanges(List<Image> images,ImageData imageData, String finalOutput) throws Exception {
		List<Change> changes = new ArrayList<Change>();
		Image targetImage = images.get(0);
		Image sourceImage = images.get(1);
		System.out.println(targetImage.getDate());
		System.out.println(sourceImage.getDate());
		Random random = new Random();
		Coordinate[] coords = images.get(0).getWKTGeometry().getCoordinates();
		Envelope env = imageData.getArea().getEnvelopeInternal();
		
		//*** Take the real DBScan result and return its polygons as changes
		String[] geometriesArray = new String[1000000];
		String line = "";
		int counter = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(finalOutput))) {
			while ((line = br.readLine()) != null) {
				System.out.println("Counter: " + counter);
				geometriesArray[counter++] = line;
				System.out.println("Line: " + counter + " : " + line);
		    }
		}
		System.out.println("counter = " + counter);
		
		WKTReader wktReader = new WKTReader();		
		Geometry geometries;
		Area area;
		DateTimeFormatter parser22 = ISODateTimeFormat.dateTimeParser();
		Change change;
		for (int i = 0; i < counter; i++)
		{
			geometries = wktReader.read(geometriesArray[i]);
			area = new Area(("Amatrice"+i),geometries,IdRetrieval.getId(false));
			change = new Change(IdRetrieval.getId(true),parser22.parseDateTime(sourceImage.getDate().toString()),
					parser22.parseDateTime(targetImage.getDate().toString()),area, sourceImage.getName(), targetImage.getName());
			changes.add(change);
		}
		
		System.out.println("RETURN General case");
		return changes;
		
		//*** Code returning 3 random polygons in the selected area given
//		int nOfChanges=0;
//		while(nOfChanges<3){
//			Coordinate[] newPolygonCoords = new Coordinate[imageData.getArea().getCoordinates().length];
//			Coordinate newLeftUp = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
//			Coordinate newRightDown = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
//			newPolygonCoords[0] = newLeftUp;
//			newPolygonCoords[1] = new Coordinate(newRightDown.x,newLeftUp.y);
//			newPolygonCoords[2] = newRightDown;
//			newPolygonCoords[3] = new Coordinate(newLeftUp.x,newRightDown.y);
//			newPolygonCoords[imageData.getArea().getCoordinates().length-1] = newPolygonCoords[0];
//			GeometryFactory f = new GeometryFactory();
//			System.out.println(f.createPolygon(newPolygonCoords));
//			Geometry newg = f.createPolygon(newPolygonCoords);
//			nOfChanges++;		
//			Area area = new Area("test area" + nOfChanges, newg, IdRetrieval.getId(false));
//			Change change = new Change(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area);
//			changes.add(change);
//			}
//		
//		return changes;
		// ~~~ End of random-polygon-code
	}

}
