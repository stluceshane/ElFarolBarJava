package abm.elfarolbar.experiments;

import abm.elfarolbar.exceptions.ExperimentFailureException;
import abm.elfarolbar.simulations.Simulation;
import abm.elfarolbar.simulations.reporting.SimulationReport;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExperimentExecutorTest {
    @Mock
    private ExperimentDesigner experimentDesigner;

    @Mock
    private Simulation simulation1;

    @Mock
    private Simulation simulation2;

    @Mock
    private Simulation simulation3;

    @Mock
    private ObjectWriter objectWriter;

    @Mock
    private Experiment experiment;

    @Test
    public void execute_completesAllSimulations() throws Exception {
        final List<Simulation> simulationList = List.of(
                simulation1,
                simulation2,
                simulation3
        );
        doReturn(experiment).when(experimentDesigner).design();

        final String outputPath = "outputPath";
        doReturn(outputPath).when(experiment).getOutputPath();
        doReturn(simulationList).when(experiment).getSimulations();

        doReturn(SimulationReport.builder().build()).when(simulation1).generateReport();
        doReturn(SimulationReport.builder().build()).when(simulation2).generateReport();
        doReturn(SimulationReport.builder().build()).when(simulation3).generateReport();

        final ExperimentExecutor experimentExecutor = new ExperimentExecutor(experimentDesigner, objectWriter);

        final ExperimentResults results = experimentExecutor.execute();

        verify(experiment, atLeastOnce()).getOutputPath();
        verify(simulation1).execute();
        verify(simulation2).execute();
        verify(simulation3).execute();
        verify(objectWriter, times(3)).writeValue(any(File.class), any(SimulationReport.class));

        assertAll("Results are populated correctly",
                () -> assertThat("Results contains experiment", results.getExperiment(), is(experiment)),
                () -> assertThat("Results record execution time", results.getExecutionTime(), greaterThan(0L))
        );
    }

    @Test
    public void execute_throwsExperimentFailureException_whenSimulationReportingFails() throws Exception {
        final List<Simulation> simulationList = List.of(
                simulation1,
                simulation2,
                simulation3
        );
        doReturn(experiment).when(experimentDesigner).design();

        final String outputPath = "outputPath";
        doReturn(outputPath).when(experiment).getOutputPath();
        doReturn(simulationList).when(experiment).getSimulations();

        doReturn(SimulationReport.builder().build()).when(simulation1).generateReport();
        doReturn(SimulationReport.builder().build()).when(simulation2).generateReport();
        doReturn(SimulationReport.builder().build()).when(simulation3).generateReport();

        doThrow(new RuntimeException()).when(objectWriter).writeValue(any(File.class), any(SimulationReport.class));

        final ExperimentExecutor experimentExecutor = new ExperimentExecutor(experimentDesigner, objectWriter);

        assertThrows(ExperimentFailureException.class, experimentExecutor::execute);
    }

    @Test
    public void execute_throwsExperimentFailureException_whenDesignFails() throws Exception {
        doThrow(new RuntimeException()).when(experimentDesigner).design();

        final ExperimentExecutor experimentExecutor = new ExperimentExecutor(experimentDesigner, objectWriter);

        assertThrows(ExperimentFailureException.class, experimentExecutor::execute);
    }
}
