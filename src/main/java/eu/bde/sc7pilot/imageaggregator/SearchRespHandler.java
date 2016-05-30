package eu.bde.sc7pilot.imageaggregator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author efi
 */

public class SearchRespHandler extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(SearchRespHandler.class.getName());
    private final static String ENTRY = "entry";
    private final static String FOOTPRINT = "footprint";
    private final static String HREF = "href";
    private final static String ID = "id";
    private final static String LINK = "link";
    private final static String NAME = "name";
    private final static String STR = "str";
    private final static String TITLE = "title";
    
    private boolean inFootprint;
    private boolean inProduct;
    private int linksCount;
    
    private List<Image> prodList = null;
    private Image prod = null;
    private String content;

    public SearchRespHandler() {
        prodList = null;
        prod = null;
        inProduct = false;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase(ENTRY)) {
            prod = new Image();
            // initialize list
            if (prodList == null) {
                prodList = new ArrayList<>();
            }
            inProduct = true;
        }

        if (qName.equalsIgnoreCase(LINK)) {
            if (inProduct && linksCount == 0) {
                try {
                    prod.setPath(new URL(attributes.getValue(HREF)));
                    linksCount = 1;
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.SEVERE, null, e);
                }
            }
        }

        if (qName.equalsIgnoreCase(STR) && inProduct) {
            if (attributes.getValue(NAME) != null) {
                if (attributes.getValue(NAME).equals(FOOTPRINT)) {
                    inFootprint = true;
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inProduct) {
            if (localName.equals(TITLE)) {
                prod.setName(content);
            }
            if (localName.equals(ID)) {
                prod.setId(content);
            }
            if (localName.equals(STR)) {
                if (inFootprint) {
                    prod.setFootPrint(content);
                    inFootprint = false;
                }
            }
        }

        if (qName.equalsIgnoreCase(ENTRY)) {
            prodList.add(prod);
            linksCount = 0;
            inProduct = false;
        }
    }

    public List<Image> getProdList() {
        return prodList;
    }

    public Image getProduct() {
        return prod;
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        content = new String(ch, start, length);
    }
}
