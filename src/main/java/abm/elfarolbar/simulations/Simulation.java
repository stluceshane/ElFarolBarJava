package abm.elfarolbar.simulations;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.Patron;
import abm.elfarolbar.agents.patron.PatronReport;
import abm.elfarolbar.simulations.reporting.PatronSetupDetailsReport;
import abm.elfarolbar.simulations.reporting.Reportable;
import abm.elfarolbar.simulations.reporting.SimulationDataset;
import abm.elfarolbar.simulations.reporting.SimulationInputDataset;
import abm.elfarolbar.simulations.reporting.SimulationReport;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Builder
@Getter
@Log4j2
public class Simulation implements Reportable<SimulationReport> {
    @NonNull
    private final String simulationId;
    @NonNull
    private final List<Integer> barPreviousHistory;
    @NonNull
    private final List<PatronSetupDetails> initialPatronSetupDetails;
    @NonNull
    private final Bar bar;
    @NonNull
    private final List<Patron> patrons;
    @NonNull
    private final Set<DecisionStrategy> decisionStrategies;
    @NonNull
    private final Set<ReplacementStrategy> replacementStrategies;
    @NonNull
    private final List<Map<String, Integer>> strategyDistributions;

    private final int simulationLength;

    public void execute() {
        IntStream.range(1, this.simulationLength + 1)
                .forEach(step -> {
                    if (step % 50 == 0) {
                        log.info("{} \tStep {}", simulationId, step);
                    }
                    this.simulate();
                });
    }

    private void simulate() {
        this.decide();
        this.record();
        this.replaceAgentStrategies();
        this.resetRound();
    }

    private void decide() {
        this.patrons.parallelStream()
                .forEach(patron -> patron.decide(this.bar));
    }

    private void record() {
        this.bar.record();
        this.patrons.parallelStream()
                .forEach(patron -> patron.record(this.bar));
        this.strategyDistributions.add(
                this.decisionStrategies
                        .stream()
                        .collect(Collectors.toMap(DecisionStrategy::getName, DecisionStrategy::getUsers))
        );
    }

    private void replaceAgentStrategies() {
        this.patrons.parallelStream()
                .filter(Patron::shouldReplaceStrategy)
                .forEach(Patron::selectNewStrategy);
    }

    private void resetRound() {
        this.bar.reset();
        this.decisionStrategies
                .parallelStream()
                .forEach(DecisionStrategy::reset);
    }

    @Override
    public SimulationReport generateReport() {
        final List<PatronSetupDetailsReport> initialPatronSetupDetailsReport =
                this.initialPatronSetupDetails.stream()
                        .map(patronSetupDetails -> PatronSetupDetailsReport.builder()
                                .decisionStrategyName(patronSetupDetails.getDecisionStrategyName())
                                .count(patronSetupDetails.getCount())
                                .build())
                        .collect(Collectors.toList());

        final SimulationInputDataset input = SimulationInputDataset.builder()
                .barCapacity(this.bar.getMaxCapacity())
                .simulationLength(this.simulationLength)
                .barPreviousHistory(this.barPreviousHistory)
                .initialPatronSetupDetails(initialPatronSetupDetailsReport)
                .build();

        final List<PatronReport> patronReports = this.patrons.stream()
                .map(Patron::generateReport)
                .collect(Collectors.toList());

        final SimulationDataset dataset = SimulationDataset.builder()
                .attendanceHistory(this.bar.getAttendanceHistory())
                .strategyDistributions(this.strategyDistributions)
                .patronReports(patronReports)
                .build();

        return SimulationReport.builder()
                .input(input)
                .dataset(dataset)
                .build();
    }
}
