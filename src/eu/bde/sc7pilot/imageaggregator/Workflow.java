package eu.bde.sc7pilot.imageaggregator;

import java.io.File;
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
			String outputDirectory=System.getProperty("user.home")+"/images/";
			File dir=new File(outputDirectory);
					if(!dir.exists()){
						dir.mkdir();
					}
						
			System.out.println(dir.getPath());
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
			RunChangeDetector ch=new RunChangeDetector("test.sh");
			ch.runchangeDetector();
			//storageWorkflow.storeChanges(changes);
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
