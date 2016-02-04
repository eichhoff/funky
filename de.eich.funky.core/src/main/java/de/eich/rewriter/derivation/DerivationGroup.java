/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import org.javatuples.Pair;

import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.LabeledGraphMapping;
import de.eich.utils.ComparableList;

public class DerivationGroup {
	
	public Derivation currentCanonicalForm;

	public final List<Integer> independentIndices = new ComparableList<Integer>();
	
	public DerivationGroupHost derivationGroupHost;
	
	protected final DerivationGroup parent;
	protected final Set<DerivationGroup> children = new HashSet<DerivationGroup>();
	
	// candidates will be added here if they are confirmed
	// only non-isomorphic direct derivations are stored here
	public final Map<Integer, Set<DirectDerivation>> memberDirectDerivations = new TreeMap<Integer, Set<DirectDerivation>>();
	// all (also isomorphic) occurrences are stored here
	public final Map<Integer, Set<Occurrence>> memberOccurrences = new TreeMap<Integer, Set<Occurrence>>();
	
	protected final JoinCache joinCache = new JoinCache(this);
	
	public final CandidateManager candidateManager = new CandidateManager(this);
	
	// only non-isomorphic function structures are stored here, first is source, second is target
	private final Set<Pair<FunctionStructure,FunctionStructure>> resultingFunctionStructures = new HashSet<Pair<FunctionStructure,FunctionStructure>>();
	
	public DerivationGroup originalGroup = null;
	public LabeledGraphMapping originalGroupSourceMapping = null;
	public boolean unchanged = true;

	public DerivationGroup(DerivationGroupHost derivationGroupHost, Derivation canonicalForm){
		this.derivationGroupHost = derivationGroupHost;
		this.currentCanonicalForm = canonicalForm;
		this.parent = null;
	}
	
	public DerivationGroup(DerivationGroupHost derivationGroupHost, DerivationGroup parent){
		this.derivationGroupHost = derivationGroupHost;
		this.currentCanonicalForm = parent.currentCanonicalForm;
		this.parent = parent;
	}
	
	public Set<Pair<FunctionStructure,FunctionStructure>> addDirectDerivationsFromSource(){
		// get direct derivations for first rule index
		Set<DirectDerivation> newDirectDerivations = currentCanonicalForm.ruleSequence.get(0).derive(derivationGroupHost.functionStructure, null);
		
		candidateManager.candidateOccurrences = new TreeSet<Occurrence>();
		for(DirectDerivation directDerivation : newDirectDerivations){
			if(!currentCanonicalForm.ruleSequence.isIntraRuleCommonConfiguration(0, directDerivation)){
				resultingFunctionStructures.add(new Pair<FunctionStructure,FunctionStructure>(directDerivation.source, directDerivation.target));
				candidateManager.uniqueOccurrenceCandidateDirectDerivations.add(directDerivation);
			}
			candidateManager.candidateOccurrences.add(directDerivation.left);
		}
		
		independentIndices.add(0);
		memberDirectDerivations.put(0, new HashSet<DirectDerivation>(candidateManager.uniqueOccurrenceCandidateDirectDerivations));
		memberOccurrences.put(0, new TreeSet<Occurrence>(candidateManager.candidateOccurrences));
		
		return resultingFunctionStructures;
	}
	
	public Set<Pair<FunctionStructure,FunctionStructure>> addDirectDerivationsFromSource2(){
		// get direct derivations for first rule index
		Set<DirectDerivation> newDirectDerivations;
		
		candidateManager.candidateOccurrences = new TreeSet<Occurrence>();
		if(!memberDirectDerivations.isEmpty() && (newDirectDerivations = memberDirectDerivations.get(0)) != null){ // not applicable rule index reached
			for(DirectDerivation directDerivation : newDirectDerivations){
				if(!currentCanonicalForm.ruleSequence.isIntraRuleCommonConfiguration(0, directDerivation)){
					resultingFunctionStructures.add(new Pair<FunctionStructure,FunctionStructure>(directDerivation.source, directDerivation.target));
					candidateManager.uniqueOccurrenceCandidateDirectDerivations.add(directDerivation);
				}
				candidateManager.candidateOccurrences.add(directDerivation.left);
			}
		}
		
		return resultingFunctionStructures;
	}
	
