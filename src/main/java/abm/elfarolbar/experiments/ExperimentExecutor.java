package abm.elfarolbar.experiments;

import abm.elfarolbar.exceptions.ExperimentFailureException;
import abm.elfarolbar.exceptions.SimulationReportingFailureException;
import abm.elfarolbar.simulations.Simulation;
import abm.elfarolbar.simulations.reporting.SimulationReport;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.time.Instant;

@AllArgsConstructor
@Log4j2
public class ExperimentExecutor {
    private final ExperimentDesigner experimentDesigner;
    private final ObjectWriter objectWriter;

    public ExperimentResults execute() {
        try {
            final Experiment experiment = experimentDesigner.design();

            final Instant start = Instant.now();
            experiment.getSimulations()
                    .parallelStream()
                    .forEach(simulation -> startSimulation(simulation, experiment.getOutputPath()));
            final Instant end = Instant.now();
            final long executionTime = end.minusMillis(start.toEpochMilli()).toEpochMilli();
            log.info("Total Execution Time: {} ms", executionTime);

            return ExperimentResults.builder()
                    .experiment(experiment)
                    .executionTime(executionTime)
                    .build();
        } catch (final Exception ex) {
            throw new ExperimentFailureException("Experiment failed to complete", ex);
        }
    }

    private void startSimulation(final Simulation simulation, final String outputPath) {
        final Instant start = Instant.now();
        simulation.execute();
        final Instant end = Instant.now();

        final Long executionTime = end.toEpochMilli() - start.toEpochMilli();
        log.info("Simulation {} Execution Time: {} ms", simulation.getSimulationId(), executionTime);
        final SimulationReport simulationReport = simulation.generateReport().toBuilder()
                .executionTime(executionTime)
                .build();
        try {
            objectWriter.writeValue(new File(String.format("%s/%s.json", outputPath, simulation.getSimulationId())), simulationReport);
        } catch (final Exception ex) {
            throw new SimulationReportingFailureException("Failed to write simulation results to file system", ex);
        }
    }
}
