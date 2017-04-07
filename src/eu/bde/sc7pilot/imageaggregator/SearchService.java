package eu.bde.sc7pilot.imageaggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;

public class SearchService {

    private final static int MAX_NO_OF_IMAGES = 4;
    private String username;
    private String password;

    public SearchService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<Image> searchImages(ImageData imageData) throws Exception {
        WKTReader wktReader = new WKTReader();
        WKTWriter wktWriter = new WKTWriter();
        Query query = new QueryBuilder().setFootPrint("Intersects(" + wktWriter.write(imageData.getArea()) + ")")
                .setProductType("GRD").setPlatformName("Sentinel-1")
                .setFromBeginPosition(imageData.getReferenceDate().toString())
                .setToBeginPosition(imageData.getTargetDate().toDateTime(DateTimeZone.UTC).toString())
                .setFromBeginPosition(imageData.getReferenceDate().toDateTime(DateTimeZone.UTC).toString())
                .createQuery();
        System.out.println("QUERY TO SCIHUB:");
        System.out.println(query.toString());
        System.out.println("END OF QUERY\n");

        List<Image> productsToSearch = new ArrayList<Image>();
        Comparator dateTimeComparator = DateTimeComparator.getInstance().reversed();
        try {
            // search for the first MAX_NO_OF_IMAGES images that match the query
            SearchClient sClient = new SearchClient(username, password);
            productsToSearch = sClient.search(query.toString(), 0, MAX_NO_OF_IMAGES);
            System.out.println("PRODUCTS:");
            System.out.println(productsToSearch.toString());
            System.out.println("END OF PRODUCTS:");
            Collections.sort(productsToSearch, (d1, d2) -> dateTimeComparator.compare(d1.getDate(), d2.getDate()));

        } catch (Exception e) {
            throw e;
        }
        TreeMap<Double, Image> imagesAreas = new TreeMap<Double, Image>();
        List<Image> productsToDowload = new ArrayList<Image>();
        Image targetImage;
        if (productsToSearch.size() > 1) {
            Geometry targetGeometry;
            targetImage = productsToSearch.get(0);
            try {
                targetGeometry = wktReader.read(targetImage.getFootPrint());
                targetImage = productsToSearch.get(0);
                System.out.println("date!!" + productsToSearch.get(0).getDate() + " " + productsToSearch.get(1).getDate());
                for (int i = 1; i < productsToSearch.size(); i++) {
                    Geometry geometry = wktReader.read(productsToSearch.get(i).getFootPrint());
                    imagesAreas.put(targetGeometry.intersection(geometry).getArea(), productsToSearch.get(i));
                }
                productsToDowload.add(targetImage);

                productsToDowload.add(imagesAreas.firstEntry().getValue());
                Collections.sort(productsToDowload, (d1, d2) -> dateTimeComparator.compare(d1.getDate(), d2.getDate()));
            } catch (ParseException e) {
                throw e;
            }
        }

        // System.out.println("Number of images to download\t:\t" +
        // productsToDowload.size());
        return productsToDowload;
    }
}
