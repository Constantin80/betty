package info.fmro.betty.entities;

import info.fmro.betty.enums.InstructionReportErrorCode;
import info.fmro.betty.enums.InstructionReportStatus;

public class UpdateInstructionReport {

    private InstructionReportStatus status;
    private InstructionReportErrorCode errorCode;
    private UpdateInstruction instruction;

    public UpdateInstructionReport() {
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

    public synchronized UpdateInstruction getInstruction() {
        return instruction;
    }

    public synchronized void setInstruction(UpdateInstruction instruction) {
        this.instruction = instruction;
    }
}
