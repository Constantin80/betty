package info.fmro.betty.entities;

import info.fmro.betty.enums.ExecutionReportErrorCode;
import info.fmro.betty.enums.ExecutionReportStatus;
import java.util.ArrayList;
import java.util.List;

public class PlaceExecutionReport {

    private String customerRef;
    private ExecutionReportStatus status;
    private ExecutionReportErrorCode errorCode;
    private String marketId;
    private List<PlaceInstructionReport> instructionReports;

    public PlaceExecutionReport() {
    }

    public synchronized String getCustomerRef() {
        return customerRef;
    }

    public synchronized void setCustomerRef(String customerRef) {
        this.customerRef = customerRef;
    }

    public synchronized ExecutionReportStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(ExecutionReportStatus status) {
        this.status = status;
    }

    public synchronized ExecutionReportErrorCode getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(ExecutionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public synchronized List<PlaceInstructionReport> getInstructionReports() {
        return instructionReports == null ? null : new ArrayList<>(instructionReports);
    }

    public synchronized void setInstructionReports(List<PlaceInstructionReport> instructionReports) {
        this.instructionReports = instructionReports == null ? null : new ArrayList<>(instructionReports);
    }
}
