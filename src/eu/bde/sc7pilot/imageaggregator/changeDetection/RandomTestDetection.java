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
			//for(int i=0;i<2;i++){
			Coordinate newLeftUp=new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
			Coordinate newRightDown=new Coordinate((double)(env.getMinX()+random.nextDouble()*(env.getMaxX()-env.getMinX())),env.getMinY()+random.nextDouble()*(env.getMaxY()-env.getMinY()));
//			double d=random.nextDouble();
//			System.out.println(d);
//			System.out.println((double)(rightUp.x-leftUp.x));
//			System.out.println((double)((double)leftUp.x+d*(double)(rightUp.x-leftUp.x)));
				
			newPolygonCoords[0] = newLeftUp;
			newPolygonCoords[1] = new Coordinate(newRightDown.x,newLeftUp.y);
			newPolygonCoords[2] = newRightDown;
			newPolygonCoords[3] = new Coordinate(newLeftUp.x,newRightDown.y);
		//}
			newPolygonCoords[imageData.getArea().getCoordinates().length-1] = newPolygonCoords[0];
			GeometryFactory f = new GeometryFactory();
			System.out.println(f.createPolygon(newPolygonCoords));
			Geometry newg = f.createPolygon(newPolygonCoords);
			nOfChanges++;		
			Area area = new Area("test area" + nOfChanges, newg, IdRetrieval.getId(false));
			Change change=new Change(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area);
			changes.add(change);
			}
//			WKTReader reader=new WKTReader();
//			Geometry geometry1 = reader.read("POLYGON((12.24151611328125 51.392693633076206,12.48046875 51.392693633076206,"
//					+ "12.48046875 51.27600606574848,12.24151611328125 51.27600606574848,12.24151611328125 51.392693633076206))");
//			Geometry geometry2 = reader.read("POLYGON((8.652763366699219 50.128326033656,8.7506103515625 50.128326033656,"
//					+ "8.7506103515625 50.07613532273157,8.652763366699219 50.07613532273157,8.652763366699219 50.128326033656))");
//			Geometry geometry3 = reader.read("POLYGON((11.480712890625 48.19007766272618,11.68670654296875 48.19007766272618,"
//					+ "11.68670654296875 48.073674658823236,11.480712890625 48.073674658823236,11.480712890625 48.19007766272618))");
//			Geometry geometry4 = reader.read("POLYGON((11.7938232421875 53.11776792873197,13.9141845703125 53.11776792873197"
//					+ ",13.9141845703125 51.51284501865027,11.7938232421875 51.51284501865027,11.7938232421875 53.11776792873197))");
//			Area area1 = new Area("Leipzig",geometry1,IdRetrieval.getId(false));
//			Area area2 = new Area("Frankfurt",geometry2,IdRetrieval.getId(false));
//			Area area3 = new Area("Munich",geometry3,IdRetrieval.getId(false));
//			Area area4 = new Area("Berlin",geometry4,IdRetrieval.getId(false));
//			Change change1=new Change(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area1);
//			Change change2=new Change(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area2);
//			Change change3=new Change(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area3);
//			Change change4=new Change(IdRetrieval.getId(true),sourceImage.getDate(),targetImage.getDate(),area4);
//			System.out.println("source "+change1.getSourceDate());
//			System.out.println("target "+change1.getTargetDate());
//			System.out.println(sourceImage.getDate());
//			changes.add(change1);
//			changes.add(change2);
//			changes.add(change3);
//			changes.add(change4);
			//System.out.println(change.getId()+" "+geometryFactory.createPolygon(newPolygonCoords).within(geometry));
		//}
		return changes;
	}

}
