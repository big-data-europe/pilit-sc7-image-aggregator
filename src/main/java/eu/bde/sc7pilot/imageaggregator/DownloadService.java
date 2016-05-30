package eu.bde.sc7pilot.imageaggregator;

import java.util.List;
import model.Image;

/**
 *
 * @author efi
 */

public class DownloadService {

    private final static String USERNAME = "efaki";
    private final static String PASSWORD = "testapp";

    public void downloadImages(List<Image> images, String outputDirectory) {
        DataClient imageService = new DataClient(USERNAME, PASSWORD);
        for (Image image : images) {
            String imageName = image.getName();
            try {
                imageService.downloadAndSaveById(image.getId(), outputDirectory + imageName + ".zip");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("All images have been saved.");
    }
}
