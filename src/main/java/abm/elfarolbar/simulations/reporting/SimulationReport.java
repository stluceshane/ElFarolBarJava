package abm.elfarolbar.simulations.reporting;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class SimulationReport {
    private SimulationInputDataset input;
    private SimulationDataset dataset;
    private Long executionTime;
}
