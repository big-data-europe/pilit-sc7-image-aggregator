package model;

import java.net.URL;
import org.joda.time.DateTime;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 *
 * @author efi
 */

public class Image {

    private String id;
    private String name;
    private URL path;
    private String footPrint;
    private DateTime date;

    public Image() {
        this(null, null, null, null);
    }

    public Image(String id, String name, URL path, String footPrint) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.footPrint = footPrint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getPath() {
        return path;
    }

    public void setPath(URL path) {
        this.path = path;
    }

    public String getFootPrint() {
        return footPrint;
    }

    public Geometry getWKTGeometry() {
        WKTReader wktReader = new WKTReader();
        Geometry geometry = null;
        try {
            geometry = wktReader.read(getFootPrint());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return geometry;
    }

    public void setFootPrint(String footPrint) {
        this.footPrint = footPrint;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }
}
