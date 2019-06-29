package info.fmro.betty.entities;

public class AccountDetailsResponse {
    private String currencyCode; // Default user currency Code.
    private String firstName; // First Name.
    private String lastName; // Last Name.
    private String localeCode; // Locale Code.
    private String region; // Region.
    private String timezone; // User Time Zone.
    private Double discountRate; // User Discount Rate.
    private Integer pointsBalance; // The Betfair points balance.
    private String countryCode; // The customer's country of residence (ISO 2 Char format)

    public AccountDetailsResponse() {
    }

    public synchronized String getCurrencyCode() {
        return currencyCode;
    }

    public synchronized void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public synchronized String getFirstName() {
        return firstName;
    }

    public synchronized void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public synchronized String getLastName() {
        return lastName;
    }

    public synchronized void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public synchronized String getLocaleCode() {
        return localeCode;
    }

    public synchronized void setLocaleCode(final String localeCode) {
        this.localeCode = localeCode;
    }

    public synchronized String getRegion() {
        return region;
    }

    public synchronized void setRegion(final String region) {
        this.region = region;
    }

    public synchronized String getTimezone() {
        return timezone;
    }

    public synchronized void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public synchronized Double getDiscountRate() {
        return discountRate;
    }

    public synchronized void setDiscountRate(final Double discountRate) {
        this.discountRate = discountRate;
    }

    public synchronized Integer getPointsBalance() {
        return pointsBalance;
    }

    public synchronized void setPointsBalance(final Integer pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public synchronized String getCountryCode() {
        return countryCode;
    }

    public synchronized void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }
}
