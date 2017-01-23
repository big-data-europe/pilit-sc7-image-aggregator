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
			
			if(images.size()<=1)
			{
				subject.onNext("No images were found for the specified parameters.");
				subject.onCompleted();
				return "ok";
			}
			//subject.onNext("Downloading images...");
			//downloadService.downloadImages(images, outputDirectory);
			
			//Getting the local filepath's of the downloaded images
//			String img1 = outputDirectory + images.get(0).getName() + ".zip";
//			String img2 = outputDirectory + images.get(1).getName() + ".zip";
			subject.onNext("Already downloaded...");
			String img1 = "S1A_IW_GRDH_1SSV_20141225T142407_20141225T142436_003877_004A54_040F.zip";
			String img2 = "S1A_IW_GRDH_1SSV_20150518T142409_20150518T142438_005977_007B49_AF76.zip";
			System.out.println("The first img's filepath is:" + outputDirectory + img1);
			System.out.println("The second img's filepath is:" + outputDirectory + img2);
			
			//Preparing subseting
			subject.onNext("Performing subseting...");
//		    String img1name = images.get(0).getName();
//		    String img2name = images.get(1).getName();
		    String img1name = "S1A_IW_GRDH_1SSV_20141225T142407_20141225T142436_003877_004A54_040F";
		    String img2name = "S1A_IW_GRDH_1SSV_20150518T142409_20150518T142438_005977_007B49_AF76";
			String polygonSelected = imageData.getArea().toString(); //.replace("(", "\\(");
			//polygonFixed = polygonFixed.replace(")", "\\)");
			System.out.println("PolygonSelected: " + polygonSelected);
			//Run Subset operator
			System.out.println("running Subset operator...");
			RunSubset subsetOp1 = new RunSubset("/runsubset.sh", outputDirectory, img1, polygonSelected);
		    //String resultSubsetOp1 = subsetOp1.runSubset();
		    RunSubset subsetOp2 = new RunSubset("/runsubset.sh", outputDirectory, img2, polygonSelected);
		    //String resultSubsetOp2 = subsetOp2.runSubset();
		    
		    //Preparing change-detectioning
		    subject.onNext("performing Change-Detection...");
			String sub1dim = outputDirectory + "subset_of_" + img1name + ".dim";
			String sub1tif = outputDirectory + "subset_of_" + img1name + ".tif";
			String sub2dim = outputDirectory + "subset_of_" + img2name + ".dim";
			String sub2tif = outputDirectory + "subset_of_" + img2name + ".tif";
			//Run change detection
			RunChangeDetector runCD = new RunChangeDetector("/runchangedet.sh", sub1dim, sub1tif, sub2dim, sub2tif);
	        //String resultCD = runCD.runchangeDetector();
			//uncomment the next line to see the output of the shell script
			//subject.onNext(result.substring(0, 20));

			//Preparing DBScaning
	        subject.onNext("performing DBScan...");
		    String img1cod = img1name.substring(img1name.length()-4);//last 4 characters of the image name
		    String img2cod = img2name.substring(img2name.length()-4);
			String dbSCANoutput = img1cod + "vs" + img2cod + "coords.txt";
			//Run DBScan	    
			RunDBscan runDBS = new RunDBscan("/rundbscan.sh", outputDirectory, "SparkChangeDetResult.dim", dbSCANoutput);
			//String resultDBS = runDBS.runDBscan();
			//String dbSCANoutputFilepath = outputDirectory + dbSCANoutput; //general case
			String dbSCANoutputFilepath = "/snap/0EE2vsECCCcoords.txt";
			
			// Processing the DBScan's output with polygons defining possible changes
			ChangeDetection changeDetection = new RandomTestDetection();
			List<Change> changes = changeDetection.detectChanges(images, imageData, dbSCANoutputFilepath);
			
			// Visualizing Polygons with chnages to Sextant
			System.out.println("Visualizing results...");
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JodaModule());
			objectMapper.registerModule(new JtsModule());			
			objectMapper.setConfig(objectMapper.getSerializationConfig().withView(Views.Public.class));
			objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
			String res = objectMapper.writerWithView(Views.Public.class).writeValueAsString(changes);
			subject.onNext(res);
			
			//Storing to Strabon through Geotriples
			System.out.println("Storing results...");
			List<ChangeStore> changesToStore = changeDetection.detectChangesForStore(images, imageData, dbSCANoutputFilepath);
			GeotriplesClient client = new GeotriplesClient("http://geotriples","8080");
			client.saveChanges(changes);
			
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
