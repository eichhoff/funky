/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.javatuples.Pair;

import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.LabeledGraphMapping;
import de.eich.utils.ComparableList;
import de.eich.utils.DerivationUtils;
import de.eich.utils.MathUtils;

public class DerivationGroupCache {
	
	private final Set<Pair<Integer, Integer>> dependentIDPairs = new TreeSet<Pair<Integer, Integer>>();
	private final Map<Integer, Set<DerivationGroup>> cachedDerivationGroups = new TreeMap<Integer, Set<DerivationGroup>>();
	private final Set<List<Integer>> testedRuleSubsequences = new TreeSet<List<Integer>>();
	
	private Map<Integer, Integer> ruleIndexMapIndependent = new TreeMap<Integer, Integer>();
	
	private final DerivationGroupComparator comparator = new DerivationGroupComparator();

	public static class DerivationGroupComparator implements Comparator<DerivationGroup> {
	    public int compare(DerivationGroup a, DerivationGroup b) {
	    	int difference = b.independentIndices.size() - a.independentIndices.size();
	    	if(difference == 0)
	    		return b.hashCode() - a.hashCode();
	    	return difference;
	    }
	}
	
	public void addDependentIDPair(int id1, int id2){
		Pair<Integer, Integer> dependentIDPair = new Pair<Integer, Integer>(id1, id2);
		dependentIDPairs.add(dependentIDPair);
	}
	
	public boolean isDependentIDPair(int id1, int id2){
		Pair<Integer, Integer> dependentIDPair1 = new Pair<Integer, Integer>(id1, id2);
		Pair<Integer, Integer> dependentIDPair2 = new Pair<Integer, Integer>(id2, id1);
		return dependentIDPairs.contains(dependentIDPair1) || dependentIDPairs.contains(dependentIDPair2);
	}

	public void clearCache(int id){
		for(Iterator<Pair<Integer, Integer>> iterator = dependentIDPairs.iterator(); iterator.hasNext();)
			if(iterator.next().contains(id)) iterator.remove();
		
		// remove all derivation groups that contain id as independent id
		Set<DerivationGroup> targetDerivationGroups = cachedDerivationGroups.get(id);
		if(targetDerivationGroups != null){
			cachedDerivationGroups.remove(id);
			for(Set<DerivationGroup> derivationGroups : cachedDerivationGroups.values()){
				for(Iterator<DerivationGroup> iterator = derivationGroups.iterator(); iterator.hasNext();)
					if(targetDerivationGroups.contains(iterator.next())) iterator.remove();
			}
		}
		
		// remove all derivation groups that contain id in host graph
		for(Set<DerivationGroup> derivationGroups : cachedDerivationGroups.values()){
			for(Iterator<DerivationGroup> iterator = derivationGroups.iterator(); iterator.hasNext();){
				List<RuleAppearance> ruleIDAppearances = iterator.next().derivationGroupHost.ruleIDAppearances;
				boolean contained = false;
				for(RuleAppearance ruleAppearance : ruleIDAppearances){
					if(ruleAppearance.id == id){
						contained = true;
						break;
					}
				}
				if(contained) iterator.remove();
			}
		}
		
		for(Iterator<List<Integer>> iterator = testedRuleSubsequences.iterator(); iterator.hasNext();)
			if(iterator.next().contains(id)) iterator.remove();
	}
	
	public boolean wasNotApplicableBefore(Derivation derivation, int ruleIndexCandidate, int derivationGroupSetIndex){
		RuleSequence ruleSequence = derivation.ruleSequence;
		Set<DerivationGroup> derivationGroups = cachedDerivationGroups.get(ruleSequence.ruleAppearances.get(ruleIndexCandidate).id);
		if(derivationGroups == null) return false;
		DerivationGroupSet derivationGroupSet = derivation.derivationGroupSets.get(derivationGroupSetIndex);
		for(DerivationGroup derivationGroup1 : derivationGroupSet){
			boolean match = false;
			for(DerivationGroup derivationGroup2 : derivationGroups){
				if(derivationGroup2.currentCanonicalForm.notApplicableAtAllRuleIndex == 
						DerivationUtils.getCorrespondingRuleIndex(ruleSequence, ruleIndexCandidate, derivationGroup2.currentCanonicalForm.ruleSequence) 
						&& derivationGroup1.derivationGroupHost.functionStructure.isomorph(derivationGroup2.derivationGroupHost.functionStructure)){
					match = true;
					break;
				}
			}
			if(!match) return false;
		}
		// found isomorphic duplicates for every host graph and for each of these the rule wasn't applicable
		return true;
	}
	
