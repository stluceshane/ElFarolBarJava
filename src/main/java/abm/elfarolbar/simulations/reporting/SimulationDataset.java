package abm.elfarolbar.simulations.reporting;

import abm.elfarolbar.agents.patron.PatronReport;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class SimulationDataset {
    private List<Integer> attendanceHistory;
    private final List<Map<String, Integer>> strategyDistributions;
    private final List<PatronReport> patronReports;
}
