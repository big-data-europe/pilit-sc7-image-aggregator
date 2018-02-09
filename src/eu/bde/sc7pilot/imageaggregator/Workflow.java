package eu.bde.sc7pilot.imageaggregator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.NotAuthorizedException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import eu.bde.sc7pilot.imageaggregator.changeDetection.RandomTestDetection;
import eu.bde.sc7pilot.imageaggregator.model.Change;
import eu.bde.sc7pilot.imageaggregator.model.ChangeStore;
import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import eu.bde.sc7pilot.imageaggregator.utils.GeotriplesClient;
import eu.bde.sc7pilot.imageaggregator.utils.IAutils;
import eu.bde.sc7pilot.imageaggregator.utils.Views;
import rx.Observable;
import rx.subjects.ReplaySubject;

public class Workflow {
	
	private final static String IMG_DIR_FILEPATH = "/allImages";
	private final static String DOWNL_DEM_SH = "/downlDem.sh";
	private final static String PRE_PROCESS_SH = "/runImgPreProcess.sh";
	private final static String TERRAIN_CORRECT_SH = "/runTerrainCorrection.sh";
	private final static String CHANGE_DET_SH = "/runChangeDetection.sh";
	private final static String DBSCAN_SH = "/runPyDBScan.sh";
	private final static String HC_PROPERTIES = "/properties.txt";
	
