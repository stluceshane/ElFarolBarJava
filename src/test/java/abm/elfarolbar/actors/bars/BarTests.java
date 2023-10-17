package abm.elfarolbar.actors.bars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BarTests {
    @Test
    public void defaultBuilder_createsBar_withDefaultValues() {
        final Bar bar = Bar.builder().build();
        final Bar expectedBar = Bar.builder()
                .totalPopulation(0)
                .maxCapacity(0)
                .attendanceHistory(Lists.newArrayList())
                .attendance(0)
                .build();

        assertThat("Bar is created correctly", bar, is(expectedBar));
    }

    @Test
    public void addPatron_updatesAttendance() {
        final int currentAttendance = 5;
        final Bar bar = Bar.builder()
                .attendance(currentAttendance)
                .build();
        bar.addPatron();

        assertThat("Bar attendance is incremented", bar.getAttendance(), is(currentAttendance + 1));
    }

    @Test
    public void record_updatesAttendanceHistory() {
        final int previousAttendance = 3;
        final int currentAttendance = 5;
        final Bar bar = Bar.builder()
                .attendanceHistory(Lists.newArrayList(previousAttendance))
                .attendance(currentAttendance)
                .build();
        bar.record();

        assertThat("Bar attendance is recorded in attendance history", bar.getAttendanceHistory(), is(ImmutableList.of(previousAttendance, currentAttendance)));
    }

    @Test
    public void reset_setsAttendanceToZero() {
        final int currentAttendance = 5;
        final Bar bar = Bar.builder()
                .attendance(currentAttendance)
                .build();
        bar.reset();

        assertThat("Bar attendance is set to zero", bar.getAttendance(), is(0));
    }

    @Test
    public void isOvercrowded_returnsTrue_whenAttendanceIsGreaterThanCapacity() {
        final int currentAttendance = 5;
        final Bar bar = Bar.builder()
                .attendance(currentAttendance)
                .maxCapacity(currentAttendance - 1)
                .build();

        assertThat("Bar is overcrowded when attendance > capacity", bar.isOvercrowded(), is(true));
    }

    @Test
    public void isOvercrowded_returnsFalse_whenAttendanceIsLessThanCapacity() {
        final int currentAttendance = 5;
        final Bar bar = Bar.builder()
                .attendance(currentAttendance)
                .maxCapacity(currentAttendance + 1)
                .build();

        assertThat("Bar is not overcrowded when attendance < capacity", bar.isOvercrowded(), is(false));
    }

    @Test
    public void isOvercrowded_returnsFalse_whenAttendanceIsSameAsCapacity() {
        final int currentAttendance = 5;
        final Bar bar = Bar.builder()
                .attendance(currentAttendance)
                .maxCapacity(currentAttendance)
                .build();

        assertThat("Bar is not overcrowded when attendance matches capacity", bar.isOvercrowded(), is(false));
    }

    @Test
    public void crowdedHistory_convertsAttendanceHistoryTo() {
        final Bar bar = Bar.builder()
                .attendanceHistory(
                        ImmutableList.of(4, 5, 6)
                )
                .maxCapacity(5)
                .build();
        final List<Boolean> expectedCrowdedHistory = ImmutableList.of(false, false, true);

        assertThat("Crowded history is generated correctly", bar.crowdedHistory(), is(expectedCrowdedHistory));
    }
}
