package info.fmro.betty.entities;

import info.fmro.betty.enums.ItemClass;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatementItem {
    private String refId; // An external reference, eg. equivalent to betId in the case of an exchange bet statement item.
    private Date itemDate; // The date and time of the statement item, eg. equivalent to settledData for an exchange bet statement item. (in ISO-8601 format, not translated)
    private Double amount; // The amount of money the balance is adjusted by
    private Double balance; // Account balance.
    private ItemClass itemClass; // Class of statement item. This value will determine which set of keys will be included in itemClassData
    private Map<String, String> itemClassData; // Key value pairs describing the current statement item. The set of keys will be determined by the itemClass
    private StatementLegacyData legacyData; // Set of fields originally returned from APIv6. Provided to facilitate migration from APIv6 to API-NG, and ultimately onto itemClass 

    public StatementItem() {
    }

    // and itemClassData
    public synchronized String getRefId() {
        return refId;
    }

    public synchronized void setRefId(final String refId) {
        this.refId = refId;
    }

    public synchronized Date getItemDate() {
        return itemDate == null ? null : (Date) itemDate.clone();
    }

    public synchronized void setItemDate(final Date itemDate) {
        this.itemDate = itemDate == null ? null : (Date) itemDate.clone();
    }

    public synchronized Double getAmount() {
        return amount;
    }

    public synchronized void setAmount(final Double amount) {
        this.amount = amount;
    }

    public synchronized Double getBalance() {
        return balance;
    }

    public synchronized void setBalance(final Double balance) {
        this.balance = balance;
    }

    public synchronized ItemClass getItemClass() {
        return itemClass;
    }

    public synchronized void setItemClass(final ItemClass itemClass) {
        this.itemClass = itemClass;
    }

    public synchronized Map<String, String> getItemClassData() {
        return itemClassData == null ? null : new HashMap<>(itemClassData);
    }

    public synchronized void setItemClassData(final Map<String, String> itemClassData) {
        this.itemClassData = itemClassData == null ? null : new HashMap<>(itemClassData);
    }

    public synchronized StatementLegacyData getLegacyData() {
        return legacyData;
    }

    public synchronized void setLegacyData(final StatementLegacyData legacyData) {
        this.legacyData = legacyData;
    }
}
