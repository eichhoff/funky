/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.opt4j.core.genotype.IntegerMapGenotype;
import org.opt4j.core.genotype.PermutationGenotype;

import com.google.inject.Inject;

import de.eich.rewriter.derivation.Rule;

public class Creator implements org.opt4j.core.problem.Creator<RuleSequenceGenotype> {

	@Inject
	private Problem problem;
	
	private final int lowerBoundRepetition = 0;
	private final int upperBoundRepetition = 10;

	private Random random = new Random();
	
	public RuleSequenceGenotype create() {
		RuleSequenceGenotype genotype = new RuleSequenceGenotype();
		
		PermutationGenotype<Integer> ruleOrderGenotype = new PermutationGenotype<Integer>(problem.ruleIDs);
		Collections.shuffle(ruleOrderGenotype);
		genotype.setRuleOrderGenotype(ruleOrderGenotype);
		
		List<Integer> withCloseGapsRule = new ArrayList<Integer>(problem.ruleIDs);
		withCloseGapsRule.add(Rule.closeGapsRuleID);
		IntegerMapGenotype<Integer> ruleRepetitionGenotype = new IntegerMapGenotype<Integer>(withCloseGapsRule, lowerBoundRepetition, upperBoundRepetition);
		ruleRepetitionGenotype.init(random);
		genotype.setRuleRepetitionGenotype(ruleRepetitionGenotype);

		Iterator<List<Integer>> iterator = null;
		if(problem.promisingRuleIDs != null && (iterator = problem.promisingRuleIDs.iterator()).hasNext()){
			List<Integer> promisingRuleIDs = iterator.next();
			iterator.remove();
			
			ruleOrderGenotype.clear();
			ruleRepetitionGenotype.clear();
			
			for(int ruleID : promisingRuleIDs)
				if(!ruleOrderGenotype.contains(ruleID) && ruleID != Rule.closeGapsRuleID)
					ruleOrderGenotype.add(ruleID);
			
			for(int ruleID : withCloseGapsRule) // all must be present
				if(!ruleOrderGenotype.contains(ruleID) && ruleID != Rule.closeGapsRuleID)
					ruleOrderGenotype.add(ruleID);
			
			for(int ruleID : withCloseGapsRule){
				int ruleCount = 0;
				for(int ruleID2 : promisingRuleIDs)
					if(ruleID == ruleID2) ruleCount++;
				ruleRepetitionGenotype.setValue(ruleID, ruleCount);
			}
		}
		
		return genotype;
	}
}
