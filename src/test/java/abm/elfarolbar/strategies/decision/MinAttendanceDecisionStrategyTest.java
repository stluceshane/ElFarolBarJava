package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.doReturn;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.MinAttendanceDecisionStrategy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MinAttendanceDecisionStrategyTest {
    @Mock
    private Bar bar;

    private final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();

    @Test
    public void decide_returnsTrueOrFalse_whenNoHistory() {
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder().build();
        doReturn(100)
            .when(bar)
            .getTotalPopulation();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(bar, memoryProps)));

        assertThat("Can return true with correct proportions", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(400.0, 75.0));
        assertThat("Can return false with correct proportions", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(600.0, 75.0));
    }

    @Test
    public void decide_returnsFalse_whenMinInHistoryIsAboveThreshold() {
        final int localMin = 40;
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder()
            .localMin(localMin)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(localMin + 1, 101))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns false", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenMinInHistoryIsAtThreshold() {
        final int localMin = 40;
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder()
            .localMin(localMin)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> localMin)
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenMinInHistoryIsBelowThreshold() {
        final int localMin = 40;
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder()
            .localMin(localMin)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(0, localMin))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar, memoryProps));
    }

    @Test
    public void default_setsThresholdTo40() {
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder().build();
        final MinAttendanceDecisionStrategy expectedStrategy = MinAttendanceDecisionStrategy.builder()
            .localMin(40)
            .build();


        assertThat("Default threshold is 40", strategy, samePropertyValuesAs(expectedStrategy));
    }

    @Test
    public void getName_returnsCorrectName() {
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("Min-40"));
    }

    @Test
    public void getName_returnsCorrectName_whenThresholdIsNotDefault() {
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder()
            .localMin(50)
            .build();

        assertThat("Returns correct name", strategy.getName(), is("Min-50"));
    }

    @Test
    public void clone_createsCopy() {
        final MinAttendanceDecisionStrategy strategy = MinAttendanceDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
