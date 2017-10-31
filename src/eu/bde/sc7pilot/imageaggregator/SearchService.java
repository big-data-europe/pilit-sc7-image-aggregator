package eu.bde.sc7pilot.imageaggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;

public class SearchService {

    private final static int MAX_NO_OF_IMAGES = 1;
    private final static int DAYS_WINDOW = 4;
    private String username;
    private String password;

    public SearchService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<Image> searchImages(ImageData imageData) throws Exception {
        DateTime refDate = imageData.getReferenceDate();
        DateTime targetDate = imageData.getTargetDate();
        Geometry area = imageData.getArea();
        Query query1 = queryMakerSentinel1(area, refDate);
        Query query2 = queryMakerSentinel1(area, targetDate);
        Query query3 = queryMakerSentinel2(area, refDate);
        Query query4 = queryMakerSentinel2(area, targetDate);

        List<Image> productsToSearch = new ArrayList<Image>();
        List<Image> qlooksToSearch = new ArrayList<Image>();
        List<Image> refProductsToSearch = new ArrayList<Image>();
        List<Image> refQuickLookToSearch = new ArrayList<Image>();
        List<Image> eventProoductsToSearch = new ArrayList<Image>();
        List<Image> eventQuickLookToSearch = new ArrayList<Image>();
        Comparator dateTimeComparator = DateTimeComparator.getInstance().reversed();
        try {
            // search for the first MAX_NO_OF_IMAGES images that match the query
            SearchClient sClient = new SearchClient(username, password);
            refProductsToSearch = sClient.search(query1.toString(), 0, MAX_NO_OF_IMAGES);
            eventProoductsToSearch = sClient.search(query2.toString(), 0, MAX_NO_OF_IMAGES);
            refQuickLookToSearch = sClient.search(query3.toString(), 0, MAX_NO_OF_IMAGES);
            eventQuickLookToSearch = sClient.search(query4.toString(), 0, MAX_NO_OF_IMAGES);
            for(int i = 0; i < MAX_NO_OF_IMAGES; i++) {
            	productsToSearch.add(refProductsToSearch.get(i));
            	productsToSearch.add(eventProoductsToSearch.get(i));
            	qlooksToSearch.add(refQuickLookToSearch.get(i));
            	qlooksToSearch.add(eventQuickLookToSearch.get(i));
            }
            Collections.sort(productsToSearch, (d1, d2) -> dateTimeComparator.compare(d1.getDate(), d2.getDate()));
            Collections.sort(qlooksToSearch, (d3, d4) -> dateTimeComparator.compare(d3.getDate(), d4.getDate()));

        } catch (Exception e) {
            throw e;
        }
        TreeMap<Double, Image> imagesAreas = new TreeMap<Double, Image>();
        List<Image> productsToDowload = new ArrayList<Image>();
        Image targetImage;
        WKTReader wktReader = new WKTReader();
        if (productsToSearch.size() > 1) {
            Geometry targetGeometry;
            targetImage = productsToSearch.get(0);
            try {
                targetGeometry = wktReader.read(targetImage.getFootPrint());
                targetImage = productsToSearch.get(0);

                for (int i = 1; i < productsToSearch.size(); i++) {
                    Geometry geometry = wktReader.read(productsToSearch.get(i).getFootPrint());
                    imagesAreas.put(targetGeometry.intersection(geometry).getArea(), productsToSearch.get(i));
                }
                productsToDowload.add(targetImage);

                productsToDowload.add(imagesAreas.firstEntry().getValue());
                Collections.sort(productsToDowload, (d1, d2) -> dateTimeComparator.compare(d1.getDate(), d2.getDate()));
                for(int j = 0; j < qlooksToSearch.size(); j++) {
                	productsToDowload.add(qlooksToSearch.get(j));
                }
                
            } catch (ParseException e) {
                throw e;
            }
        }
//        for(int i = 0; i < productsToDowload.size(); i++) {
//        	System.out.println("\n" + i + "th of products to download:\n");
//        	System.out.println(productsToDowload.get(i).getName());
//        	System.out.println(productsToDowload.get(i).getId());
//        	System.out.println("\n\t...end of info for product " + i + "\n\n");
//        }
       
        return productsToDowload;
    }
    
    public Query queryMakerSentinel1(Geometry area, DateTime dateTime) {
        WKTWriter wktWriter = new WKTWriter();
        DateTime DateStart = dateTime.minusDays(DAYS_WINDOW);
        DateTime DateEnd = dateTime.plusDays(DAYS_WINDOW);
        Query query = new QueryBuilder().setFootPrint("Intersects(" + wktWriter.write(area) + ")")
                .setProductType("GRD").setPlatformName("Sentinel-1")
                .setFromBeginPosition(DateStart.toString())
                .setToBeginPosition(DateEnd.toDateTime(DateTimeZone.UTC).toString())
                .setFromBeginPosition(DateStart.toDateTime(DateTimeZone.UTC).toString())
                .createQuery();
        return query;
    }
    
    public Query queryMakerSentinel2(Geometry area, DateTime dateTime) {
        WKTWriter wktWriter = new WKTWriter();
        DateTime DateStart = dateTime.minusDays(DAYS_WINDOW);
        DateTime DateEnd = dateTime.plusDays(DAYS_WINDOW);
        Query query = new QueryBuilder().setFootPrint("Intersects(" + wktWriter.write(area) + ")")
                .setProductType("S2MSI1C").setPlatformName("Sentinel-2")
                .setFromBeginPosition(DateStart.toString())
                .setToBeginPosition(DateEnd.toDateTime(DateTimeZone.UTC).toString())
                .setFromBeginPosition(DateStart.toDateTime(DateTimeZone.UTC).toString())
                .createQuery();
        return query;
    }
    
}
