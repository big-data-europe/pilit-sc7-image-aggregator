package eu.bde.sc7pilot.imageaggregator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.NotAuthorizedException;

import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import rx.Observable;
import rx.subjects.ReplaySubject;

public class Workflow {
public String runWorkflow(ImageData imageData,ReplaySubject<String> subject) {
	 try {
//			String outputDirectory=System.getProperty("user.home")+"/images/";
//			File dir=new File(outputDirectory);
//					if(!dir.exists()){
//						dir.mkdir();
//					}
		 	String outputDirectory="/images/";		
			SearchService searchService=new SearchService(imageData.getUsername(),imageData.getPassword());
			DownloadService downloadService=new DownloadService(imageData.getUsername(),imageData.getPassword());
			subject.onNext("Searching for images...");
			List<Image> images=searchService.searchImages(imageData);
			
			if(images.size()<=1)
			{
				subject.onNext("No images were found for the specified parameters.");
				subject.onCompleted();
				return "ok";
			}
			subject.onNext("Downloading images...");
			downloadService.downloadImages(images, outputDirectory);
			
			//Getting the local filepath's of the downloaded images
			String img1 = outputDirectory + images.get(0).getName();
			String img2 = outputDirectory + images.get(1).getName();
			System.out.println("The first img's filepath is:" + img1);
			System.out.println("The second img's filepath is:" + img2);
			
			//uncomment the next block to perform change detection
			subject.onNext("performing change detection...");
			RunChangeDetector ch=new RunChangeDetector("/runchangedet.sh", img1, img2);
			String result=ch.runchangeDetector();
			
			//uncomment the next line to see the output of the shell script
			//subject.onNext(result.substring(0, 20));
			
			subject.onNext("Change detection completed successfully.");
			subject.onCompleted();
			return "ok";
		 }catch (NotAuthorizedException e) {
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
	    } else {
	         return ex.getMessage();
	    }
	});
	return subject;
}
private CompletableFuture<String> detectChangesAsync(ImageData imageData,ReplaySubject<String> subject){
	ExecutorService executor = Executors.newFixedThreadPool(1);
    return CompletableFuture.supplyAsync(()->runWorkflow(imageData,subject),executor);
}
}