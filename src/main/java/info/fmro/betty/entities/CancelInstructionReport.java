package info.fmro.betty.entities;

import info.fmro.betty.enums.InstructionReportErrorCode;
import info.fmro.betty.enums.InstructionReportStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class CancelInstructionReport {
    private InstructionReportStatus status;
    private InstructionReportErrorCode errorCode;
    private CancelInstruction instruction;
    private Double sizeCancelled;
    @Nullable
    private Date cancelledDate;

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

    public synchronized CancelInstruction getInstruction() {
        return this.instruction;
    }

    public synchronized void setInstruction(final CancelInstruction instruction) {
        this.instruction = instruction;
    }

    public synchronized Double getSizeCancelled() {
        return this.sizeCancelled;
    }

    public synchronized void setSizeCancelled(final Double sizeCancelled) {
        this.sizeCancelled = sizeCancelled;
    }

    @Nullable
    public synchronized Date getCancelledDate() {
        return this.cancelledDate == null ? null : (Date) this.cancelledDate.clone();
    }

    public synchronized void setCancelledDate(final Date cancelledDate) {
        this.cancelledDate = cancelledDate == null ? null : (Date) cancelledDate.clone();
    }
}
