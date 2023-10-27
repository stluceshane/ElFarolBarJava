package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.AlwaysDecisionStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AlwaysDecisionStrategyTest {
    @Mock
    private Bar bar;

    private final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();

    @Test
    public void decide_returnsTrue() {
        final AlwaysDecisionStrategy strategy = AlwaysDecisionStrategy.builder().build();

        assertThat("Returns true, regardless of bar", strategy.decide(bar, memoryProps));
    }

    @Test
    public void getName_returnsCorrectName() {
        final AlwaysDecisionStrategy strategy = AlwaysDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("Always"));
    }

    @Test
    public void clone_createsCopy() {
        final AlwaysDecisionStrategy strategy = AlwaysDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
