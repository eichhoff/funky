/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import de.eich.rewriter.derivation.DerivationResult;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.derivation.RuleSequence;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.utils.MyObservable;

@SuppressWarnings("serial")
public abstract class AbstractRewriteSystem extends TreeSet<Rule>{

	protected final MyObservable observable = new MyObservable();
	
	public static int directDerivationCounter = 0;
	public static int directDerivationCounter2 = 0;
	public static Map<Pair<Integer, Integer>, Integer> costs = new TreeMap<Pair<Integer, Integer>, Integer>();
	
	private final Set<FunctionStructure> commonEndConfigs = new HashSet<FunctionStructure>(); 
	
	// map from id to rule
	public Map<Integer, Rule> rules = new TreeMap<Integer,Rule>();
	
	public abstract DerivationResult derive(FunctionStructure functionStructure, List<Integer> ruleIDs);
	
	public DerivationResult derive(FunctionStructure source, Integer... ruleIDs){
		return derive(source, Arrays.asList(ruleIDs));
	}
	
	public void reset(){
		commonEndConfigs.clear();
	}
	
	public boolean add(Rule rule){
		boolean added = super.add(rule);
		if(added) rules.put(rule.id, rule);
		return added;
	}
	
	public boolean remove(Rule rule){
		boolean removed = super.remove(rule);
		if(removed) rules.remove(rule.id);
		return removed;
	}
	
	public void readFromXML(String pathToXMLSchema, String pathToXML){
		for(Rule rule : this){
			rule.readFromXML(pathToXMLSchema, pathToXML);
		}
	}
	
	protected boolean addValidDerivation(Pair<FunctionStructure,FunctionStructure> sourceAndTargetFunctionStructure, RuleSequence ruleSequence, DerivationResult derivationResult, int ruleIndex){
		FunctionStructure sourceFunctionStructure = sourceAndTargetFunctionStructure.getValue0();
		FunctionStructure targetFunctionStructure = sourceAndTargetFunctionStructure.getValue1();
		derivationResult.distanceToValidity = Math.min(derivationResult.distanceToValidity, targetFunctionStructure.getDistanceToValidity());
		if(!isCommonEndConfiguration(targetFunctionStructure)){
			if(targetFunctionStructure.isValid()){
				derivationResult.functionStructures.add(new Triplet<List<Integer>, FunctionStructure, FunctionStructure>(ruleSequence.getRuleIDSublist(ruleIndex), sourceFunctionStructure, targetFunctionStructure));
				commonEndConfigs.add(targetFunctionStructure);
				return true;
			} else {
				derivationResult.invalidFunctionStructures.add(new Triplet<List<Integer>, FunctionStructure, FunctionStructure>(ruleSequence.getRuleIDSublist(ruleIndex), sourceFunctionStructure, targetFunctionStructure));
				return false;
			}
		}
		return false;
	}
	
	private boolean isCommonEndConfiguration(FunctionStructure functionStructure){
		for(FunctionStructure commonConfig : commonEndConfigs){
			if(commonConfig.isomorph(functionStructure)) return true;
		}
		return false;
	}

	public static void addCost(int id1, int id2, int cost) {
		Pair<Integer, Integer> pair1 = new Pair<Integer, Integer>(id1, id2);
		Pair<Integer, Integer> pair2 = new Pair<Integer, Integer>(id2, id1);
		Integer existingCost = costs.get(pair1);
		if(existingCost != null)
			cost += existingCost;
		costs.put(pair1, cost);
		costs.put(pair2, cost);
	}
	
	public Observable getObservable() {
		return observable;
	}
	
}
