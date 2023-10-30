package abm.elfarolbar.simulations.reporting;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SimulationInputDataset {
    private final int barCapacity;
    private final int simulationLength;
    private final List<Integer> barPreviousHistory;
    private List<PatronSetupDetailsReport> initialPatronSetupDetails;
}
