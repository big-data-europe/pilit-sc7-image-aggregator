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
import eu.bde.sc7pilot.imageaggregator.model.ChangeStore;
import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import eu.bde.sc7pilot.imageaggregator.utils.IdRetrieval;

public class RandomTestDetection {

	public List<Change> detectChanges(List<Image> images, ImageData imageData, String finalOutput) throws Exception {
		List<Change> changes = new ArrayList<Change>();
		Image targetImage = images.get(0);
		Image sourceImage = images.get(1);
		Change change = null;
		DateTimeFormatter parser22 = ISODateTimeFormat.dateTimeParser();
		
		//*** Take the real DBScan result and return its polygons as changes
		ArrayList<String> geometriesArrayList = new ArrayList<>();
		String line = "";
		try (BufferedReader br = new BufferedReader(new FileReader(finalOutput))) {
			while ((line = br.readLine()) != null) {
				geometriesArrayList.add(line);
		    }
		}
		System.out.println("Number Of Lines in geometriesArrayList: " + geometriesArrayList.size());		
		
		if (geometriesArrayList.size() == 0) {
			// If we have an empty DBScan-output, then we make 3 dummy for posing!
			Random random = new Random();
			Envelope env = imageData.getArea().getEnvelopeInternal();
			int nOfChanges = 0;
			while (nOfChanges < 3) {
				Coordinate[] newPolygonCoords = new Coordinate[imageData.getArea().getCoordinates().length];
				Coordinate newLeftUp = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
				Coordinate newRightDown = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
				newPolygonCoords[0] = newLeftUp;
				newPolygonCoords[1] = new Coordinate(newRightDown.x, newLeftUp.y);
				newPolygonCoords[2] = newRightDown;
				newPolygonCoords[3] = new Coordinate(newLeftUp.x, newRightDown.y);
				newPolygonCoords[imageData.getArea().getCoordinates().length - 1] = newPolygonCoords[0];
				GeometryFactory f = new GeometryFactory();
//				System.out.println(f.createPolygon(newPolygonCoords));	//Uncomment to see the dummy-response polygons.
				Geometry newg = f.createPolygon(newPolygonCoords);
				nOfChanges++;		
				Area area = new Area("test area" + nOfChanges, newg, IdRetrieval.getId(false));
				change = new Change(IdRetrieval.getId(true), parser22.parseDateTime(sourceImage.getDate().toString()),
																					parser22.parseDateTime(targetImage.getDate().toString()),
																					area,
																					sourceImage.getName(),
																					targetImage.getName());
				changes.add(change);				
			}
			System.out.println("Some changes returned");
			return changes;
			
		}
		else {
			WKTReader wktReader = new WKTReader();
			Area area;
			Geometry geometries;
			for (int i = 0; i < geometriesArrayList.size(); i++)
			{
				geometries = wktReader.read(geometriesArrayList.get(i));
				area = new Area(("ChangedArea" + i), geometries, IdRetrieval.getId(false));
				change = new Change(IdRetrieval.getId(true), parser22.parseDateTime(sourceImage.getDate().toString()),
																					parser22.parseDateTime(targetImage.getDate().toString()),
																					area,
																					sourceImage.getName(),
																					targetImage.getName());
				changes.add(change);
			}
			System.out.println("Real changes returned");
			return changes;
		
		}		
	}
	
	public List<ChangeStore> detectChangesForStore(List<Image> images, ImageData imageData, String finalOutput) throws Exception {
		List<ChangeStore> changesToStore = new ArrayList<ChangeStore>();
		Image targetImage = images.get(0);
		Image sourceImage = images.get(1);
		ChangeStore changeToStore = null;
		//Coordinate[] coords = images.get(0).getWKTGeometry().getCoordinates();

		//*** Take the real DBScan result and return its polygons as changes
		ArrayList<String> geometriesArrayList = new ArrayList<>();
		String line = "";
		try (BufferedReader br = new BufferedReader(new FileReader(finalOutput))) {
			while ((line = br.readLine()) != null) {
				geometriesArrayList.add(line);
		    }
		}
		System.out.println("Number Of Lines in geometriesArrayList: " + geometriesArrayList.size());		
		
		if (geometriesArrayList.size() == 0) {
			// If we have an empty DBScan-output, then we make 3 dummy for posing!
			Random random = new Random();
			Envelope env = imageData.getArea().getEnvelopeInternal();
			int nOfChanges = 0;
			while (nOfChanges < 3) {
				Coordinate[] newPolygonCoords = new Coordinate[imageData.getArea().getCoordinates().length];
				Coordinate newLeftUp = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
				Coordinate newRightDown = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
				newPolygonCoords[0] = newLeftUp;
				newPolygonCoords[1] = new Coordinate(newRightDown.x,newLeftUp.y);
				newPolygonCoords[2] = newRightDown;
				newPolygonCoords[3] = new Coordinate(newLeftUp.x,newRightDown.y);
				newPolygonCoords[imageData.getArea().getCoordinates().length-1] = newPolygonCoords[0];
				GeometryFactory f = new GeometryFactory();
//				System.out.println(f.createPolygon(newPolygonCoords));	//Uncomment to see the dummy-response polygons.
				Geometry newg = f.createPolygon(newPolygonCoords);
				nOfChanges++;		
				Area area = new Area("test area" + nOfChanges, newg, IdRetrieval.getId(false));
				changeToStore = new ChangeStore(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area);
				changesToStore.add(changeToStore);				
			}
			System.out.println("Some changes returned");
			return changesToStore;
			
		}
		else {
			WKTReader wktReader = new WKTReader();		
			Geometry geometries;
			Area area;
			for (int i = 0; i < geometriesArrayList.size(); i++)
			{
				geometries = wktReader.read(geometriesArrayList.get(i));
				area = new Area(("ChangedArea" + i), geometries, IdRetrieval.getId(false));
				changeToStore = new ChangeStore(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area);
				changesToStore.add(changeToStore);
			}
			
			System.out.println("Real changes returned");
			return changesToStore;			
		}
	}

}
