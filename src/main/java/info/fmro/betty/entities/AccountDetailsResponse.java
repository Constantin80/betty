package info.fmro.betty.entities;

@SuppressWarnings("unused")
class AccountDetailsResponse {
    private String currencyCode; // Default user currency Code.
    private String firstName; // First Name.
    private String lastName; // Last Name.
    private String localeCode; // Locale Code.
    private String region; // Region.
    private String timezone; // User Time Zone.
    private Double discountRate; // User Discount Rate.
    private Integer pointsBalance; // The Betfair points balance.
    private String countryCode; // The customer's country of residence (ISO 2 Char format)

    public synchronized String getCurrencyCode() {
        return this.currencyCode;
    }

    public synchronized void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public synchronized String getFirstName() {
        return this.firstName;
    }

    public synchronized void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public synchronized String getLastName() {
        return this.lastName;
    }

    public synchronized void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public synchronized String getLocaleCode() {
        return this.localeCode;
    }

    public synchronized void setLocaleCode(final String localeCode) {
        this.localeCode = localeCode;
    }

    public synchronized String getRegion() {
        return this.region;
    }

    public synchronized void setRegion(final String region) {
        this.region = region;
    }

    public synchronized String getTimezone() {
        return this.timezone;
    }

    public synchronized void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public synchronized Double getDiscountRate() {
        return this.discountRate;
    }

    public synchronized void setDiscountRate(final Double discountRate) {
        this.discountRate = discountRate;
    }

    public synchronized Integer getPointsBalance() {
        return this.pointsBalance;
    }

    public synchronized void setPointsBalance(final Integer pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public synchronized String getCountryCode() {
        return this.countryCode;
    }

    public synchronized void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }
}
