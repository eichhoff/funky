/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer.optimization;

import java.util.Arrays;
import java.util.List;

import org.opt4j.core.Objective;
import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;

import de.eich.rewriter.derivation.DerivationResult;

public class Evaluator implements org.opt4j.core.problem.Evaluator<DerivationResult> {

	private Objective foundSolutions = new Objective("foundSolutions", Sign.MAX);
	private Objective ruleSequenceLength = new Objective("ruleSequenceLength", Sign.MIN);
	private Objective percentOfRuleSequenceAchieved = new Objective("percentOfRuleSequenceAchieved", Sign.MAX);

	public Objectives evaluate(DerivationResult derivationResult) {
		Objectives objectives = new Objectives();
		objectives.add(foundSolutions, derivationResult.functionStructures.size());
		objectives.add(ruleSequenceLength, derivationResult.ruleIDs.size());
		objectives.add(percentOfRuleSequenceAchieved, derivationResult.percentOfRuleSetAchieved);
		return objectives;
	}

	public List<Objective> getObjectives() {
		return Arrays.asList(foundSolutions, ruleSequenceLength, percentOfRuleSequenceAchieved);
	}

}
