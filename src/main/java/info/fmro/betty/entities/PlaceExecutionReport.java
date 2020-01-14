package info.fmro.betty.entities;

import info.fmro.shared.enums.ExecutionReportErrorCode;
import info.fmro.shared.enums.ExecutionReportStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlaceExecutionReport {
    private String customerRef;
    private ExecutionReportStatus status;
    private ExecutionReportErrorCode errorCode;
    private String marketId;
    @Nullable
    private List<PlaceInstructionReport> instructionReports;

    public synchronized String getCustomerRef() {
        return this.customerRef;
    }

    public synchronized void setCustomerRef(final String customerRef) {
        this.customerRef = customerRef;
    }

    public synchronized ExecutionReportStatus getStatus() {
        return this.status;
    }

    public synchronized void setStatus(final ExecutionReportStatus status) {
        this.status = status;
    }

    public synchronized ExecutionReportErrorCode getErrorCode() {
        return this.errorCode;
    }

    public synchronized void setErrorCode(final ExecutionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized void setMarketId(final String marketId) {
        this.marketId = marketId;
    }

    @Nullable
    public synchronized List<PlaceInstructionReport> getInstructionReports() {
        return this.instructionReports == null ? null : new ArrayList<>(this.instructionReports);
    }

    public synchronized void setInstructionReports(final List<? extends PlaceInstructionReport> instructionReports) {
        this.instructionReports = instructionReports == null ? null : new ArrayList<>(instructionReports);
    }
}
