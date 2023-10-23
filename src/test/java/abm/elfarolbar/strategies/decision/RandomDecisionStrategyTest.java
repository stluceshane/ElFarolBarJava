package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.doReturn;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.strategies.decision.RandomDecisionStrategy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RandomDecisionStrategyTest {
    @Mock
    private Bar bar;

    @Test
    public void decide_returnsTrueOrFalse_whenNoHistory() {
        final RandomDecisionStrategy strategy = RandomDecisionStrategy.builder().build();
        doReturn(100)
            .when(bar)
            .getTotalPopulation();
        doReturn(60)
            .when(bar)
            .getMaxCapacity();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(bar)));

        assertThat("Can return true with correct proportions", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(600.0, 100.0));
        assertThat("Can return false with correct proportions", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(400.0, 100.0));
    }

    @Test
    public void getName_returnsCorrectName() {
        final RandomDecisionStrategy strategy = RandomDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("Random"));
    }

    @Test
    public void clone_createsCopy() {
        final RandomDecisionStrategy strategy = RandomDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
