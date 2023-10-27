package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.doReturn;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.LastCorrectDecisionStrategy;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LastCorrectDecisionStrategyTest {
    @Mock
    private Bar bar;

    private final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();

    @Test
    public void decide_returnsTrue_whenLastDecisionIsNotCrowded() {
        final LastCorrectDecisionStrategy strategy = LastCorrectDecisionStrategy.builder().build();
        doReturn(ImmutableList.of(false))
            .when(bar)
            .crowdedHistory();

        assertThat("Returns true, if last bar attendance was not crowded", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenLastDecisionIsNotCrowdedWithPreviousCrowdedHistory() {
        final LastCorrectDecisionStrategy strategy = LastCorrectDecisionStrategy.builder().build();
        doReturn(ImmutableList.of(true, true, true, true, true, false))
            .when(bar)
            .crowdedHistory();

        assertThat("Returns true, if last bar attendance was not crowded", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenLastDecisionIsCrowded() {
        final LastCorrectDecisionStrategy strategy = LastCorrectDecisionStrategy.builder().build();
        doReturn(ImmutableList.of(true))
            .when(bar)
            .crowdedHistory();

        assertThat("Returns false, if last bar attendance was crowded", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenLastDecisionIsCrowdedWithPreviousNotCrowdedHistory() {
        final LastCorrectDecisionStrategy strategy = LastCorrectDecisionStrategy.builder().build();
        doReturn(ImmutableList.of(false, false, false, false, false, true))
            .when(bar)
            .crowdedHistory();

        assertThat("Returns false, if last bar attendance was crowded", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrueOrFalse_whenNoHistory() {
        final LastCorrectDecisionStrategy strategy = LastCorrectDecisionStrategy.builder().build();
        doReturn(ImmutableList.of())
            .when(bar)
            .crowdedHistory();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 50)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(bar, memoryProps)));

        assertThat("Can return true if bar has no history", not(mapResults.get(Boolean.TRUE).isEmpty()));
        assertThat("Can return false if bar has no history", not(mapResults.get(Boolean.FALSE).isEmpty()));
    }

    @Test
    public void getName_returnsCorrectName() {
        final LastCorrectDecisionStrategy strategy = LastCorrectDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("LastCorrect"));
    }

    @Test
    public void clone_createsCopy() {
        final LastCorrectDecisionStrategy strategy = LastCorrectDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
