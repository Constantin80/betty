package info.fmro.betty.entities;

import info.fmro.betty.enums.InstructionReportErrorCode;
import info.fmro.betty.enums.InstructionReportStatus;

import java.util.Date;

public class CancelInstructionReport {
    private InstructionReportStatus status;
    private InstructionReportErrorCode errorCode;
    private CancelInstruction instruction;
    private Double sizeCancelled;
    private Date cancelledDate;

    public CancelInstructionReport() {
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

    public synchronized CancelInstruction getInstruction() {
        return instruction;
    }

    public synchronized void setInstruction(final CancelInstruction instruction) {
        this.instruction = instruction;
    }

    public synchronized Double getSizeCancelled() {
        return sizeCancelled;
    }

    public synchronized void setSizeCancelled(final Double sizeCancelled) {
        this.sizeCancelled = sizeCancelled;
    }

    public synchronized Date getCancelledDate() {
        return cancelledDate == null ? null : (Date) cancelledDate.clone();
    }

    public synchronized void setCancelledDate(final Date cancelledDate) {
        this.cancelledDate = cancelledDate == null ? null : (Date) cancelledDate.clone();
    }
}