	public boolean tested(Derivation canonicalForm, int ruleIndexCandidate, int derivationGroupSetIndex){
		if(canonicalForm.derivationGroupSets.get(derivationGroupSetIndex).independentIndices.contains(ruleIndexCandidate)) return false;
		
		// build up rule sequence up to derivation group where candidate shall be added to
		int i = 0;
		List<Integer> ruleSubSequence = new ComparableList<Integer>();
		for(int ruleIndex = 0; ruleIndex < ruleIndexCandidate && i < derivationGroupSetIndex; ruleIndex++){
			int ruleID = canonicalForm.ruleSequence.get(ruleIndex).id;
			if(canonicalForm.derivationGroupSets.get(i).independentIndices.contains(ruleIndex)){
				ruleSubSequence.add(ruleID);
			} else if (++i < derivationGroupSetIndex && canonicalForm.derivationGroupSets.get(i).independentIndices.contains(ruleIndex)){
				ruleSubSequence.add(ruleID);
			} else {
				break;
			}
		}

		List<Integer> ruleSubSequenceWithAdditionals = new ComparableList<Integer>(ruleSubSequence);
		ruleSubSequenceWithAdditionals.add(canonicalForm.ruleSequence.get(ruleIndexCandidate).id);
		if(!testedRuleSubsequences.contains(ruleSubSequenceWithAdditionals)){
			return false;
		} 
		
		// make variations of independent rule indices of derivation group where candidate shall be added to together with candidate as last index
		Set<List<Integer>> additionalRuleIndicesVariations = MathUtils.getVariations(canonicalForm.derivationGroupSets.get(derivationGroupSetIndex).independentIndices, false);
		for(List<Integer> additionalRuleIndicesVariation : additionalRuleIndicesVariations){
			ruleSubSequenceWithAdditionals = new ComparableList<Integer>(ruleSubSequence);
			for(int ruleIndex : additionalRuleIndicesVariation){
				ruleSubSequenceWithAdditionals.add(canonicalForm.ruleSequence.get(ruleIndex).id);
			}
			ruleSubSequenceWithAdditionals.add(canonicalForm.ruleSequence.get(ruleIndexCandidate).id);
			
			if(!testedRuleSubsequences.contains(ruleSubSequenceWithAdditionals)){
				return false;
			}
		}
		
		return true;
	}
	
	public void addAllRuleSubsequences(Derivation derivation){
		for(int lastRuleIndex = 0; lastRuleIndex < derivation.ruleSequence.size(); lastRuleIndex++){
			List<Integer> ruleSubSequence = new ComparableList<Integer>();
			for(int ruleIndex = 0; ruleIndex <= lastRuleIndex; ruleIndex++)
				ruleSubSequence.add(derivation.ruleSequence.get(ruleIndex).id);
			testedRuleSubsequences.add(ruleSubSequence);
		}
	}
	
	private void addDerivationGroup(Derivation derivation, int ruleIndex, DerivationGroup derivationGroup){
		if(derivationGroup.originalGroup != null && derivationGroup.unchanged) return;

		RuleAppearance ruleIDAppearance = derivation.ruleSequence.ruleAppearances.get(ruleIndex);
		
		Set<DerivationGroup> subList = cachedDerivationGroups.get(ruleIDAppearance.id);
		if(subList == null) {
			cachedDerivationGroups.put(ruleIDAppearance.id, subList = new TreeSet<DerivationGroup>(comparator));
		} else {
			for (Iterator<DerivationGroup> iterator = subList.iterator(); iterator.hasNext();) {
				DerivationGroup oldDerivationGroup = iterator.next();

				if(oldDerivationGroup.derivationGroupHost.functionStructure.different(derivationGroup.derivationGroupHost.functionStructure)) continue;
				
				// corresponding indices
				DerivationUtils.getCorrespondingRuleIndicesInSequence(ruleIndexMapIndependent, oldDerivationGroup.currentCanonicalForm.ruleSequence, oldDerivationGroup.independentIndices, derivation.ruleSequence, ruleIndex);
				
				if(ruleIndexMapIndependent.values().containsAll(derivationGroup.independentIndices)) {
					LabeledGraphMapping mapping = oldDerivationGroup.derivationGroupHost.functionStructure.isomorphMapping(derivationGroup.derivationGroupHost.functionStructure, false);
					if(mapping == null) continue;
					// new is fully contained in existing
					return;
				} else if(ruleIndexMapIndependent.keySet().containsAll(oldDerivationGroup.independentIndices)) {
					LabeledGraphMapping mapping = oldDerivationGroup.derivationGroupHost.functionStructure.isomorphMapping(derivationGroup.derivationGroupHost.functionStructure, false);
					if(mapping == null) continue;
					// existing is fully contained in new
					iterator.remove();
				}
			}
		}
		
		if(!subList.contains(derivationGroup)){
			subList.add(derivationGroup);
		}
	}
	
