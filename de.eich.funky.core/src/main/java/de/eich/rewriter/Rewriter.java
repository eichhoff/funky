/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;

import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.derivation.Derivation;
import de.eich.rewriter.derivation.DerivationGroupCache;
import de.eich.rewriter.derivation.DerivationResult;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.derivation.RuleSequence;
import de.eich.rewriter.functionstructure.FunctionStructure;

@SuppressWarnings("serial")
public class Rewriter extends AbstractRewriteSystem {

	private Set<Pair<FunctionStructure,FunctionStructure>> resultingFunctionStructures;
	
	public static enum RewriteMode{
		NO_TESTING,
		TEST_BEST,
		TEST_ALL,
		TEST_ALL_THEN_BEST,
		TEST_BEST_THEN_ALL
	}
	
	public RewriteMode rewriteMode = RewriteMode.NO_TESTING;

	public DerivationGroupCache derivationGroupCache = new DerivationGroupCache();
	
	public double minDistanceToValidity = 1.0;
	public double maxPercentOfRuleSetAchieved = 0.0;
	
	public DerivationResult derive(FunctionStructure functionStructure, List<Integer> ruleIDs){
		DerivationResult derivationResult = new DerivationResult();
		
		RuleSequence sequence = new RuleSequence();
		for(Integer id : ruleIDs){
			Rule rule = rules.get(id);
			rule.reset();
			sequence.add(rule);
		}
		
		derivationResult.ruleIDs = ruleIDs;
		
		Set<FunctionStructure> sources = new HashSet<FunctionStructure>();
		sources.add(functionStructure);
		
		Derivation canonicalForm2 = new Derivation(sequence, this);
		
		derivationResult.canonicalForm = canonicalForm2;
		if(!apply(canonicalForm2, 0, sources, derivationResult)) return derivationResult;
		
		double applicableOutOfRuleSet = 0.0;
		int previousRuleID = -1;
		for(int ruleIndex = 0; ruleIndex < ruleIDs.size(); ruleIndex++){
			int ruleID = ruleIDs.get(ruleIndex);
			if(ruleIndex == derivationResult.notApplicableAtAllRuleIndex) break;
			if(ruleID == previousRuleID) continue;
			applicableOutOfRuleSet++;
			previousRuleID = ruleID;
		}
		derivationResult.percentOfRuleSetAchieved = applicableOutOfRuleSet / this.size();
		
		if(!derivationResult.functionStructures.isEmpty() && rewriteMode == RewriteMode.TEST_ALL_THEN_BEST){
			rewriteMode = RewriteMode.TEST_BEST;
		}
		
		if(rewriteMode == RewriteMode.TEST_ALL || rewriteMode == RewriteMode.TEST_ALL_THEN_BEST){
			// do shifting for all
			derivationGroupCache.addAllRuleSubsequences(canonicalForm2);
			applyShifts(canonicalForm2);
		} else if(rewriteMode == RewriteMode.NO_TESTING){
			derivationGroupCache.addAllRuleSubsequences(canonicalForm2);
		}
		
		derivationGroupCache.addAllDerivationGroups(canonicalForm2); // is required for computing the next derivation - don't put this solely into evolutionary post processing
		
		minDistanceToValidity = Math.min(minDistanceToValidity, derivationResult.distanceToValidity);
		maxPercentOfRuleSetAchieved = Math.max(maxPercentOfRuleSetAchieved, derivationResult.percentOfRuleSetAchieved);
		
		return derivationResult;
	}
	