	protected boolean hasDirectDerivationsForIndices(List<Integer> ruleIndices){
		if(memberDirectDerivations.isEmpty()) return false;
		for(int ruleIndex : ruleIndices){
			if(!hasDirectDerivationsForIndex(ruleIndex)){
				return false;
			}
		}
		return true;
	}
	
	protected boolean hasDirectDerivationsForIndex(int ruleIndex){
		Set<DirectDerivation> directDerivations;
		if((directDerivations = memberDirectDerivations.get(ruleIndex)) == null || directDerivations.isEmpty()){
			return false;
		}
		return true;
	}
	
	public Map<FunctionStructure, Pair<DerivationGroup, List<DirectDerivation>>> reinstantiateNextGroups(int nextRuleIndex, List<Integer> ruleIndicesJoin){
		Map<FunctionStructure, Pair<DerivationGroup, List<DirectDerivation>>> nextGroups = new HashMap<FunctionStructure, Pair<DerivationGroup, List<DirectDerivation>>>();
		Pair<List<FunctionStructure>, List<List<DirectDerivation>>> joinsAndDerivationSequences = joinCache.getJoinsAndDerivationSequences(ruleIndicesJoin);
		List<FunctionStructure> joins = joinsAndDerivationSequences.getValue0();
		List<List<DirectDerivation>> derivationSequences = joinsAndDerivationSequences.getValue1();
		for(int i = 0; i < joins.size(); i++){
			FunctionStructure source = joins.get(i);
			List<DirectDerivation> derivationSequence = derivationSequences.get(i);
			DerivationGroup newGroup = currentCanonicalForm.rewriter.derivationGroupCache.getDerivationGroup(source, this.currentCanonicalForm.ruleSequence, this, nextRuleIndex, this.currentCanonicalForm);
			nextGroups.put(source, new Pair<DerivationGroup, List<DirectDerivation>>(newGroup, derivationSequence));
		}
		return nextGroups;
	}
	
	public Set<Pair<FunctionStructure,FunctionStructure>> makeChildren2(int currentRuleIndex, int nextRuleIndex, Map<FunctionStructure, Pair<DerivationGroup, List<DirectDerivation>>> reinstantiatedNextGroups){
		// produce joins by combining all group members
		// note: if the candidate is independent from all group members each group join will produce the same direct derivations
		Map<Occurrence, DirectDerivation> candidateDirectDerivationsByOccurrence = new TreeMap<Occurrence, DirectDerivation>();
		resultingFunctionStructures.clear();
		candidateManager.uniqueOccurrenceCandidateDirectDerivations.clear();
		
		for(Entry<FunctionStructure, Pair<DerivationGroup, List<DirectDerivation>>> entry : reinstantiatedNextGroups.entrySet()){
			
			FunctionStructure source = entry.getKey();
			DerivationGroup newGroup;
			
			Set<DirectDerivation> newDirectDerivations;
			
			newGroup = new DerivationGroup(new DerivationGroupHost(source, currentCanonicalForm.ruleSequence, nextRuleIndex), this);
			
			for(int ruleIndex : independentIndices){
				if(ruleIndex != currentRuleIndex){
					newGroup.independentIndices.add(ruleIndex);
					newGroup.memberDirectDerivations.put(ruleIndex, currentCanonicalForm.ruleSequence.get(ruleIndex).apply(newGroup.derivationGroupHost.functionStructure, memberDirectDerivations.get(ruleIndex)));
					newGroup.memberOccurrences.put(ruleIndex, memberOccurrences.get(ruleIndex));
				}
			}
			
			if(!newGroup.memberDirectDerivations.isEmpty() && (newDirectDerivations = newGroup.memberDirectDerivations.get(nextRuleIndex)) != null){ // not applicable rule index reached
				for(DirectDerivation directDerivation : newDirectDerivations){
					if(!currentCanonicalForm.ruleSequence.isIntraRuleCommonConfiguration(nextRuleIndex, directDerivation)){
						candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
						resultingFunctionStructures.add(new Pair<FunctionStructure,FunctionStructure>(directDerivation.source, directDerivation.target));
					}
				}
			}
			
			children.add(newGroup);
		}
		
		candidateManager.uniqueOccurrenceCandidateDirectDerivations.addAll(candidateDirectDerivationsByOccurrence.values());
		
		return resultingFunctionStructures;
	}
	/*
	private class MakeChildrenThread extends Thread {
		
		DerivationGroup parent;
		Map<Occurrence, DirectDerivation> candidateDirectDerivationsByOccurrence;
		int nextRuleIndex; 
		Entry<FunctionStructure, DerivationGroup> entry;
		CountDownLatch latch;
		
		public void run(){
			FunctionStructure source = entry.getKey();
			DerivationGroup newGroup = entry.getValue();
			
			Set<DirectDerivation> newDirectDerivations;
			
			if(newGroup != null){
				
				if(!newGroup.memberDirectDerivations.isEmpty() && (newDirectDerivations = newGroup.memberDirectDerivations.get(nextRuleIndex)) != null){ // not applicable rule index reached
					for(DirectDerivation directDerivation : newDirectDerivations){
						if(!currentCanonicalForm.ruleSequence.isIntraRuleCommonConfiguration(nextRuleIndex, directDerivation)){
							candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
							resultingFunctionStructures.add(directDerivation.target);
						}
					}
				}
				
				children.add(newGroup);
				
			} else {
				
				newGroup = new DerivationGroup(new DerivationGroupHost(source, currentCanonicalForm.ruleSequence, nextRuleIndex), parent);
				
				Set<Occurrence> occurrences = new TreeSet<Occurrence>();
				newDirectDerivations = new HashSet<DirectDerivation>();
				// get direct derivations for candidate
				Set<DirectDerivation> directDerivations = currentCanonicalForm.ruleSequence.get(nextRuleIndex).derive(source);
				for(DirectDerivation directDerivation : directDerivations){
					if(!currentCanonicalForm.ruleSequence.isIntraRuleCommonConfiguration(nextRuleIndex, directDerivation)){
						candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
						resultingFunctionStructures.add(directDerivation.target);
						newDirectDerivations.add(directDerivation);
					}
					occurrences.add(directDerivation.left);
				}
				
				newGroup.independentIndices.add(nextRuleIndex);
				newGroup.memberDirectDerivations.put(nextRuleIndex, newDirectDerivations);
				newGroup.memberOccurrences.put(nextRuleIndex, occurrences);
				
				children.add(newGroup);
				
			}
			
			latch.countDown();
		}
	}*/
	
