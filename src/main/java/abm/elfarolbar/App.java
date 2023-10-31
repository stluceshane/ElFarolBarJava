package abm.elfarolbar;

import abm.elfarolbar.experiments.ExperimentDesigner;
import abm.elfarolbar.experiments.ExperimentExecutor;
import abm.elfarolbar.experiments.ExperimentResults;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.decision.LastCorrectDecisionStrategy;
import abm.elfarolbar.strategies.decision.LastIncorrectDecisionStrategy;
import abm.elfarolbar.strategies.decision.NeverDecisionStrategy;
import abm.elfarolbar.strategies.replacement.FlatToleranceReplacementStrategy;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableSet;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.Set;

/**
 * Initializes and executes simulations.
 */
@Log4j2
public class App {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    private static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer(new MinimalPrettyPrinter());

    public static void main(final String[] args) {
        final LastCorrectDecisionStrategy lastCorrectStrategy = LastCorrectDecisionStrategy.builder().build();
        final LastIncorrectDecisionStrategy lastIncorrectStrategy = LastIncorrectDecisionStrategy.builder().build();
        final NeverDecisionStrategy neverDecisionStrategy = NeverDecisionStrategy.builder().build();

        final Set<DecisionStrategy> decisionStrategies = ImmutableSet.of(
                lastCorrectStrategy,
                lastIncorrectStrategy,
                neverDecisionStrategy
        );

        final FlatToleranceReplacementStrategy flatToleranceReplacementStrategy = FlatToleranceReplacementStrategy.builder().build();

        final Instant start = Instant.now();
        final ExperimentDesigner experimentDesigner = ExperimentDesigner.builder()
                .barCapacity(100)
                .distributionSize(10)
                .decisionStrategies(decisionStrategies)
                .replacementStrategy(flatToleranceReplacementStrategy)
                .initialBarAttendance(0)
                .simulationLength(500)
                .totalPatrons(100)
                .build();
        final ExperimentExecutor experimentExecutor = new ExperimentExecutor(experimentDesigner, OBJECT_WRITER);

        final ExperimentResults results = experimentExecutor.execute();
        final Instant end = Instant.now();
        log.info("Total Execution Time: {} ms", end.minusMillis(start.toEpochMilli()).toEpochMilli());
    }
}
