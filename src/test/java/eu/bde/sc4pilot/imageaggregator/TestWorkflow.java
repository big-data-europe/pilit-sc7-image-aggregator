package eu.bde.sc4pilot.imageaggregator;


import eu.bde.sc4pilot.imageaggregator.DataClient;
import eu.bde.sc4pilot.imageaggregator.Image;
import eu.bde.sc4pilot.imageaggregator.Query;
import eu.bde.sc4pilot.imageaggregator.QueryBuilder;
import eu.bde.sc4pilot.imageaggregator.SearchClient;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestWorkflow {

    private final static Logger LOGGER = Logger.getLogger(TestWorkflow.class.getName());

    // to be read from a properties file
    private final static int MAX_NO_OF_IMAGES = 2;
    private final static String USERNAME = "efaki";
    private final static String PASSWORD = "testapp";
    private final static String FOOTPRINT = "Intersects(POLYGON((53.788611135482 25.394578978013,53.810583791732 25.394578978013,53.810583791732 25.414426912966,53.788611135482 25.414426912966,53.788611135482 25.394578978013)))";
    //private final static String OUTPUT_DIRECTORY = "G:\\ImageProcessing\\sentinel-images2\\";
    private final static String OUTPUT_DIRECTORY = "/home/efi/SNAP/";

    public static void main(String[] args) {
        String beginPosition = "NOW-3MONTHS TO NOW-2MONTHS";
        Query query = new QueryBuilder().setBeginPosition(beginPosition).createQuery();
        //Query query = new QueryBuilder().setFootPrint(FOOTPRINT).setProductType("GRD").setPlatformName("Sentinel-1").createQuery();
        System.out.println(query.toString());

        List<Image> products = null;

        //search for the first MAX_NO_OF_IMAGES images that match the query
        SearchClient sClient = new SearchClient(USERNAME, PASSWORD);
        products = sClient.search(query.toString(), 0, MAX_NO_OF_IMAGES);

        if (products == null) {
            System.exit(-1);
            System.out.println("test");
        }

        System.out.println("Number of images to download\t:\t" + products.size());

        // download and save the images found during searching
        DataClient imageService = new DataClient(USERNAME, PASSWORD);
        for (Image image : products) {
            String imageName = image.getName();

            try {
                imageService.downloadAndSaveById(image.getId(), OUTPUT_DIRECTORY + imageName + ".zip");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

        }

        System.out.println("All images have been saved.");
    }
}
