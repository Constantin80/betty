package info.fmro.betty.entities;

import info.fmro.betty.enums.InstructionReportErrorCode;
import info.fmro.betty.enums.InstructionReportStatus;
import java.util.Date;

public class PlaceInstructionReport {

    private InstructionReportStatus status;
    private InstructionReportErrorCode errorCode;
    private PlaceInstruction instruction;
    private String betId;
    private Date placedDate;
    private Double averagePriceMatched;
    private Double sizeMatched;

    public PlaceInstructionReport() {
    }

    public synchronized InstructionReportStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(InstructionReportStatus status) {
        this.status = status;
    }

    public synchronized InstructionReportErrorCode getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(InstructionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized PlaceInstruction getInstruction() {
        return instruction;
    }

    public synchronized void setInstruction(PlaceInstruction instruction) {
        this.instruction = instruction;
    }

    public synchronized String getBetId() {
        return betId;
    }

    public synchronized void setBetId(String betId) {
        this.betId = betId;
    }

    public synchronized Date getPlacedDate() {
        return placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized void setPlacedDate(Date placedDate) {
        this.placedDate = placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized Double getAveragePriceMatched() {
        return averagePriceMatched;
    }

    public synchronized void setAveragePriceMatched(Double averagePriceMatched) {
        this.averagePriceMatched = averagePriceMatched;
    }

    public synchronized Double getSizeMatched() {
        return sizeMatched;
    }

    public synchronized void setSizeMatched(Double sizeMatched) {
        this.sizeMatched = sizeMatched;
    }
}
