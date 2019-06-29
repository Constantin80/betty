package info.fmro.betty.entities;

import info.fmro.betty.enums.InstructionReportErrorCode;
import info.fmro.betty.enums.InstructionReportStatus;

public class ReplaceInstructionReport {
    private InstructionReportStatus status;
    private InstructionReportErrorCode errorCode;
    private CancelInstructionReport cancelInstructionReport;
    private PlaceInstructionReport placeInstructionReport;

    public ReplaceInstructionReport() {
    }

    public synchronized InstructionReportStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(final InstructionReportStatus status) {
        this.status = status;
    }

    public synchronized InstructionReportErrorCode getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(final InstructionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized CancelInstructionReport getCancelInstructionReport() {
        return cancelInstructionReport;
    }

    public synchronized void setCancelInstructionReport(final CancelInstructionReport cancelInstructionReport) {
        this.cancelInstructionReport = cancelInstructionReport;
    }

    public synchronized PlaceInstructionReport getPlaceInstructionReport() {
        return placeInstructionReport;
    }

    public synchronized void setPlaceInstructionReport(final PlaceInstructionReport placeInstructionReport) {
        this.placeInstructionReport = placeInstructionReport;
    }
}
