/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.List;

import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.utils.ComparableList;

public class DerivationGroupHost {

	protected RuleSequence ruleSequence;
	public FunctionStructure functionStructure;
	public final List<Integer> ruleIndices = new ComparableList<Integer>();
	public final List<RuleAppearance> ruleIDAppearances;
	
	public DerivationGroupHost(FunctionStructure source, RuleSequence ruleSequence, int nextRuleIndex) {
		this.functionStructure = source;
		this.ruleSequence = ruleSequence;
		for(int ruleIndex = 0; ruleIndex < nextRuleIndex; ruleIndex++){
			ruleIndices.add(ruleIndex);
		}
		ruleIDAppearances = ruleSequence.getRuleIDAppearancesSublist(nextRuleIndex);
	}
	
	public DerivationGroupHost(FunctionStructure source, RuleSequence ruleSequence, int nextRuleIndex, int additionalRuleIndex) {
		this.functionStructure = source;
		this.ruleSequence = ruleSequence;
		for(int ruleIndex = 0; ruleIndex < nextRuleIndex; ruleIndex++){
			ruleIndices.add(ruleIndex);
		}
		if(additionalRuleIndex == -1){
			System.err.println("fail -1 aa");
			System.exit(0);
		}
		ruleIndices.add(additionalRuleIndex);
		
		ruleIDAppearances = ruleSequence.getRuleIDAppearancesSublist(nextRuleIndex);
		
		int ruleID = ruleSequence.get(additionalRuleIndex).id;
		int count = 0;
		for(RuleAppearance ruleIDAppearance : ruleIDAppearances)
			if(ruleIDAppearance.id == ruleID) count++;
		ruleIDAppearances.add(new RuleAppearance(ruleID, count));
	}
	
}
