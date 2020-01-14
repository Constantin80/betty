package info.fmro.betty.entities;

import info.fmro.shared.entities.CancelInstructionReport;
import info.fmro.shared.enums.InstructionReportErrorCode;
import info.fmro.shared.enums.InstructionReportStatus;

class ReplaceInstructionReport {
    private InstructionReportStatus status;
    private InstructionReportErrorCode errorCode;
    private CancelInstructionReport cancelInstructionReport;
    private PlaceInstructionReport placeInstructionReport;

    public synchronized InstructionReportStatus getStatus() {
        return this.status;
    }

    public synchronized void setStatus(final InstructionReportStatus status) {
        this.status = status;
    }

    public synchronized InstructionReportErrorCode getErrorCode() {
        return this.errorCode;
    }

    public synchronized void setErrorCode(final InstructionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized CancelInstructionReport getCancelInstructionReport() {
        return this.cancelInstructionReport;
    }

    public synchronized void setCancelInstructionReport(final CancelInstructionReport cancelInstructionReport) {
        this.cancelInstructionReport = cancelInstructionReport;
    }

    public synchronized PlaceInstructionReport getPlaceInstructionReport() {
        return this.placeInstructionReport;
    }

    public synchronized void setPlaceInstructionReport(final PlaceInstructionReport placeInstructionReport) {
        this.placeInstructionReport = placeInstructionReport;
    }
}
