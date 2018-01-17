package eu.bde.sc7pilot.imageaggregator;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.NotAuthorizedException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;

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
	
	public String runWorkflow(ImageData imageData, ReplaySubject<String> subject) {
		try {
//			String outputDirectory = "/snap/";
			String outputDirectory = "/media/indiana/data/imgs/";
			SearchService searchService = new SearchService(imageData.getUsername(), imageData.getPassword());
			DownloadService downloadService = new DownloadService(imageData.getUsername(), imageData.getPassword());
			subject.onNext("Searching for images...");
			List<Image> images = searchService.searchImages(imageData);
			
			if(images.size() <= 1) {
				subject.onNext("No suitable images were found for the specified parameters.");
				subject.onCompleted();
				return "ok";
			}
			
			IAutils.infoImages(images);
			
			//Name-processing of the to-be-downloaded images
		    String img1name = images.get(0).getName();
		    String img2name = images.get(1).getName();
		    String img3name = images.get(2).getName();
		    String img4name = images.get(3).getName();
			String img1 = img1name + ".zip";
			String img2 = img2name + ".zip";
			String img3 = img3name + ".jpeg";
			String img4 = img4name + ".jpeg";
		    String img1cod = img1name.substring(img1name.length()-4);//last 4 characters of the image name
		    String img2cod = img2name.substring(img2name.length()-4);
		    String cdCode = img1cod + "vs" + img2cod;

		    subject.onNext("Downloading images...@@@" + img1name + "@@@" + img2name);
			downloadService.downloadImages(images, outputDirectory);
			
			File qlook1File = new File(outputDirectory + img3);
			File qlook2File = new File(outputDirectory + img4);
			
			// Downloading dem
			IAutils.downloadDem(imageData.getArea(), cdCode);
			
			//Preparing subseting
			subject.onNext("Performing subseting...");
			String polygonSelected = imageData.getArea().toString(); //.replace("(", "\\(");
			//polygonFixed = polygonFixed.replace(")", "\\)");
			System.out.println("\n\nUser's selected polygon is: " + polygonSelected);
			//Run Subset operator
			System.out.println("\n\n\tRunning Subset operator one time for each Sentinel1 image.");
			IAutils.runShellScript("/runsubset.sh", outputDirectory, img1, polygonSelected);
			IAutils.runShellScript("/runsubset.sh", outputDirectory, img2, polygonSelected);
		    
		    //Preparing change-detectioning
		    System.out.println("\n\n\tPerforming Change-Detection.");
		    subject.onNext("Performing Change-Detection...");
			String sub1dim = outputDirectory + "subset_of_" + img1name + ".dim";
			String sub1tif = outputDirectory + "subset_of_" + img1name + ".tif";
			String sub2dim = outputDirectory + "subset_of_" + img2name + ".dim";
			String sub2tif = outputDirectory + "subset_of_" + img2name + ".tif";
			//Run change detection
			IAutils.runShellScript("/runchangedet.sh", sub1dim, sub1tif, sub2dim, sub2tif);

			//Preparing DBScaning
	        System.out.println("\n\n\tPerforming DBScan.");
	        subject.onNext("Performing DBScan...");
			String dbSCANoutput = img1cod + "vs" + img2cod + "coords.txt";
			//Run DBScan	    
			IAutils.runShellScript("/rundbscan.sh", outputDirectory, "SparkChangeDetResult.dim", dbSCANoutput);
			String dbSCANoutputFilepath = outputDirectory + dbSCANoutput;

			// Processing the DBScan's output with polygons defining possible changes
			RandomTestDetection changeDetection = new RandomTestDetection();
			
			//Storing to Strabon through Geotriples
			System.out.println("\n\tStoring results...");
			subject.onNext("Storing results...");
			List<ChangeStore> changesToStore = changeDetection.detectChangesForStore(images, imageData, dbSCANoutputFilepath);
			GeotriplesClient client = new GeotriplesClient("http://geotriples", "8080");
			client.saveChanges(changesToStore);

			// Visualizing Polygons with changes to Sextant
			System.out.println("\n\n\tVisualizing results...");
			List<Change> changes = changeDetection.detectChanges(images, imageData, dbSCANoutputFilepath);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JodaModule());
			objectMapper.registerModule(new JtsModule());			
			objectMapper.setConfig(objectMapper.getSerializationConfig().withView(Views.Public.class));
			objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
			String res = objectMapper.writerWithView(Views.Public.class).writeValueAsString(changes);
			//Create .json and return json. qlook1FileDest qlook2FileDest
			JSONObject responseJSON = new JSONObject();
			responseJSON.put("changeset", res);
			JSONArray imagesList = new JSONArray();
			JSONObject img1JSON = new JSONObject();
			JSONObject img2JSON = new JSONObject();
			img1JSON.put("url", qlook1File.getAbsolutePath());
			img1JSON.put("extent", images.get(2).getFootPrint());
			img2JSON.put("url", qlook2File.getAbsolutePath());
			img2JSON.put("extent", images.get(3).getFootPrint());
			imagesList.add(img1JSON);
			imagesList.add(img2JSON);
			responseJSON.put("images", imagesList);
//			System.out.println("\tJsonResponse to be send to Sextant:\n" + responseJSON.toString() + "\n\t...end of response.");
			subject.onNext(responseJSON.toString());	
			subject.onNext("Session Completed!");
			subject.onCompleted();
			System.out.println("\n\tSession Completed!");
//			if (img1Handler.getUnzipFile().exists()) {
//				try {
//					FileUtils.deleteDirectory(img1Handler.getUnzipFile());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.out.println(img1Handler.getUnzipFile().getName() + " deleted succesfully!\n");
//			}
//			else {
//				System.out.println("No file: " + img1Handler.getUnzipFile().getName() + " found.\n");
//			}
//			if (img2Handler.getUnzipFile().exists()) {
//				try {
//					FileUtils.deleteDirectory(img2Handler.getUnzipFile());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.out.println(img1Handler.getUnzipFile().getName() + " deleted succesfully!\n");
//			}
//			else {
//				System.out.println("No file: " + img1Handler.getUnzipFile().getName() + " found.\n");
//			}
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
