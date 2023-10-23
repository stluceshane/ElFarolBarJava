package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import java.util.List;
import lombok.Builder;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
public class MinAttendanceDecisionStrategy extends AttendanceDecisionStrategy {
    @Builder.Default
    private int localMin = 40;

    public boolean decideByAttendance(final Bar bar, final List<Integer> recentAttendanceHistory) {
        return recentAttendanceHistory.stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(RandomUtils.nextInt(0, bar.getTotalPopulation() + 1)) <= localMin;
    }

    @Override
    public String getName() {
        return String.format("Min-%d", localMin);
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
