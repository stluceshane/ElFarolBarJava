package abm.elfarolbar.experiments;

import abm.elfarolbar.simulations.Simulation;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class Experiment {
    String id;
    String outputPath;
    List<Simulation> simulations;
}
