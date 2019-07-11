package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
class AccountStatementReport {
    @Nullable
    private List<StatementItem> accountStatement; // The list of statement items returned by your request.
    private Boolean moreAvailable; // Indicates whether there are further result items beyond this page.

    @Nullable
    public synchronized List<StatementItem> getAccountStatement() {
        return this.accountStatement == null ? null : new ArrayList<>(this.accountStatement);
    }

    public synchronized void setAccountStatement(final List<? extends StatementItem> accountStatement) {
        this.accountStatement = accountStatement == null ? null : new ArrayList<>(accountStatement);
    }

    public synchronized Boolean getMoreAvailable() {
        return this.moreAvailable;
    }

    public synchronized void setMoreAvailable(final Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }
}
