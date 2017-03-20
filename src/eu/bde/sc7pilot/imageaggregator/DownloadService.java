package eu.bde.sc7pilot.imageaggregator;

import java.util.List;

import eu.bde.sc7pilot.imageaggregator.model.Image;

/**
 *
 * @author efi
 */

public class DownloadService {

	private String username;
	private String password;
	
	public DownloadService(String username,String password) {
		this.username=username;
		this.password=password;
	}
	
    public void downloadImages(List<Image> images, String outputDirectory) {
    	System.out.println("images to download: " + images.size());
        DataClient imageService = new DataClient(username, password);
        for (int i = 0; i < images.size(); i++) {
        	Image image = images.get(i);
            String imageName = image.getName();
            try {
            	long startDownl = System.currentTimeMillis();
                imageService.downloadAndSaveById(image.getId(), outputDirectory + imageName + ".zip");
                long endDownl = System.currentTimeMillis();
                long downlTime = (endDownl - startDownl)/60000;
                System.out.println(downlTime + " mins for Downloading and saving Image No.: " + i);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }   
    }
    
}
