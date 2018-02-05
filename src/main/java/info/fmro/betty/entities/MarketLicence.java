package info.fmro.betty.entities;

public class MarketLicence {

    private String wallet;
    private String rules;
    private Boolean rulesHasDate;
    private String clarifications;

    public MarketLicence() {
    }

    public synchronized String getWallet() {
        return wallet;
    }

    public synchronized void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public synchronized String getRules() {
        return rules;
    }

    public synchronized void setRules(String rules) {
        this.rules = rules;
    }

    public synchronized Boolean isRulesHasDate() {
        return rulesHasDate;
    }

    public synchronized void setRulesHasDate(Boolean rulesHasDate) {
        this.rulesHasDate = rulesHasDate;
    }

    public synchronized String getClarifications() {
        return clarifications;
    }

    public synchronized void setClarifications(String clarifications) {
        this.clarifications = clarifications;
    }
}
