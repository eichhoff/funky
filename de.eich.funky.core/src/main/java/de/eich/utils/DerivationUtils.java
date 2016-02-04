/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.utils;

import java.util.List;
import java.util.Map;

import de.eich.rewriter.derivation.RuleAppearance;
import de.eich.rewriter.derivation.RuleSequence;

public class DerivationUtils {

	public static int getCorrespondingRuleIndex(RuleSequence fromRuleSequence, int fromRuleIndex, RuleSequence toRuleSequence){
		return fromRuleSequence.getCorrespondingRuleIndex(fromRuleIndex, toRuleSequence);
	}
	
	public static Map<Integer, Integer> getCorrespondingRuleIndices(Map<Integer, Integer> ruleIndexMap, RuleSequence fromRuleSequence, List<Integer> fromRuleIndices, RuleSequence toRuleSequence){
		ruleIndexMap.clear();
		for(int fromRuleIndex : fromRuleIndices){
			int toRuleIndex = getCorrespondingRuleIndex(fromRuleSequence, fromRuleIndex, toRuleSequence);
			if(toRuleIndex != -1){
				ruleIndexMap.put(fromRuleIndex, toRuleIndex);
			}
		}
		return ruleIndexMap;
	}
	
	public static Map<Integer, Integer> getCorrespondingRuleIndicesInSequence(Map<Integer, Integer> ruleIndexMap, RuleSequence fromRuleSequence, List<Integer> fromRuleIndices, RuleSequence toRuleSequence, int toRuleIndexStart){
		ruleIndexMap.clear();
		for(int toRuleIndex = toRuleIndexStart; toRuleIndex < toRuleSequence.size(); toRuleIndex++){
			int fromRuleIndex = getCorrespondingRuleIndex(toRuleSequence, toRuleIndex, fromRuleSequence);
			if(fromRuleIndex != -1 && fromRuleIndices.contains(fromRuleIndex)){
				ruleIndexMap.put(fromRuleIndex, toRuleIndex);
			} else {
				break;
			}
		}
		return ruleIndexMap;
	}
	
	public static List<RuleAppearance> getRuleIDAppearances(List<Integer> ruleIDs){
		List<RuleAppearance> ruleIDAppearances = new ComparableList<RuleAppearance>();
		
		for(int ruleID : ruleIDs){
			int nextAppearance = 0;
			for(RuleAppearance ruleIDAppearance : ruleIDAppearances)
				if(ruleIDAppearance.id == ruleID) nextAppearance++;
			ruleIDAppearances.add(new RuleAppearance(ruleID, nextAppearance));
		}
		return ruleIDAppearances;
	}
	
}
