package eu.bde.sc7pilot.imageaggregator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.NotAuthorizedException;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.bde.sc7pilot.imageaggregator.changeDetection.ChangeDetection;
import eu.bde.sc7pilot.imageaggregator.changeDetection.RandomTestDetection;
import eu.bde.sc7pilot.imageaggregator.changeDetection.RunChangeDetector;
import eu.bde.sc7pilot.imageaggregator.changeDetection.RunDBscan;
import eu.bde.sc7pilot.imageaggregator.changeDetection.RunSubset;
import eu.bde.sc7pilot.imageaggregator.model.Change;
import eu.bde.sc7pilot.imageaggregator.model.ChangeStore;
import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import eu.bde.sc7pilot.imageaggregator.utils.GeotriplesClient;
import eu.bde.sc7pilot.imageaggregator.utils.Sentinel1ImagesHandler;
import eu.bde.sc7pilot.imageaggregator.utils.Views;
import rx.Observable;
import rx.subjects.ReplaySubject;

public class Workflow {
	
	public String runWorkflow(ImageData imageData,ReplaySubject<String> subject) {
		try {
			String outputDirectory = "/snap/";		
			SearchService searchService = new SearchService(imageData.getUsername(),imageData.getPassword());
			DownloadService downloadService = new DownloadService(imageData.getUsername(),imageData.getPassword());
			subject.onNext("Searching for images...");
			List<Image> images = searchService.searchImages(imageData);
			
			if(images.size() <= 1)
			{
				subject.onNext("No images were found for the specified parameters.");
				subject.onCompleted();
				return "ok";
			}
		    String img1name = images.get(0).getName();
		    String img2name = images.get(1).getName();
		    
		    subject.onNext("Downloading images...@@@" + img1name + "@@@" + img2name);
			System.out.println("Downloading images...@@@" + img1name + "@@@" + img2name);
			downloadService.downloadImages(images, outputDirectory);
			
			//Name-processing of the downloaded images
			String img1 = img1name + ".zip";
			String img2 = img2name + ".zip";
		    String img1cod = img1name.substring(img1name.length()-4);//last 4 characters of the image name
		    String img2cod = img2name.substring(img2name.length()-4);
			System.out.println("\nThe first img's filepath is: " + outputDirectory + img1);
			System.out.println("The second img's filepath is: " + outputDirectory + img2);

			
			//Handling the downloaded images
			Sentinel1ImagesHandler img1Handler = new Sentinel1ImagesHandler(img1);
			Sentinel1ImagesHandler img2Handler = new Sentinel1ImagesHandler(img2);
			String qlook1 = img1Handler.findQuickLook();
			String qlook2 = img2Handler.findQuickLook();
			File qlook1File = new File(qlook1);
			File qlook2File = new File(qlook2);
			File qlook1FileDest = new File(outputDirectory + img1cod + "qlook.png");
			File qlook2FileDest = new File(outputDirectory + img2cod + "qlook.png");
			FileUtils.copyFile(qlook1File, qlook1FileDest);
			FileUtils.copyFile(qlook2File, qlook2FileDest);
			
			
			//Preparing subseting
			subject.onNext("Performing subseting...");
			String polygonSelected = imageData.getArea().toString(); //.replace("(", "\\(");
			//polygonFixed = polygonFixed.replace(")", "\\)");
			System.out.println("User's selected polygon is: " + polygonSelected);
			//Run Subset operator
			System.out.println("\n\n\tRunning Subset operator 2 times for the 2 images.");
			RunSubset subsetOp1 = new RunSubset("/runsubset.sh", outputDirectory, img1, polygonSelected);
		    String resultSubsetOp1 = subsetOp1.runSubset();
		    RunSubset subsetOp2 = new RunSubset("/runsubset.sh", outputDirectory, img2, polygonSelected);
		    String resultSubsetOp2 = subsetOp2.runSubset();
		    
		    //Preparing change-detectioning
		    System.out.println("\n\n\tPerforming Change-Detection.");
		    subject.onNext("Performing Change-Detection...");
			String sub1dim = outputDirectory + "subset_of_" + img1name + ".dim";
			String sub1tif = outputDirectory + "subset_of_" + img1name + ".tif";
			String sub2dim = outputDirectory + "subset_of_" + img2name + ".dim";
			String sub2tif = outputDirectory + "subset_of_" + img2name + ".tif";
			//Run change detection
			RunChangeDetector runCD = new RunChangeDetector("/runchangedet.sh", sub1dim, sub1tif, sub2dim, sub2tif);
	        String resultCD = runCD.runchangeDetector();
			//uncomment the next line to see the output of the shell script
			//subject.onNext(result.substring(0, 20));

			//Preparing DBScaning
	        System.out.println("\n\n\tPerforming DBScan.");
	        subject.onNext("Performing DBScan...");
			String dbSCANoutput = img1cod + "vs" + img2cod + "coords.txt";
			//Run DBScan	    
			RunDBscan runDBS = new RunDBscan("/rundbscan.sh", outputDirectory, "SparkChangeDetResult.dim", dbSCANoutput);
			String resultDBS = runDBS.runDBscan();
			String dbSCANoutputFilepath = outputDirectory + dbSCANoutput;

			// Processing the DBScan's output with polygons defining possible changes
			ChangeDetection changeDetection = new RandomTestDetection();
			
			//Storing to Strabon through Geotriples
			System.out.println("\n\tStoring results...");
			subject.onNext("Storing results...");
			List<ChangeStore> changesToStore = changeDetection.detectChangesForStore(images, imageData, dbSCANoutputFilepath);
			GeotriplesClient client = new GeotriplesClient("http://geotriples","8080");
			client.saveChanges(changesToStore);

			// Visualizing Polygons with changes to Sextant			
			List<Change> changes = changeDetection.detectChanges(images, imageData, dbSCANoutputFilepath);
			System.out.println("\n\tVisualizing results...");
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
			img1JSON.put("url", qlook1FileDest.getAbsolutePath());
			img1JSON.put("extent", img1name);
			img2JSON.put("url", qlook1FileDest.getAbsolutePath());
			img2JSON.put("extent", img2name);
			imagesList.add(img1JSON);
			imagesList.add(img2JSON);
			responseJSON.put("images", imagesList);
			System.out.println("\tJsonResponse to be send to Sextant:\n" + responseJSON + "\n\t...end of response.");
			subject.onNext(res);	
			subject.onNext("Session Completed!");
			subject.onCompleted();
			System.out.println("\n\tSession Completed!");
			if (img1Handler.getUnzipFile().exists()) {
				try {
					FileUtils.deleteDirectory(img1Handler.getUnzipFile());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(img1Handler.getUnzipFile().getName() + " deleted succesfully!\n");
			}
			else {
				System.out.println("No file: " + img1Handler.getUnzipFile().getName() + " found.\n");
			}
			if (img2Handler.getUnzipFile().exists()) {
				try {
					FileUtils.deleteDirectory(img2Handler.getUnzipFile());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(img1Handler.getUnzipFile().getName() + " deleted succesfully!\n");
			}
			else {
				System.out.println("No file: " + img1Handler.getUnzipFile().getName() + " found.\n");
			}
			return "ok";
			}
		catch (NotAuthorizedException e) {
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
		detectChangesAsync(imageData, subject).handle((ok, ex) -> {
			if (ok != null) {
				return ok;
				}
			else {
				return ex.getMessage();
				}
			});
		return subject;
		}
	
	private CompletableFuture<String> detectChangesAsync(ImageData imageData, ReplaySubject<String> subject) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		return CompletableFuture.supplyAsync(()->runWorkflow(imageData,subject), executor);
		}
	}
