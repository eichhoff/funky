/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.utils.ComparableList;

public class RuleSequence {

	private final List<Rule> rules = new ComparableList<Rule>();
	public final List<RuleAppearance> ruleAppearances = new ComparableList<RuleAppearance>();
	
	// map from rule id to common configurations
	private final Map<Integer, Set<FunctionStructure>> intraRuleCommonConfigs = new TreeMap<Integer, Set<FunctionStructure>>(); 
	
	public void add(Rule rule){
		rules.add(rule);
		
		int count = 0;
		for(RuleAppearance ruleIDAppearance : ruleAppearances)
			if(ruleIDAppearance.id == rule.id) count++;
		RuleAppearance newRuleAppearance = new RuleAppearance(rule.id, count);
		ruleAppearances.add(newRuleAppearance);
	}
	
	public Rule get(int ruleIndex){
		return rules.get(ruleIndex);
	}
	
	public int size(){
		return rules.size();
	}
	
	public int getCorrespondingRuleIndex(int ruleIndex, RuleSequence otherRuleSequence){
		return otherRuleSequence.ruleAppearances.indexOf(ruleAppearances.get(ruleIndex));
	}
	
	public static int getCorrespondingRuleIndex(List<RuleAppearance> ruleIDAppearances, int ruleIndex, List<RuleAppearance> otherRuleIDAppearances){
		return otherRuleIDAppearances.indexOf(ruleIDAppearances.get(ruleIndex));
	}
	
	public List<RuleAppearance> getRuleIDAppearancesSublist(int ruleIndex){
		return new ComparableList<RuleAppearance>(ruleAppearances.subList(0, ruleIndex)); // ruleIndex exclusive
	}
	
	public List<Integer> getRuleIDSublist(int maxRuleIndex){ // maxRuleIndex inclusive
		List<Integer> applicableRuleIDs = new ComparableList<Integer>();
		for(int ruleIndex = 0; ruleIndex <= maxRuleIndex; ruleIndex++){
			applicableRuleIDs.add(rules.get(ruleIndex).id);
		}
		return applicableRuleIDs;
	}
	
	// permutations over multiple applications of the same rule are avoided by checking against common configurations
	public boolean isIntraRuleCommonConfiguration(int ruleIndex, DirectDerivation directDerivation){
		int ruleID = rules.get(ruleIndex).id;
		Set<FunctionStructure> subset = intraRuleCommonConfigs.get(ruleID);
		if(subset == null)
			intraRuleCommonConfigs.put(ruleID, subset = new HashSet<FunctionStructure>());
		
		// perform isomorphism testing against existing common configurations
		for(FunctionStructure commonConfig : subset)
			if(commonConfig.isomorph(directDerivation.target)){
				return true;
			}
			
		// add as common configuration
		subset.add(directDerivation.target);
	
		return false;
	}
	
	public void resetIntraRuleCommonConfigurations(int ruleIndex){
		int ruleID = rules.get(ruleIndex).id;
		Set<FunctionStructure> subsetA = intraRuleCommonConfigs.get(ruleID);
		if(subsetA != null) subsetA.clear();
	}
	
	public void readFromXML(String pathToXMLSchema, String pathToXML){
		for(Rule rule : rules){
			rule.readFromXML(pathToXMLSchema, pathToXML);
		}
	}
	
	public String toString(){
		return rules.toString();
	}
	
}
