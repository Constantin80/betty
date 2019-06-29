package info.fmro.betty.stream.definitions;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class OrderFilter
        implements Serializable {
    private static final long serialVersionUID = 2933689998723721000L;
    private Set<Integer> accountIds; // This is for internal use only & should not be set on your filter (your subscription is already locked to your account).
    private Boolean includeOverallPosition = true; // Returns overall / net position (OrderRunnerChange.mb / OrderRunnerChange.ml) Default true
    private Set<String> customerStrategyRefs; // Restricts to specified customerStrategyRefs; this will filter orders and StrategyMatchChanges accordingly (Note: overall postition is not filtered)
    private Boolean partitionMatchedByStrategyRef; // Returns strategy positions (OrderRunnerChange.smc=Map<customerStrategyRef, StrategyMatchChange>) - these are sent in delta format as per overall position.

    public OrderFilter() {
    }

    public synchronized Set<String> getCustomerStrategyRefs() {
        return customerStrategyRefs == null ? null : new HashSet<>(customerStrategyRefs);
    }

    public synchronized void setCustomerStrategyRefs(final Set<String> customerStrategyRefs) {
        this.customerStrategyRefs = customerStrategyRefs == null ? null : new HashSet<>(customerStrategyRefs);
    }

    public synchronized Set<Integer> getAccountIds() {
        return accountIds == null ? null : new HashSet<>(accountIds);
    }

    public synchronized void setAccountIds(final Set<Integer> accountIds) {
        this.accountIds = accountIds == null ? null : new HashSet<>(accountIds);
    }

    public synchronized Boolean getIncludeOverallPosition() {
        return includeOverallPosition;
    }

    public synchronized void setIncludeOverallPosition(final Boolean includeOverallPosition) {
        this.includeOverallPosition = includeOverallPosition;
    }

    public synchronized Boolean getPartitionMatchedByStrategyRef() {
        return partitionMatchedByStrategyRef;
    }

    public synchronized void setPartitionMatchedByStrategyRef(final Boolean partitionMatchedByStrategyRef) {
        this.partitionMatchedByStrategyRef = partitionMatchedByStrategyRef;
    }
}
