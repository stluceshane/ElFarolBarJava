package abm.elfarolbar.agents.patron;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.simulations.reporting.Reportable;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;

@Builder
@Data
public class Patron implements Reportable<PatronReport> {
    @NonNull
    private String id;
    @NonNull
    private Map<String, DecisionStrategy> decisionStrategies;
    @NonNull
    private String decisionStrategyName;
    @NonNull
    private ReplacementStrategy replacementStrategy;
    @NonNull
    private PatronMemoryProps memoryProps;
    @NonNull
    @Builder.Default
    private List<PatronHistoryEvent> history = Lists.newArrayList();
    @Builder.Default
    private int patience = 5;
    @Builder.Default
    private int lastStrategySwitchStep = 0;

    public boolean decide(final Bar bar) {
        final Map<String, Boolean> decisions = this.decisionStrategies.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().decide(bar, memoryProps)));
        this.decisionStrategies.get(this.decisionStrategyName).select();

        final boolean decision = decisions.get(this.decisionStrategyName);
        final PatronHistoryEvent patronHistoryEvent = PatronHistoryEvent.builder()
                .strategyNameToDecisionMap(decisions)
                .decision(decision)
                .strategy(this.decisionStrategyName)
                .build();

        this.history.add(patronHistoryEvent);

        if (decision) {
            this.attendBar(bar);
        }
        return decision;
    }

    public void selectNewStrategy() {
        this.lastStrategySwitchStep = getHistory().size();
        final List<PatronHistoryEvent> recentHistory = this.recentHistory();

        final Map<String, Integer> strategyNameToCorrectRecentDecisionsCountMap = recentHistory.stream()
            .map(this::getDecisionObject)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .filter(Map.Entry::getValue)
            .collect(Collectors.groupingBy(Map.Entry::getKey))
            .entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), entry.getValue().size()))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        final int maxCount = strategyNameToCorrectRecentDecisionsCountMap.values()
            .stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0);

        List<String> strategyNames = strategyNameToCorrectRecentDecisionsCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Collections.shuffle(strategyNames);
        this.decisionStrategyName = strategyNames.get(0);
    }

    public boolean shouldReplaceStrategy() {
        final int validatedPatience = Math.max(this.patience, 0);
        final int validatedLastStrategySwitchStep = Math.max(this.lastStrategySwitchStep, 0);
        return (this.getHistory().size() - validatedLastStrategySwitchStep >= validatedPatience) &&
                this.replacementStrategy.decide(this.memoryProps, this.recentHistory());
    }

    public void record(final Bar bar) {
        Optional.ofNullable(Iterables.getLast(this.history, null))
            .ifPresent(lastEvent -> {
                lastEvent.setOvercrowded(bar.isOvercrowded());
                lastEvent.setCorrect(lastEvent.getDecision() != bar.isOvercrowded());
            });
    }

    @Override
    public PatronReport generateReport() {
        return PatronReport.builder()
            .id(this.id)
            .history(this.history)
            .memoryProps(this.memoryProps)
            .patience(this.patience)
            .lastStrategySwitchStep(this.lastStrategySwitchStep)
            .build();
    }

    private void attendBar(final Bar bar) {
        bar.addPatron();
    }

    private Map<String, Boolean> getDecisionObject(final PatronHistoryEvent event) {
        return event.getStrategyNameToDecisionMap()
            .entrySet()
            .stream()
            .filter(entry -> !entry.getKey().equals(this.decisionStrategyName))
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() != event.getOvercrowded()));
    }

    private List<PatronHistoryEvent> recentHistory() {
        return this.history.subList(Math.max(0, this.history.size() - this.memoryProps.getMemoryLength()), this.history.size());
    }
}
