package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.strategies.decision.NeverDecisionStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NeverDecisionStrategyTest {
    @Mock
    private Bar bar;

    @Test
    public void decide_returnsFalse() {
        final NeverDecisionStrategy strategy = NeverDecisionStrategy.builder().build();

        assertThat("Returns false, regardless of bar", strategy.decide(bar), is(false));
    }

    @Test
    public void getName_returnsCorrectName() {
        final NeverDecisionStrategy strategy = NeverDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("Never"));
    }

    @Test
    public void clone_createsCopy() {
        final NeverDecisionStrategy strategy = NeverDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
