package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class MinAverageAttendanceDecisionStrategy extends AttendanceDecisionStrategy {
    @Builder.Default
    private int minAverage = 30;

    public boolean decideByAttendance(final Bar bar, final List<Integer> recentAttendanceHistory) {
        return recentAttendanceHistory.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(RandomUtils.nextInt(0, bar.getTotalPopulation() + 1)) >= minAverage;
    }

    @Override
    public String getName() {
        return String.format("MinAverage-%d", minAverage);
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
