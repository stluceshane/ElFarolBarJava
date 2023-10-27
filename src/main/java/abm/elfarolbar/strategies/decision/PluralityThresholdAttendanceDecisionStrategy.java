package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class PluralityThresholdAttendanceDecisionStrategy extends AttendanceDecisionStrategy {
    @Builder.Default
    private int threshold = 30;
    @Builder.Default
    private boolean reversed = false;

    public boolean decideByAttendance(final Bar bar, final List<Integer> recentAttendanceHistory) {
        final long countBelowThreshold = recentAttendanceHistory.stream()
                .filter(value -> value <= threshold)
                .count();
        final long countAboveThreshold = recentAttendanceHistory.stream()
                .filter(value -> value >= threshold)
                .count();
        return (countBelowThreshold <= countAboveThreshold) != reversed;
    }

    @Override
    public String getName() {
        return String.format("PluralityThreshold-%d-%b", threshold, reversed);
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
