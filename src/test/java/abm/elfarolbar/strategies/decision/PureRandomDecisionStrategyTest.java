package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.PureRandomDecisionStrategy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PureRandomDecisionStrategyTest {
    @Mock
    private Bar bar;

    private final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();

    @Test
    public void decide_returnsTrueOrFalse_whenNoHistory() {
        final PureRandomDecisionStrategy strategy = PureRandomDecisionStrategy.builder().build();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(bar, memoryProps)));

        assertThat("Can return true with correct proportions", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(500.0, 75.0));
        assertThat("Can return false with correct proportions", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(500.0, 75.0));
    }

    @Test
    public void getName_returnsCorrectName() {
        final PureRandomDecisionStrategy strategy = PureRandomDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("PureRandom"));
    }

    @Test
    public void clone_createsCopy() {
        final PureRandomDecisionStrategy strategy = PureRandomDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