	public Set<Pair<FunctionStructure,FunctionStructure>> makeChildren(int nextRuleIndex, Map<FunctionStructure, Pair<DerivationGroup, List<DirectDerivation>>> reinstantiatedNextGroups){
		
		// produce joins by combining all group members
		// note: if the candidate is independent from all group members each group join will produce the same direct derivations
		Map<Occurrence, DirectDerivation> candidateDirectDerivationsByOccurrence = new TreeMap<Occurrence, DirectDerivation>();
		resultingFunctionStructures.clear();
		candidateManager.uniqueOccurrenceCandidateDirectDerivations.clear();
		
		/*
		
		CountDownLatch latch = new CountDownLatch(reinstantiatedNextGroups.size());
		
		for(Entry<FunctionStructure, DerivationGroup> entry : reinstantiatedNextGroups.entrySet()){
			int i = 0;
			MakeChildrenThread thread = new MakeChildrenThread();
			thread.latch = latch;
			thread.candidateDirectDerivationsByOccurrence = candidateDirectDerivationsByOccurrence;
			thread.parent = this;
			thread.nextRuleIndex = nextRuleIndex;
			
			thread.entry = entry;
			thread.start();
		}
		
		 try {
	            latch.await();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		
		*/
		 
		for(Entry<FunctionStructure, Pair<DerivationGroup, List<DirectDerivation>>> entry : reinstantiatedNextGroups.entrySet()){
			FunctionStructure source = entry.getKey();
			DerivationGroup newGroup = entry.getValue().getValue0();
			
			Set<DirectDerivation> newDirectDerivations;
			
			if(newGroup != null){
				
				if(!newGroup.memberDirectDerivations.isEmpty() && (newDirectDerivations = newGroup.memberDirectDerivations.get(nextRuleIndex)) != null){ // not applicable rule index reached
					for(DirectDerivation directDerivation : newDirectDerivations){
						if(!currentCanonicalForm.ruleSequence.isIntraRuleCommonConfiguration(nextRuleIndex, directDerivation)){
							candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
							resultingFunctionStructures.add(new Pair<FunctionStructure,FunctionStructure>(directDerivation.source, directDerivation.target));
						}
					}
				}
				
				children.add(newGroup);
				
			} else {
				
				DirectDerivation precedingDirectDerivationOfSameRule = null;
				List<DirectDerivation> derivationSequence = entry.getValue().getValue1();
				for(int i = derivationSequence.size() -1; i >= 0; i--){
					DirectDerivation directDerivation = derivationSequence.get(i);
					if(directDerivation.rule.id == currentCanonicalForm.ruleSequence.get(nextRuleIndex).id){
						precedingDirectDerivationOfSameRule = derivationSequence.get(i);
						break;
					}
				}
				
				newGroup = new DerivationGroup(new DerivationGroupHost(source, currentCanonicalForm.ruleSequence, nextRuleIndex), this);
				
				Set<Occurrence> occurrences = new TreeSet<Occurrence>();
				newDirectDerivations = new HashSet<DirectDerivation>();
				// get direct derivations for candidate
				Set<DirectDerivation> directDerivations = currentCanonicalForm.ruleSequence.get(nextRuleIndex).derive(source, precedingDirectDerivationOfSameRule);
				for(DirectDerivation directDerivation : directDerivations){
					if(!currentCanonicalForm.ruleSequence.isIntraRuleCommonConfiguration(nextRuleIndex, directDerivation)){
						candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
						resultingFunctionStructures.add(new Pair<FunctionStructure,FunctionStructure>(directDerivation.source, directDerivation.target));
						newDirectDerivations.add(directDerivation);
					}
					occurrences.add(directDerivation.left);
				}
				
				newGroup.independentIndices.add(nextRuleIndex);
				newGroup.memberDirectDerivations.put(nextRuleIndex, newDirectDerivations);
				newGroup.memberOccurrences.put(nextRuleIndex, occurrences);
				
				children.add(newGroup);
				
			}
		}
		
		candidateManager.uniqueOccurrenceCandidateDirectDerivations.addAll(candidateDirectDerivationsByOccurrence.values());
		
		return resultingFunctionStructures;
	}
	
