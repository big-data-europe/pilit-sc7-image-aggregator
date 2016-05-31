package eu.bde.sc7pilot.imageaggregator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.bde.sc7pilot.imageaggregator.model.Image;

public class SearchRespHandler extends DefaultHandler {

    private List<Image> prodList = null;
    private Image prod = null;
    private boolean inProduct = false;
    private boolean inProperties = false;
    private int linksCount;
    private boolean inFootprint;
    private boolean inBeginPosition = false;
    private StringBuffer stringBuffer;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("entry")) {
            prod = new Image();
            // initialize list
            if (prodList == null) {
                prodList = new ArrayList<>();
            }
            inProduct = true;
        }
        if (qName.equalsIgnoreCase("m:properties")) {
            inProperties = true;
        }
        if (qName.equalsIgnoreCase("link")) {
            if (inProduct && linksCount == 0) {
                try {
                    prod.setPath(new URL(attributes.getValue("href")));
                    linksCount = 1;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (qName.equalsIgnoreCase("str") && inProduct) {
            if (attributes.getValue("name") != null) {
                if (attributes.getValue("name").equals("footprint")) {
                    inFootprint = true;
                }
            }
        }
        if (qName.equalsIgnoreCase("date") && inProduct) {
            if (attributes.getValue("name") != null) {
                if (attributes.getValue("name").equals("beginposition")) {
                    inBeginPosition = true;
                }
            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        prodList = new ArrayList<>();
        stringBuffer = new StringBuffer();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String result = stringBuffer.toString();
        stringBuffer.setLength(0);

        if (inProduct) {
            if (localName.equals("title")) {
                prod.setName(result.trim());
            }
            if (localName.equals("id")) {
                prod.setId(result.trim());
            }
//            if(inProperties&&localName.equalsIgnoreCase("d:CreationDate")){
//            	DateTime date=new DateTime(result.trim());
//            	prod.setDate(date);
//            }
            if (inBeginPosition) {
                DateTime date = new DateTime(result.trim());
                prod.setDate(date);
                inBeginPosition = false;
            }
            if (localName.equals("str")) {
                if (inFootprint) {
                    prod.setFootPrint(result.trim());
                    inFootprint = false;
                }
            }
        }
        if (qName.equalsIgnoreCase("entry")) {
            // add Employee object to list
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
        stringBuffer.append(new String(ch, start, length));
    }
}
