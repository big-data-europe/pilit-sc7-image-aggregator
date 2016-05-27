package eu.bde.sc4pilot.imageaggregator;

import java.net.URL;

/**
 *
 * @author efi
 */

public class Image {

    private String id;
    private String name;
    private URL path;
    private String footPrint;

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

    public void setFootPrint(String footPrint) {
        this.footPrint = footPrint;
    }
}
