package model;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author efi
 */

public class ImageData {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private DateTime targetDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private DateTime referenceDate;
    private Geometry area;
    private String username;
    private String password;
    private String[] selectedPolarisations;

    public ImageData() {
        this(null, null, null);
    }

    public ImageData(DateTime targetDate, Geometry area) {
        this(targetDate, null, area);
    }

    public ImageData(DateTime targetDate, DateTime referenceDate) {
        this(targetDate, referenceDate, null);
    }

    public ImageData(DateTime targetDate, DateTime referenceDate, Geometry area) {
        this(targetDate, referenceDate, area, null, null, null);
    }

    public ImageData(DateTime targetDate, DateTime referenceDate, Geometry area, String username, String password, String[] selectedPolarisations) {
        this.targetDate = targetDate;
        this.referenceDate = referenceDate;
        this.area = area;
        this.username = username;
        this.password = password;
        this.selectedPolarisations = selectedPolarisations;
    }

    public DateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(DateTime targetDate) {
        this.targetDate = targetDate;
    }

    public DateTime getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(DateTime sourceDates) {
        this.referenceDate = sourceDates;
    }

    public Geometry getArea() {
        return area;
    }

    public void setArea(Geometry area) {
        this.area = area;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getSelectedPolarisations() {
        return selectedPolarisations;
    }

    public void setSelectedPolarisations(String[] selectedPolarisations) {
        this.selectedPolarisations = selectedPolarisations;
    }
}
