package eu.bde.sc7pilot.imageaggregator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.NotAuthorizedException;

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
			//subject.onNext("Downloading images...");
			//downloadService.downloadImages(images, outputDirectory);
			
			//Name-processing of the downloaded images
//		    String img1name = images.get(0).getName(); // general case
//		    String img2name = images.get(1).getName(); // general case
		    String img1name = "S1A_IW_GRDH_1SDV_20160129T153207_20160129T153232_009711_00E2D2_2F5A"; // hardcoded case
		    String img2name = "S1A_IW_GRDH_1SDV_20161031T153214_20161031T153239_013736_0160B3_82A5"; // hardcoded case
		    subject.onNext("Downloading images...@@@" + img1name + "@@@" + img2name);
//		    System.out.println("Going to sleep for 20secs");	// hardcoded WEBINAR
//			Thread.sleep(20000);								// hardcoded WEBINAR
//			System.out.println("I AM AWAKE!");					// hardcoded WEBINAR

			String img1 = img1name + ".zip";
			String img2 = img2name + ".zip";
			System.out.println("The first img's filepath is:" + outputDirectory + img1);
			System.out.println("The second img's filepath is:" + outputDirectory + img2);
			
			//Preparing subseting
			System.out.println("Performing subseting...");
			String polygonSelected = imageData.getArea().toString(); //.replace("(", "\\(");
			//polygonFixed = polygonFixed.replace(")", "\\)");
			System.out.println("Polygon for SubsetOp: " + polygonSelected);
			//Run Subset operator
			//System.out.println("Running Subset operator...");
			subject.onNext("Performing subseting...");
		    System.out.println("Going to sleep for 10secs");
			Thread.sleep(10000);
			System.out.println("I AM AWAKE!");

//			RunSubset subsetOp1 = new RunSubset("/runsubset.sh", outputDirectory, img1, polygonSelected);	// hardcoded WEBINAR
//		    String resultSubsetOp1 = subsetOp1.runSubset();													// hardcoded WEBINAR
		    //subject.onNext("Performing subseting on 2nd image...");
//		    RunSubset subsetOp2 = new RunSubset("/runsubset.sh", outputDirectory, img2, polygonSelected);	// hardcoded WEBINAR
//		    String resultSubsetOp2 = subsetOp2.runSubset();													// hardcoded WEBINAR
		    
		    //Preparing change-detectioning
		    System.out.println("performing Change-Detection...");
		    subject.onNext("Performing Change-Detection...");
		    System.out.println("Going to sleep for 10secs");	// hardcoded WEBINAR
			Thread.sleep(10000);								// hardcoded WEBINAR
			System.out.println("I AM AWAKE!");					// hardcoded WEBINAR

			String sub1dim = outputDirectory + "subset_of_" + img1name + ".dim";
			String sub1tif = outputDirectory + "subset_of_" + img1name + ".tif";
			String sub2dim = outputDirectory + "subset_of_" + img2name + ".dim";
			String sub2tif = outputDirectory + "subset_of_" + img2name + ".tif";
			//Run change detection
//			RunChangeDetector runCD = new RunChangeDetector("/runchangedet.sh", sub1dim, sub1tif, sub2dim, sub2tif);	// hardcoded WEBINAR
//	        String resultCD = runCD.runchangeDetector();																// hardcoded WEBINAR
			//uncomment the next line to see the output of the shell script
			//subject.onNext(result.substring(0, 20));

			//Preparing DBScaning
	        System.out.println("performing DBScan...");
		    subject.onNext("Performing DBScan...");
		    System.out.println("Going to sleep for 10secs");	// hardcoded WEBINAR
			Thread.sleep(10000);								// hardcoded WEBINAR
			System.out.println("I AM AWAKE!");					// hardcoded WEBINAR

		    String img1cod = img1name.substring(img1name.length()-4);//last 4 characters of the image name
		    String img2cod = img2name.substring(img2name.length()-4);
			String dbSCANoutput = img1cod + "vs" + img2cod + "coords.txt";
			//Run DBScan	    
//			RunDBscan runDBS = new RunDBscan("/rundbscan.sh", outputDirectory, "SparkChangeDetResult.dim", dbSCANoutput);	// hardcoded WEBINAR
//			String resultDBS = runDBS.runDBscan();																			// hardcoded WEBINAR
//			String dbSCANoutputFilepath = outputDirectory + dbSCANoutput; // general case	// hardcoded WEBINAR
			String dbSCANoutputFilepath = "/snap/2F5Avs82A5coords.txt"; // hardcoded case	// hardcoded WEBINAR
			
			// Processing the DBScan's output with polygons defining possible changes
			ChangeDetection changeDetection = new RandomTestDetection();			
			
			//Storing to Strabon through Geotriples
			System.out.println("Storing results...");
		    subject.onNext("Storing results...");
		    System.out.println("Going to sleep for 3secs");		// hardcoded WEBINAR
			Thread.sleep(3000);									// hardcoded WEBINAR
			System.out.println("I AM AWAKE!");					// hardcoded WEBINAR

			List<ChangeStore> changesToStore = changeDetection.detectChangesForStore(images, imageData, dbSCANoutputFilepath);
			GeotriplesClient client = new GeotriplesClient("http://geotriples","8080");
			client.saveChanges(changesToStore);
			
			// Visualizing Polygons with changes to Sextant
			List<Change> changes = changeDetection.detectChanges(images, imageData, dbSCANoutputFilepath);
			System.out.println("Visualizing results...");
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JodaModule());
			objectMapper.registerModule(new JtsModule());			
			objectMapper.setConfig(objectMapper.getSerializationConfig().withView(Views.Public.class));
			objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
			String res = objectMapper.writerWithView(Views.Public.class).writeValueAsString(changes);
			subject.onNext(res);
			subject.onNext("Session Completed!");
			subject.onCompleted();
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
