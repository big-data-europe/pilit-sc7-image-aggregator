package eu.bde.sc7pilot.imageaggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import eu.bde.sc7pilot.imageaggregator.utils.IAutils;

public class SearchService {

    private final static int MAX_NO_OF_IMAGES = 1;
    private final static int DAYS_WINDOW = 5;
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
        
        List<Image> productsToDowload = new ArrayList<Image>();
        try {
            // search for the first MAX_NO_OF_IMAGES images that match the query
            SearchClient sClient = new SearchClient(username, password);
            refProductsToSearch = sClient.search(query1.toString(), 0, MAX_NO_OF_IMAGES);
            eventProoductsToSearch = sClient.search(query2.toString(), 0, MAX_NO_OF_IMAGES);
            refQuickLookToSearch = sClient.search(query3.toString(), 0, MAX_NO_OF_IMAGES);
            eventQuickLookToSearch = sClient.search(query4.toString(), 0, MAX_NO_OF_IMAGES);
            if (refProductsToSearch.isEmpty() ||
            		eventProoductsToSearch.isEmpty() ||
            		refQuickLookToSearch.isEmpty() ||
            		eventQuickLookToSearch.isEmpty() ||
            		!IAutils.areaWithinImages(area, refProductsToSearch) ||
            		!IAutils.areaWithinImages(area, eventProoductsToSearch)) {
            	System.out.println("query1 returned Sentinel1 old images =\t" + refProductsToSearch.size());
            	System.out.println("query2 returned Sentinel1 new images =\t" + eventProoductsToSearch.size());
            	System.out.println("query3 returned Sentinel2 old images =\t" + refQuickLookToSearch.size());
            	System.out.println("query4 returned Sentinel2 new images =\t" + eventQuickLookToSearch.size());
            	System.out.println("Not all images found are good... returning 0 images!");
            	return productsToDowload;
            }
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
        
        // S1 and S2 images are legit and sorted and ready to be returned to main workflow for commanding download.
        for (int i = 0; i < productsToSearch.size(); i++) {
        	productsToDowload.add(productsToSearch.get(i));
        }
        for (int i = 0; i < qlooksToSearch.size(); i++) {
        	productsToDowload.add(qlooksToSearch.get(i));
        }
        
        // Un-comment to show more information related to the images.
//        for (int i = 0; i < productsToDowload.size(); i++) {
//        	System.out.println("\n" + i + "th of products to download:");
//        	System.out.println(productsToDowload.get(i).getName());
//        	System.out.println(productsToDowload.get(i).getId());
//        	System.out.println("\t...end of info for product " + i);
//    	}
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