	public void addAllDerivationGroups(Derivation derivation){
		for(DerivationGroupSet set : derivation.derivationGroupSets){
			for(DerivationGroup group : set){
				for(int ruleIndex : group.independentIndices){
					addDerivationGroup(derivation, ruleIndex, group);
				}
			}
		}
	}
	
	public void addReversedDerivationGroups(DerivationGroup derivationGroup, Derivation derivation) {
		Derivation newDerivation = new Derivation(derivation.ruleSequence, derivation.rewriter);
			if(derivationGroup.candidateManager.reversedDerivationGroup1 == null) return;
			DerivationGroup newGroup = derivationGroup.candidateManager.reversedDerivationGroup1;
			newGroup.currentCanonicalForm = newDerivation;
			for(int ruleIndex : newGroup.independentIndices){
				addDerivationGroup(newDerivation, ruleIndex, newGroup);
			}
			if(derivationGroup.candidateManager.reversedDerivationGroup2 == null) return;
			newGroup = derivationGroup.candidateManager.reversedDerivationGroup2;
			newGroup.currentCanonicalForm = newDerivation;
			for(int ruleIndex : newGroup.independentIndices){
				addDerivationGroup(newDerivation, ruleIndex, newGroup);
			}
	}
	
	public DerivationGroup getDerivationGroup(FunctionStructure newSource, RuleSequence newRuleSequence, DerivationGroup newParent, int newRuleIndex, Derivation canonicalForm){
		DerivationGroup newGroup = null;
		
		int maxLength = 0;
		Set<DerivationGroup> subList = cachedDerivationGroups.get(newRuleSequence.ruleAppearances.get(newRuleIndex).id);
		if(subList != null){
			for(DerivationGroup oldDerivationGroup : subList){
				if(oldDerivationGroup.independentIndices.size() <= maxLength) break; // must be higher than max length to yield a higher max length
				
				if(oldDerivationGroup.derivationGroupHost.functionStructure.different(newSource)) continue;
				
				DerivationUtils.getCorrespondingRuleIndicesInSequence(ruleIndexMapIndependent, oldDerivationGroup.currentCanonicalForm.ruleSequence, oldDerivationGroup.independentIndices, newRuleSequence, newRuleIndex);
				// must contain current rule application
				if(!ruleIndexMapIndependent.containsValue(newRuleIndex)) continue;
				
				int length = ruleIndexMapIndependent.size();
				if(length <= maxLength) continue;
				
				LabeledGraphMapping mapping = oldDerivationGroup.derivationGroupHost.functionStructure.isomorphMapping(newSource, false);
				if(mapping == null) continue;
				
				maxLength = length;
				
				if(newParent == null){
					newGroup = new DerivationGroup(oldDerivationGroup.derivationGroupHost, canonicalForm);
				} else {
					newGroup = new DerivationGroup(new DerivationGroupHost(newSource, newRuleSequence, newRuleIndex), newParent);
					newGroup.originalGroupSourceMapping = mapping;
				}
				
				newGroup.originalGroup = oldDerivationGroup;
				newGroup.getAllMembersFromOriginal(ruleIndexMapIndependent);
			}
		}
		
		return newGroup;
	}
	
