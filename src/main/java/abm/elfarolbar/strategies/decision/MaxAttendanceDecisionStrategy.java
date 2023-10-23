package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import java.util.List;
import lombok.Builder;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
public class MaxAttendanceDecisionStrategy extends AttendanceDecisionStrategy {
    @Builder.Default
    private int localMax = 60;

    public boolean decideByAttendance(final Bar bar, final List<Integer> recentAttendanceHistory) {
        return recentAttendanceHistory.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(RandomUtils.nextInt(0, bar.getTotalPopulation() + 1)) >= localMax;
    }

    @Override
    public String getName() {
        return String.format("Max-%d", localMax);
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
