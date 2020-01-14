package info.fmro.betty.entities;

import info.fmro.shared.enums.InstructionReportErrorCode;
import info.fmro.shared.enums.InstructionReportStatus;
import info.fmro.shared.enums.OrderStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class PlaceInstructionReport {
    private InstructionReportStatus status;
    private InstructionReportErrorCode errorCode;
    @SuppressWarnings("unused")
    private OrderStatus orderStatus;
    private PlaceInstruction instruction;
    private String betId;
    @Nullable
    private Date placedDate;
    private Double averagePriceMatched;
    private Double sizeMatched;

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

    public synchronized PlaceInstruction getInstruction() {
        return this.instruction;
    }

    public synchronized void setInstruction(final PlaceInstruction instruction) {
        this.instruction = instruction;
    }

    public synchronized String getBetId() {
        return this.betId;
    }

    public synchronized void setBetId(final String betId) {
        this.betId = betId;
    }

    @Nullable
    public synchronized Date getPlacedDate() {
        return this.placedDate == null ? null : (Date) this.placedDate.clone();
    }

    public synchronized void setPlacedDate(final Date placedDate) {
        this.placedDate = placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized Double getAveragePriceMatched() {
        return this.averagePriceMatched;
    }

    public synchronized void setAveragePriceMatched(final Double averagePriceMatched) {
        this.averagePriceMatched = averagePriceMatched;
    }

    public synchronized Double getSizeMatched() {
        return this.sizeMatched;
    }

    public synchronized void setSizeMatched(final Double sizeMatched) {
        this.sizeMatched = sizeMatched;
    }
}