	// check for same host graph
	public Pair<Set<Occurrence>, Set<DirectDerivation>> getCachedDirectDerivations(FunctionStructure source, RuleSequence ruleSequence, int ruleIndex, Set<Occurrence> existing){
		Set<DirectDerivation> directDerivations = new HashSet<DirectDerivation>();
		Set<Occurrence> matched = new TreeSet<Occurrence>();
		
		Set<DerivationGroup> derivationGroups = cachedDerivationGroups.get(ruleSequence.ruleAppearances.get(ruleIndex).id);
		if(derivationGroups == null) return null;
		
		for(DerivationGroup derivationGroup : derivationGroups){
			int ruleIndexOld = DerivationUtils.getCorrespondingRuleIndex(ruleSequence, ruleIndex, derivationGroup.currentCanonicalForm.ruleSequence);
			LabeledGraphMapping mapping;
			if(derivationGroup.independentIndices.contains(ruleIndexOld)
					&& (mapping = derivationGroup.derivationGroupHost.functionStructure.isomorphMapping(source, true)) != null){
				
				Set<DirectDerivation> oldMemberDirectDerivations = derivationGroup.memberDirectDerivations.get(ruleIndexOld);
				for(DirectDerivation directDerivation : oldMemberDirectDerivations){
					directDerivations.add(directDerivation.reinstantiate(source, mapping));
				}
				
				for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
					DirectDerivation directDerivation1 = iterator.next();
					for(Occurrence occurrence : existing){
						if(directDerivation1.left.equals(occurrence)){
							matched.add(occurrence);
							iterator.remove();
							break;
						}
					}
				}
				
				return new Pair<Set<Occurrence>, Set<DirectDerivation>>(matched, directDerivations);
			}
		}
		
		return null;
	}
	
	public boolean occurencesContained(LabeledGraphMapping mapping, Set<Occurrence> oldOccurrences, Set<Occurrence> newOccurrences){
		if(oldOccurrences.isEmpty() && newOccurrences.isEmpty()) return true;
		
		Set<Occurrence> newOccurrences2 = new TreeSet<Occurrence>();
		for(Occurrence occurrence : oldOccurrences)
			newOccurrences2.add(occurrence.reinstantiate(mapping));
	
		return !newOccurrences2.isEmpty() && !newOccurrences.isEmpty() && newOccurrences2.containsAll(newOccurrences);
	}
	
	public boolean isTestedIndependent(FunctionStructure source, RuleSequence ruleSequence, int ruleIndexCandidate, int ruleIndexMember, 
			Set<Occurrence> candidateOccurrences, Set<Occurrence> memberOccurrences){
		
		Set<Integer> used = new TreeSet<Integer>();
		for(Occurrence occurrence : candidateOccurrences)
			used.addAll(occurrence);
		for(Occurrence occurrence : memberOccurrences)
			used.addAll(occurrence);
		
		Set<DerivationGroup> derivationGroups = cachedDerivationGroups.get(ruleSequence.ruleAppearances.get(ruleIndexCandidate).id);
		if(derivationGroups == null) return false;
		for(DerivationGroup derivationGroup : derivationGroups){
			int ruleIndexCandidateOld = DerivationUtils.getCorrespondingRuleIndex(ruleSequence, ruleIndexCandidate, derivationGroup.currentCanonicalForm.ruleSequence);
			int ruleIndexMemberOld = DerivationUtils.getCorrespondingRuleIndex(ruleSequence, ruleIndexMember, derivationGroup.currentCanonicalForm.ruleSequence);
			LabeledGraphMapping mapping, inverseMapping;
			if(derivationGroup.independentIndices.contains(ruleIndexMemberOld)
					&& derivationGroup.independentIndices.contains(ruleIndexCandidateOld) // must be tested since we have multiple applications of a rule id
					&& (mapping = source.subgraphIsomorphMapping(derivationGroup.derivationGroupHost.functionStructure, used, true, true, null)) != null){
				
				inverseMapping = mapping.getInverse();
				
				if(occurencesContained(inverseMapping, derivationGroup.memberOccurrences.get(ruleIndexCandidateOld), candidateOccurrences)
						&& occurencesContained(inverseMapping, derivationGroup.memberOccurrences.get(ruleIndexMemberOld), memberOccurrences)) return true;
				
			}
		}
		return false;
	}
	
}
