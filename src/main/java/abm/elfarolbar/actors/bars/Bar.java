package abm.elfarolbar.actors.bars;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Bar {
    private int totalPopulation;
    private int maxCapacity;
    @Builder.Default
    private List<Integer> attendanceHistory = Lists.newArrayList();
    private int attendance;

    public void addPatron() {
        this.attendance++;
    }

    public void record() {
        attendanceHistory.add(this.attendance);
    }

    public void reset() {
        this.attendance = 0;
    }

    public boolean isOvercrowded() {
        return this.attendance > this.maxCapacity;
    }

    public List<Boolean> crowdedHistory() {
        return this.attendanceHistory.stream()
                .map(attendance -> attendance > this.maxCapacity)
                .collect(Collectors.toList());
    }
}
