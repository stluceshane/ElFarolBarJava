package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import java.util.List;
import lombok.Builder;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
public class MaxAverageAttendanceDecisionStrategy extends AttendanceDecisionStrategy {
    @Builder.Default
    private int maxAverage = 80;

    public boolean decideByAttendance(final Bar bar, final List<Integer> recentAttendanceHistory) {
        return recentAttendanceHistory.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(RandomUtils.nextInt(0, bar.getTotalPopulation() + 1)) <= maxAverage;
    }

    @Override
    public String getName() {
        return String.format("MaxAverage-%d", maxAverage);
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
