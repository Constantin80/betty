package info.fmro.betty.entities;

import java.util.ArrayList;
import java.util.List;

public class AccountStatementReport {

    private List<StatementItem> accountStatement; // The list of statement items returned by your request.
    private Boolean moreAvailable; // Indicates whether there are further result items beyond this page.

    public AccountStatementReport() {
    }

    public synchronized List<StatementItem> getAccountStatement() {
        return accountStatement == null ? null : new ArrayList<>(accountStatement);
    }

    public synchronized void setAccountStatement(List<StatementItem> accountStatement) {
        this.accountStatement = accountStatement == null ? null : new ArrayList<>(accountStatement);
    }

    public synchronized Boolean isMoreAvailable() {
        return moreAvailable;
    }

    public synchronized void setMoreAvailable(Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }
}
