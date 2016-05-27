package eu.bde.sc4pilot.imageaggregator;

/**
 *
 * @author efi
 */

public class QueryBuilder {

    private String footPrint;
    private String productType;
    private String polarisationMode;
    private String platformName;
    private String orbit;
    private String beginPosition;

    public QueryBuilder() {
    }

    public QueryBuilder setFootPrint(String footPrint) {
        this.footPrint = footPrint;
        return this;
    }

    public QueryBuilder setProductType(String productType) {
        this.productType = productType;
        return this;
    }

    public QueryBuilder setPolarisationMode(String polarisationMode) {
        this.polarisationMode = polarisationMode;
        return this;
    }

    public QueryBuilder setPlatformName(String platformName) {
        this.platformName = platformName;
        return this;
    }

    public QueryBuilder setOrbit(String orbit) {
        this.orbit = orbit;
        return this;
    }

    public QueryBuilder setBeginPosition(String beginPosition) {
        this.beginPosition = beginPosition;
        return this;
    }

    public Query createQuery() {
        return new Query(footPrint, productType, polarisationMode, platformName, orbit, beginPosition);
    }

}
