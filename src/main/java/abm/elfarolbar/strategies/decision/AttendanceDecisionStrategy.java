package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import java.util.List;

public abstract class AttendanceDecisionStrategy extends DecisionStrategy {
    abstract boolean decideByAttendance(final Bar bar, final List<Integer> recentAttendanceHistory);

    public boolean decide(final Bar bar, final PatronMemoryProps memoryProps) {
        final List<Integer> attendanceHistory = bar.getAttendanceHistory();
        return decideByAttendance(bar, attendanceHistory.subList(Math.max(0, attendanceHistory.size() - memoryProps.getMemoryLength()), attendanceHistory.size()));
    }
}