	private boolean apply(Derivation canonicalForm, int ruleIndex, Set<FunctionStructure> sources, DerivationResult derivationResult){
		for(; ruleIndex < canonicalForm.ruleSequence.size(); ruleIndex++){
//			System.out.println(sources);
//			System.out.println(canonicalForm.ruleSequence.get(ruleIndex).id + "  " + new Date());
//			long start = System.currentTimeMillis();
			
			derivationResult.percentOfRuleSequenceAchieved = ((double) ruleIndex) / canonicalForm.ruleSequence.size();
			
			resultingFunctionStructures = canonicalForm.derive(ruleIndex, sources);
			
			if(resultingFunctionStructures == null) return false;
			
			observable.setChangedAndNotifyObservers();
			
			if(canonicalForm.notApplicableAtAllRuleIndex != -1){ // this rule could not be applied
				derivationResult.notApplicableAtAllRuleIndex = canonicalForm.notApplicableAtAllRuleIndex;
				break;
			}
			
			// check if there are valid function structures among the current productions
			int i = 0;
			for(Pair<FunctionStructure,FunctionStructure> functionStructure : resultingFunctionStructures){
				
				// TODO: remove this shit when done
//				if(ruleIndex <= 11) 
//					new FunctionStructureView(functionStructure.getValue1(), "rule " + ruleIndex + " / " + (i++));
				
				addValidDerivation(functionStructure, canonicalForm.ruleSequence, derivationResult, ruleIndex);
			}
			
//			if(System.currentTimeMillis() - start > 15000){
//				System.err.println("took too long");
//				return false;
//			}
			
		}
		return true;
	}
	
	public void postProcessEvolutionary(Set<DerivationResult> derivationResultsParents, Set<DerivationResult> derivationResultsAll) {
		if(rewriteMode == RewriteMode.TEST_BEST || rewriteMode == RewriteMode.TEST_BEST_THEN_ALL){
			int a = AbstractRewriteSystem.directDerivationCounter;
			
			// add all computed derivations to cache
			for(DerivationResult derivationResult : derivationResultsAll){
				derivationGroupCache.addAllRuleSubsequences(derivationResult.canonicalForm);
				// switch test modus if solution is found 
				// in the next iteration this if block will not be visited again and shifting is done above
				if(!derivationResult.functionStructures.isEmpty() && rewriteMode == RewriteMode.TEST_BEST_THEN_ALL){
					rewriteMode = RewriteMode.TEST_ALL;
				}
			}
			
			// do shifting for parents only
			for(DerivationResult derivationResult : derivationResultsParents){
				applyShifts(derivationResult.canonicalForm);
				derivationGroupCache.addAllDerivationGroups(derivationResult.canonicalForm); // update derivation groups
			}
			
			AbstractRewriteSystem.directDerivationCounter2 += AbstractRewriteSystem.directDerivationCounter - a;
		}
		
		// compute scores
//		for(DerivationResult derivationResult : derivationResultsAll){
//			minDistanceToValidity = Math.min(minDistanceToValidity, derivationResult.distanceToValidity);
//			maxPercentOfRuleSetAchieved = Math.max(maxPercentOfRuleSetAchieved, derivationResult.percentOfRuleSetAchieved);
//		}
	}
	
	public void applyShifts(Derivation canonicalForm){
		int maxRuleIndex;
		if(canonicalForm.notApplicableAtAllRuleIndex != -1)
			// explicitly include not applicable rule index to move it towards beginning of rule sequence, gives option for failing fast
			maxRuleIndex = canonicalForm.notApplicableAtAllRuleIndex;
		else
			maxRuleIndex = canonicalForm.ruleSequence.size() -1;
		
		for(int ruleIndex = 1; ruleIndex <= maxRuleIndex; ruleIndex++){
			// must be incremental, otherwise independenceTest with higher rule indices won't work
			if(canonicalForm.ruleSequence.get(ruleIndex).id == Rule.closeGapsRuleID) continue;
			
			canonicalForm.applyShifts(ruleIndex);
		}
	}

	public Set<Pair<FunctionStructure,FunctionStructure>> getResultingFunctionStructures() {
		return resultingFunctionStructures;
	}
	
	public void reset(){
		super.reset();
		minDistanceToValidity = 1.0;
		maxPercentOfRuleSetAchieved = 0.0;
	}
	
}
