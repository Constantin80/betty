package info.fmro.betty.entities;

import info.fmro.betty.enums.ExecutionReportErrorCode;
import info.fmro.betty.enums.ExecutionReportStatus;

import java.util.ArrayList;
import java.util.List;

public class CancelExecutionReport {
    private String customerRef;
    private ExecutionReportStatus status;
    private ExecutionReportErrorCode errorCode;
    private String marketId;
    private List<CancelInstructionReport> instructionReports;

    public CancelExecutionReport() {
    }

    public synchronized String getCustomerRef() {
        return customerRef;
    }

    public synchronized void setCustomerRef(final String customerRef) {
        this.customerRef = customerRef;
    }

    public synchronized ExecutionReportStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(final ExecutionReportStatus status) {
        this.status = status;
    }

    public synchronized ExecutionReportErrorCode getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(final ExecutionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized void setMarketId(final String marketId) {
        this.marketId = marketId;
    }

    public synchronized List<CancelInstructionReport> getInstructionReports() {
        return instructionReports == null ? null : new ArrayList<>(instructionReports);
    }

    public synchronized void setInstructionReports(final List<CancelInstructionReport> instructionReports) {
        this.instructionReports = instructionReports == null ? null : new ArrayList<>(instructionReports);
    }
}