	void addUnique(Set<Occurrence> occurrences, Set<DirectDerivation> directDerivations, Set<DirectDerivation> additionalDirectDerivations){
		Map<Occurrence, DirectDerivation> directDerivationsByOccurrence = new TreeMap<Occurrence, DirectDerivation>();
		for(DirectDerivation directDerivation : additionalDirectDerivations)
			directDerivationsByOccurrence.put(directDerivation.left, directDerivation);
		occurrences.addAll(directDerivationsByOccurrence.keySet());
		directDerivations.addAll(directDerivationsByOccurrence.values());
	}
	
	public void getAllMembersFromOriginal(Map<Integer, Integer> ruleIDMap){
		for(Entry<Integer, Integer> entry : ruleIDMap.entrySet()){
			getMemberFromOriginal(entry.getKey(), entry.getValue());
		}
	}
	
	public void getMemberFromOriginal(int oldRuleIndex, int newRuleIndex){
		independentIndices.add(newRuleIndex);

		if(parent == null){
			memberDirectDerivations.put(newRuleIndex, new HashSet<DirectDerivation>(originalGroup.memberDirectDerivations.get(oldRuleIndex)));
			memberOccurrences.put(newRuleIndex, new HashSet<Occurrence>(originalGroup.memberOccurrences.get(oldRuleIndex)));
		} else {
			Set<DirectDerivation> oldMemberDirectDerivations = originalGroup.memberDirectDerivations.get(oldRuleIndex);
			Set<DirectDerivation> newMemberDirectDerivations = new HashSet<DirectDerivation>();
			for(DirectDerivation directDerivation : oldMemberDirectDerivations){
				newMemberDirectDerivations.add(directDerivation.reinstantiate(derivationGroupHost.functionStructure, originalGroupSourceMapping));
			}
			memberDirectDerivations.put(newRuleIndex, newMemberDirectDerivations);
			
			Set<Occurrence> oldMemberDirectOccurrences = originalGroup.memberOccurrences.get(oldRuleIndex);
			Set<Occurrence> newMemberDirectOccurrences = new HashSet<Occurrence>();
			for(Occurrence occurrence : oldMemberDirectOccurrences)
				newMemberDirectOccurrences.add(occurrence.reinstantiate(originalGroupSourceMapping));
			memberOccurrences.put(newRuleIndex, newMemberDirectOccurrences);
		}
	}
	
	public String toString(){
		return "Indices: " + independentIndices.toString();
	}

}
