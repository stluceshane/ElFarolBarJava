package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import abm.elfarolbar.strategies.decision.RandomDecisionStrategy;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DecisionStrategyTest {
    @Test
    public void select_incrementsNumberOfUsers() {
        final RandomDecisionStrategy strategy = RandomDecisionStrategy.builder().build();

        final int numberOfSelections = RandomUtils.nextInt(10, 100);
        IntStream.range(0, numberOfSelections)
            .boxed()
            .forEach(idx -> strategy.select());

        assertThat("Number of users match number of times strategy is selected", strategy.getUsers(), is(numberOfSelections));
    }

    @Test
    public void select_incrementsNumberOfUsers_then_reset_setsUsersToZero() {
        final RandomDecisionStrategy strategy = RandomDecisionStrategy.builder().build();

        final int numberOfSelections = RandomUtils.nextInt(10, 100);
        IntStream.range(0, numberOfSelections)
            .boxed()
            .forEach(idx -> strategy.select());

        assertThat("Number of users match number of times strategy is selected", strategy.getUsers(), is(numberOfSelections));
        strategy.reset();
        assertThat("Number of users is reset", strategy.getUsers(), is(0));
    }
}
