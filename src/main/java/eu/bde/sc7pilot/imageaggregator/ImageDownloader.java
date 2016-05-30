package eu.bde.sc7pilot.imageaggregator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author efi
 */

public class ImageDownloader {

    public ImageDownloader() {
        Properties props=new Properties();
        try {
            props.load(new FileInputStream("configuration.properties"));
        } catch (IOException ex) {
            Logger.getLogger(ImageDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
