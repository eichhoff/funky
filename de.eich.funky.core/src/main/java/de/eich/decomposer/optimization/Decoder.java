/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer.optimization;

import java.util.List;

import org.javatuples.Pair;
import org.opt4j.core.genotype.IntegerMapGenotype;
import org.opt4j.core.genotype.PermutationGenotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.derivation.DerivationResult;
import de.eich.rewriter.derivation.Rule;
import de.eich.utils.ComparableList;

public class Decoder implements org.opt4j.core.problem.Decoder<RuleSequenceGenotype, DerivationResult> {

	private static final Logger log = LoggerFactory.getLogger(Decoder.class);
	
	@Inject
	private Problem problem;
	
	@Override
	public DerivationResult decode(RuleSequenceGenotype genotype) {
		
		List<Integer> ruleIDs;
		PermutationGenotype<Integer> ruleOrderGenotype = genotype.getRuleOrderGenotype();
		IntegerMapGenotype<Integer> ruleRepetitionGenotype = genotype.getRuleRepetitionGenotype();

		int repetition;
		Pair<Integer,Integer> repetitionLimits;
		ruleIDs = new ComparableList<Integer>();
		for(int ruleID : ruleOrderGenotype){
			repetition = ruleRepetitionGenotype.getValue(ruleID);
			if(problem.repetitionLimits != null && (repetitionLimits = problem.repetitionLimits.get(ruleID)) != null)
				repetition = repetitionLimits.getValue0() + (repetition % (repetitionLimits.getValue1() - repetitionLimits.getValue0() + 1));
			for(int i = 0; i < repetition; i++)
				ruleIDs.add(ruleID);
		}
		
		repetition = ruleRepetitionGenotype.getValue(Rule.closeGapsRuleID);
		if(problem.repetitionLimits != null && (repetitionLimits = problem.repetitionLimits.get(Rule.closeGapsRuleID)) != null)
			repetition = repetitionLimits.getValue0() + (repetition % (repetitionLimits.getValue1() - repetitionLimits.getValue0() + 1));
		for(int i = 0; i < repetition; i++)
			ruleIDs.add(Rule.closeGapsRuleID);
			
		// derivation result is taken from last rewriter in list
		int i = 0;
		Integer na = null;
		DerivationResult derivationResult = null;
		for(AbstractRewriteSystem rewriter : problem.rewriters){
			AbstractRewriteSystem.directDerivationCounter = 0;
			DerivationResult derivationResult2 = rewriter.derive(problem.blackbox.getFunctionStructure().copy(true), ruleIDs);
			
			if(derivationResult == null)
			derivationResult = derivationResult2;
			problem.directDerivationCounters.set(i, 
					problem.directDerivationCounters.get(i++) 
					+ AbstractRewriteSystem.directDerivationCounter);
			
			if(na == null)
				na = derivationResult.canonicalForm.notApplicableAtAllRuleIndex;
			else if(na != derivationResult.canonicalForm.notApplicableAtAllRuleIndex){
				log.error("DIFFERENT N/A " + ruleIDs);
				System.exit(0);
			}
		}

		if(problem.writer != null){
			if(problem.rewriters.size() == 2){
				log.info(problem.directDerivationCounters + " DELTA: " + (problem.directDerivationCounters.get(0) + AbstractRewriteSystem.directDerivationCounter2 - problem.directDerivationCounters.get(1)));
			
				try {
					problem.writer.write((problem.directDerivationCounters.get(0) + AbstractRewriteSystem.directDerivationCounter2) + ";" + problem.directDerivationCounters.get(1) + "\n");
					problem.writer.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				log.info(problem.directDerivationCounters.toString());
			}
		}
		
		return derivationResult;
	}

}
