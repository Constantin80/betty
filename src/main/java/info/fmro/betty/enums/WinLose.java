package info.fmro.betty.enums;

public enum WinLose {
    RESULT_ERR, // Record has been affected by a unsettlement. There is no impact on the balance for these records, this just a label to say that these are to be corrected.
    RESULT_FIX, // Record is a correction to the balance to reverse the impact of records shown as in error. If commission has been paid on the original settlement then there will be a second FIX record to reverse the commission.
    RESULT_LOST, // Loss
    RESULT_NOT_APPLICABLE, // Include poker transactions only
    RESULT_WON, // Won
    COMMISSION_REVERSAL // Betfair have restored the funds to your account that it previously received from you in commission.
}
