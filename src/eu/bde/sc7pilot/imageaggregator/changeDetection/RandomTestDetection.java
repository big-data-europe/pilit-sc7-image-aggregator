package eu.bde.sc7pilot.imageaggregator.changeDetection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import eu.bde.sc7pilot.imageaggregator.model.Area;
import eu.bde.sc7pilot.imageaggregator.model.Change;
import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import eu.bde.sc7pilot.imageaggregator.utils.IdRetrieval;

public class RandomTestDetection implements ChangeDetection{

	@Override
	public List<Change> detectChanges(List<Image> images,ImageData imageData) throws Exception {
		List<Change> changes = new ArrayList<Change>();
		Image targetImage = images.get(0);
		Image sourceImage = images.get(1);
		System.out.println(targetImage.getDate());
		System.out.println(sourceImage.getDate());
		Random random = new Random();
		Coordinate[] coords = images.get(0).getWKTGeometry().getCoordinates();
		Envelope env = imageData.getArea().getEnvelopeInternal();
		int nOfChanges=0;
		while(nOfChanges<3){
			Coordinate[] newPolygonCoords = new Coordinate[imageData.getArea().getCoordinates().length];
			Coordinate newLeftUp = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
			Coordinate newRightDown = new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
			newPolygonCoords[0] = newLeftUp;
			newPolygonCoords[1] = new Coordinate(newRightDown.x,newLeftUp.y);
			newPolygonCoords[2] = newRightDown;
			newPolygonCoords[3] = new Coordinate(newLeftUp.x,newRightDown.y);
			newPolygonCoords[imageData.getArea().getCoordinates().length-1] = newPolygonCoords[0];
			GeometryFactory f = new GeometryFactory();
			System.out.println(f.createPolygon(newPolygonCoords));
			Geometry newg = f.createPolygon(newPolygonCoords);
			nOfChanges++;		
			Area area = new Area("test area" + nOfChanges, newg, IdRetrieval.getId(false));
			Change change = new Change(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area);
			changes.add(change);
			}
		
		return changes;
	}

}
