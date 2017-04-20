package eu.bde.sc7pilot.imageaggregator;

/**
 *
 * @author efi
 */

public class Query {

    private String footPrint;
    private String productType;
    private String polarisationMode;
    private String platformName;
    private String orbit;
    private String fromBeginPosition;
    private String toBeginPosition;

    public Query(String footPrint,
    		     String productType,
    		     String polarisationMode,
    		     String platformName,
    		     String orbit,
    		     String fromBeginPosition,
    		     String toBeginPosition) {
    	
        this.footPrint = footPrint;
        this.productType = productType;
        this.polarisationMode = polarisationMode;
        this.platformName = platformName;
        this.orbit = orbit;
        this.fromBeginPosition = fromBeginPosition;
        this.toBeginPosition = toBeginPosition;
    }

    /**
     * @return the footPrint
     */
    public String getFootPrint() {
        return footPrint;
    }

    /**
     * @param footPrint the footPrint to set
     */
    public void setFootPrint(String footPrint) {
        this.footPrint = footPrint;
    }

    /**
     * @return the productType
     */
    public String getProductType() {
        return productType;
    }

    /**
     * @param productType the productType to set
     */
    public void setProductType(String productType) {
        this.productType = productType;
    }

    /**
     * @return the polarisationMode
     */
    public String getPolarisationMode() {
        return polarisationMode;
    }

    /**
     * @param polarisationMode the polarisationMode to set
     */
    public void setPolarisationMode(String polarisationMode) {
        this.polarisationMode = polarisationMode;
    }

    /**
     * @return the platformName
     */
    public String getPlatformName() {
        return platformName;
    }

    /**
     * @param platformName the platformName to set
     */
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    /**
     * @return the orbit
     */
    public String getOrbit() {
        return orbit;
    }

    /**
     * @param orbit the orbit to set
     */
    public void setOrbit(String orbit) {
        this.orbit = orbit;
    }

    @Override
    public String toString() {
        boolean first = true;
        String query = "";
        if (platformName != null) {
            query += "platformname:" + platformName;
            first = false;
        }
        if (productType != null) {
            query += (!first ? " AND " : "") + " productType:\"" + productType + "\" ";
            first = false;
        }
        if (polarisationMode != null) {

            query += (!first ? " AND " : "") + " polarisationMode:\"" + polarisationMode + "\" ";
            first = false;
        }
        if (orbit != null) {
            query += (!first ? " AND " : "") + " orbit:\"" + orbit + "\" ";
            first = false;
        }
        if (footPrint != null) {
            query += (!first ? " AND " : "") + " ( footprint:" + '"' + footPrint + '"' + " )";
            first = false;
        }
        if (fromBeginPosition != null) {
            query += (!first ? " AND " : "") + " beginPosition:[" + fromBeginPosition + " TO " + toBeginPosition + "]";
            //query += (!first?" AND ":"")+" beginPosition:"+"[2016-03-01T07:21:20.892Z TO 2016-03-01T07:21:20.892Z]";
            first = false;
        }
        System.out.println("QUERY TO SCIHUB:");
        System.out.println(query);
        System.out.println("END OF QUERY\n");
        return query; //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the beginPosition
     */
    public String getFromBeginPosition() {
        return fromBeginPosition;
    }

    public String getToBeginPosition() {
        return toBeginPosition;
    }

    public void setToBeginPosition(String toBeginPosition) {
        this.toBeginPosition = toBeginPosition;
    }

    public void setFromBeginPosition(String fromBeginPosition) {
        this.fromBeginPosition = fromBeginPosition;
    }
}