	public String runWorkflow(ImageData imageData, ReplaySubject<String> subject) {
		try {
//			SearchService searchService = new SearchService(imageData.getUsername(), imageData.getPassword());
//			DownloadService downloadService = new DownloadService(imageData.getUsername(), imageData.getPassword());
			subject.onNext("Searching for images...");
//			List<Image> images = searchService.searchImages(imageData);
//			
//			if(images.size() <= 1) {
//				subject.onNext("No suitable images were found for the specified parameters.");
//				subject.onCompleted();
//				return "ok";
//			}
//			IAutils.infoImages(images);
			
			// Read img-names and selected polygon
			List<String> lines = new ArrayList<>();
			File propertiesFilePath = new File(HC_PROPERTIES);
			LineIterator lineIterator = FileUtils.lineIterator(propertiesFilePath);
			while (lineIterator.hasNext()) {
				String line = lineIterator.next().trim();
				lines.add(line);
			}
			
			//Name-processing of the to-be-downloaded images
			for(int i = 0; i < lines.size(); i++) {
				System.out.println(lines.get(i));
			}
			System.out.println(lines.get(0));
		    String img1name = lines.get(0);
		    String img2name = lines.get(1);
//		    String img3name = images.get(2).getName();
//		    String img4name = images.get(3).getName();
			String img1 = img1name + ".zip";
			String img2 = img2name + ".zip";
//			String img3 = img3name + ".jpeg";
//			String img4 = img4name + ".jpeg";
		    String img1code = img1name.substring(img1name.length()-4);//last 4 characters of the image name
		    String img2code = img2name.substring(img2name.length()-4);
		    String cdCode = img1code + "vs" + img2code;
		    WKTReader wkt = new WKTReader();
		    Geometry selectedArea = wkt.read(lines.get(2));
		    
		    // Downloading images
		    subject.onNext("Downloading images...@@@" + img1name + "@@@" + img2name);
//			downloadService.downloadImages(images, IMG_DIR_FILEPATH);
//			
//			File qlook1File = new File(IMG_DIR_FILEPATH + File.separator + img3);
//			File qlook2File = new File(IMG_DIR_FILEPATH + File.separator + img4);
			
			// Downloading dem
			String demFileName = "dem" + cdCode + ".tif";
			String demFilePath = IAutils.downloadDem(DOWNL_DEM_SH, selectedArea, demFileName, IMG_DIR_FILEPATH);
			
			// Pre-processing SENTINEL-1 images.
			subject.onNext("Performing subseting...");
			System.out.println("\n\nUser's selected polygon is: " + selectedArea.toString());
			System.out.println("\n\n\tRunning Subset and Calibration for each SENTINEL-1 image.");
			String preprocImg1Name = "subsetCalib" + img1code;
			String preprocImg2Name = "subsetCalib" + img2code;
			IAutils.runShellScript(PRE_PROCESS_SH, IMG_DIR_FILEPATH + File.separator + img1, preprocImg1Name, selectedArea.toString());
			IAutils.runShellScript(PRE_PROCESS_SH, IMG_DIR_FILEPATH + File.separator + img2, preprocImg2Name, selectedArea.toString());
		    
			// Applying Terrain Correction to pre-processed images.
		    System.out.println("\n\n\tPerforming Terrain Correction...");
		    String img1TCinput = IMG_DIR_FILEPATH + File.separator + preprocImg1Name + ".dim";
		    String img2TCinput = IMG_DIR_FILEPATH + File.separator + preprocImg2Name + ".dim";
		    String tcResult1FilePath = IMG_DIR_FILEPATH + File.separator + "tc" + img1code + ".tif";
		    String tcResult2FilePath = IMG_DIR_FILEPATH + File.separator + "tc" + img2code + ".tif";
			IAutils.applyTerrainCorrection(TERRAIN_CORRECT_SH, img1TCinput, demFilePath, tcResult1FilePath, selectedArea);
			IAutils.applyTerrainCorrection(TERRAIN_CORRECT_SH, img2TCinput, demFilePath, tcResult2FilePath, selectedArea);
			
		    // Applying ChangeDetection to TerrainCorrected images.
		    System.out.println("\n\n\tPerforming Change-Detection.");
		    subject.onNext("Performing Change-Detection...");
		    String cdImgFilePath = IMG_DIR_FILEPATH + File.separator + "cd" + cdCode + ".tif";
			IAutils.runShellScript(CHANGE_DET_SH, tcResult1FilePath, tcResult2FilePath, cdImgFilePath);

			// Applying DBScan
	        System.out.println("\n\n\tPerforming DBScan.");
	        subject.onNext("Performing DBScan...");
			String dbSCANoutputName = "cl" + cdCode + ".txt";
			//Run DBScan ...to be completed when   
			IAutils.runShellScript(DBSCAN_SH, cdImgFilePath, dbSCANoutputName);

			// Processing the DBScan's output with polygons defining possible changes
			RandomTestDetection changeDetection = new RandomTestDetection();
			
			//Storing to Strabon through Geotriples
//			System.out.println("\n\tStoring results...");
//			subject.onNext("Storing results...");
//			List<ChangeStore> changesToStore = changeDetection.detectChangesForStore(images, imageData, dbSCANoutputFilepath);
//			GeotriplesClient client = new GeotriplesClient("http://geotriples", "8080");
//			client.saveChanges(changesToStore);

			// Visualizing Polygons with changes to Sextant
//			System.out.println("\n\n\tVisualizing results...");
//			List<Change> changes = changeDetection.detectChanges(images, imageData, IMG_DIR_FILEPATH + File.separator + dbSCANoutputName);
//			ObjectMapper objectMapper = new ObjectMapper();
//			objectMapper.registerModule(new JodaModule());
//			objectMapper.registerModule(new JtsModule());			
//			objectMapper.setConfig(objectMapper.getSerializationConfig().withView(Views.Public.class));
//			objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
//			String res = objectMapper.writerWithView(Views.Public.class).writeValueAsString(changes);
//			//Create .json and return json. qlook1FileDest qlook2FileDest
//			JSONObject responseJSON = new JSONObject();
//			responseJSON.put("changeset", res);
//			JSONArray imagesList = new JSONArray();
//			JSONObject img1JSON = new JSONObject();
//			JSONObject img2JSON = new JSONObject();
//			img1JSON.put("url", qlook1File.getAbsolutePath());
//			img1JSON.put("extent", images.get(2).getFootPrint());
//			img2JSON.put("url", qlook2File.getAbsolutePath());
//			img2JSON.put("extent", images.get(3).getFootPrint());
//			imagesList.add(img1JSON);
//			imagesList.add(img2JSON);
//			responseJSON.put("images", imagesList);
////			System.out.println("\tJsonResponse to be send to Sextant:\n" + responseJSON.toString() + "\n\t...end of response.");
//			subject.onNext(responseJSON.toString());	
			subject.onNext("Session Completed!");
			subject.onCompleted();
			System.out.println("\n\tSession Completed!");
			return "ok";
		 } catch (NotAuthorizedException e) {
		        subject.onError(e);
		        return "error";
		    }
			 catch (Exception e) {
		        subject.onError(e);
		        return "error";
		    }
	}

	public Observable<String> downloadImages(ImageData imageData) throws Exception {
		final ReplaySubject<String> subject = ReplaySubject.create();
		detectChangesAsync(imageData,subject).handle((ok, ex) -> {
			if (ok != null) {
				return ok;
			}
			else {
				return ex.getMessage();
			}
		});
		return subject;
	}
	
	private CompletableFuture<String> detectChangesAsync(ImageData imageData,ReplaySubject<String> subject) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		return CompletableFuture.supplyAsync(()->runWorkflow(imageData,subject),executor);
	}

}
