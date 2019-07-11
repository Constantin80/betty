package info.fmro.betty.entities;

import info.fmro.betty.enums.WinLose;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class StatementLegacyData {
    private Double avgPrice;
    private Double betSize;
    private String betType;
    private String betCategoryType;
    private String commissionRate;
    private Long eventId;
    private Long eventTypeId;
    private String fullMarketName;
    private Double grossBetAmount;
    private String marketName;
    private String marketType;
    @Nullable
    private Date placedDate;
    private Long selectionId;
    private String selectionName;
    @Nullable
    private Date startDate;
    private String transactionType;
    private Long transactionId;
    private WinLose winLose;

    public synchronized Double getAvgPrice() {
        return this.avgPrice;
    }

    public synchronized void setAvgPrice(final Double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public synchronized Double getBetSize() {
        return this.betSize;
    }

    public synchronized void setBetSize(final Double betSize) {
        this.betSize = betSize;
    }

    public synchronized String getBetType() {
        return this.betType;
    }

    public synchronized void setBetType(final String betType) {
        this.betType = betType;
    }

    public synchronized String getBetCategoryType() {
        return this.betCategoryType;
    }

    public synchronized void setBetCategoryType(final String betCategoryType) {
        this.betCategoryType = betCategoryType;
    }

    public synchronized String getCommissionRate() {
        return this.commissionRate;
    }

    public synchronized void setCommissionRate(final String commissionRate) {
        this.commissionRate = commissionRate;
    }

    public synchronized Long getEventId() {
        return this.eventId;
    }

    public synchronized void setEventId(final Long eventId) {
        this.eventId = eventId;
    }

    public synchronized Long getEventTypeId() {
        return this.eventTypeId;
    }

    public synchronized void setEventTypeId(final Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public synchronized String getFullMarketName() {
        return this.fullMarketName;
    }

    public synchronized void setFullMarketName(final String fullMarketName) {
        this.fullMarketName = fullMarketName;
    }

    public synchronized Double getGrossBetAmount() {
        return this.grossBetAmount;
    }

    public synchronized void setGrossBetAmount(final Double grossBetAmount) {
        this.grossBetAmount = grossBetAmount;
    }

    public synchronized String getMarketName() {
        return this.marketName;
    }

    public synchronized void setMarketName(final String marketName) {
        this.marketName = marketName;
    }

    public synchronized String getMarketType() {
        return this.marketType;
    }

    public synchronized void setMarketType(final String marketType) {
        this.marketType = marketType;
    }

    @Nullable
    public synchronized Date getPlacedDate() {
        return this.placedDate == null ? null : (Date) this.placedDate.clone();
    }

    public synchronized void setPlacedDate(final Date placedDate) {
        this.placedDate = placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized void setSelectionId(final Long selectionId) {
        this.selectionId = selectionId;
    }

    public synchronized String getSelectionName() {
        return this.selectionName;
    }

    public synchronized void setSelectionName(final String selectionName) {
        this.selectionName = selectionName;
    }

    @Nullable
    public synchronized Date getStartDate() {
        return this.startDate == null ? null : (Date) this.startDate.clone();
    }

    public synchronized void setStartDate(final Date startDate) {
        this.startDate = startDate == null ? null : (Date) startDate.clone();
    }

    public synchronized String getTransactionType() {
        return this.transactionType;
    }

    public synchronized void setTransactionType(final String transactionType) {
        this.transactionType = transactionType;
    }

    public synchronized Long getTransactionId() {
        return this.transactionId;
    }

    public synchronized void setTransactionId(final Long transactionId) {
        this.transactionId = transactionId;
    }

    public synchronized WinLose getWinLose() {
        return this.winLose;
    }

    public synchronized void setWinLose(final WinLose winLose) {
        this.winLose = winLose;
    }
}
