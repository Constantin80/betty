package info.fmro.betty.entities;

@SuppressWarnings("unused")
public class MarketLicence {
    private String wallet;
    private String rules;
    private Boolean rulesHasDate;
    private String clarifications;

    public synchronized String getWallet() {
        return this.wallet;
    }

    public synchronized void setWallet(final String wallet) {
        this.wallet = wallet;
    }

    public synchronized String getRules() {
        return this.rules;
    }

    public synchronized void setRules(final String rules) {
        this.rules = rules;
    }

    public synchronized Boolean getRulesHasDate() {
        return this.rulesHasDate;
    }

    public synchronized void setRulesHasDate(final Boolean rulesHasDate) {
        this.rulesHasDate = rulesHasDate;
    }

    public synchronized String getClarifications() {
        return this.clarifications;
    }

    public synchronized void setClarifications(final String clarifications) {
        this.clarifications = clarifications;
    }
}
