/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.javatuples.Pair;

import de.eich.rewriter.functionstructure.Edge;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

public class CandidateManager {

	private final DerivationGroup derivationGroup;
	
	// the current candidate
	protected int candidateRuleIndex;
	protected final Set<DirectDerivation> uniqueOccurrenceCandidateDirectDerivations = new HashSet<DirectDerivation>();
	// all (also isomorphic) occurrences are stored here
	protected Set<Occurrence> candidateOccurrences;
	
	public DerivationGroup reversedDerivationGroup1 = null;
	public DerivationGroup reversedDerivationGroup2 = null;
	
	public CandidateManager(DerivationGroup derivationGroup){
		this.derivationGroup = derivationGroup;
	}
	
	public boolean prepareCandidate(int ruleIndex){
		// pessimistic independence hypothesis, if rule ids have been tested independent before regardless the host graph or occurrence it is not shiftable
		
		Map<Occurrence, DirectDerivation> candidateDirectDerivationsByOccurrence = new TreeMap<Occurrence, DirectDerivation>();

		candidateOccurrences = new TreeSet<Occurrence>();
		uniqueOccurrenceCandidateDirectDerivations.clear();
		
		if(derivationGroup.independentIndices.contains(ruleIndex)){
			Set<DirectDerivation> directDerivations = derivationGroup.memberDirectDerivations.get(ruleIndex);
			for(DirectDerivation directDerivation : directDerivations){
				candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
				candidateOccurrences.add(directDerivation.left);
			}
		} else {
			if(derivationGroup.children.isEmpty()){
				return false;
			}
			for(DerivationGroup childDerivationGroup : derivationGroup.children)
				if(!childDerivationGroup.independentIndices.contains(ruleIndex)){
					return false;
				}
			
			// gather all derivations from child groups
			for(DerivationGroup childDerivationGroup : derivationGroup.children){
				if(childDerivationGroup.candidateManager.candidateRuleIndex == ruleIndex){
					for(DirectDerivation directDerivation : childDerivationGroup.candidateManager.uniqueOccurrenceCandidateDirectDerivations){
						candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
						candidateOccurrences.add(directDerivation.left);
					}
				} else {
					Set<DirectDerivation> directDerivations = childDerivationGroup.memberDirectDerivations.get(ruleIndex);
					if(directDerivations != null){
						for(DirectDerivation directDerivation : directDerivations){
							candidateDirectDerivationsByOccurrence.put(directDerivation.left, directDerivation);
							candidateOccurrences.add(directDerivation.left);
						}
					}
				}
			}
		}
		
		candidateRuleIndex = ruleIndex;
		uniqueOccurrenceCandidateDirectDerivations.addAll(candidateDirectDerivationsByOccurrence.values());
		
		return true;
	}
	
	public Boolean simpleTestCandidate(int ruleIndexMember){
		reversedDerivationGroup1 = reversedDerivationGroup2 = null;
		
		// same rule
		int memberRuleID = derivationGroup.currentCanonicalForm.ruleSequence.get(ruleIndexMember).id;
		int candidateRuleID = derivationGroup.currentCanonicalForm.ruleSequence.get(candidateRuleIndex).id;
		if(memberRuleID == candidateRuleID){
			return true;
		}
		
		// already tested independent
		if(derivationGroup.independentIndices.contains(candidateRuleIndex)){
			return false;
		}
		
		// all children contain member and candidate
		boolean testedIndependentInChilds = true;
		for(DerivationGroup childDerivationGroup : derivationGroup.children){
			if(!childDerivationGroup.independentIndices.contains(ruleIndexMember)){
				testedIndependentInChilds = false;
				break;
			};
		}
		if(testedIndependentInChilds){
			return false;
		}

		// all candidate occurrences must be present in source - if there is only one member we can confirm this dependency right away
		boolean allPresentInSource = true;
		loop: for(Occurrence occurrence : candidateOccurrences){
			for(int nodeID : occurrence){
				if(derivationGroup.derivationGroupHost.functionStructure.getFunction(nodeID) == null){
					allPresentInSource = false;
					break loop;
				}
			}
		}
		if(!allPresentInSource && derivationGroup.independentIndices.size() == 1)
			return true;
		
		// already tested independent elsewhere - must stand before isDependentIDPair, otherwise we would waste already confirmed independent situations
		if(allPresentInSource){
			Set<Occurrence> memberOccurrences = derivationGroup.memberOccurrences.get(ruleIndexMember);
			if(derivationGroup.currentCanonicalForm.rewriter.derivationGroupCache.isTestedIndependent(derivationGroup.derivationGroupHost.functionStructure, derivationGroup.currentCanonicalForm.ruleSequence, candidateRuleIndex, ruleIndexMember,
					candidateOccurrences, memberOccurrences)){
				return false;
			}
		}
		
		// already tested dependent or same rule
		if(derivationGroup.currentCanonicalForm.rewriter.derivationGroupCache.isDependentIDPair(memberRuleID, candidateRuleID) || memberRuleID == candidateRuleID){
			return true;
		}
		
		return null;
	}
	
	public boolean overlap(DirectDerivation candidateDirectDerivation, DirectDerivation memberDirectDerivation){
			
		Occurrence candidateOccurrence = candidateDirectDerivation.left;
		Occurrence memberOccurrence = memberDirectDerivation.left;
		Occurrence memberCoccurrence = memberDirectDerivation.right;
			
		FunctionStructure source = candidateDirectDerivation.source;
		Set<Integer> extendedCandidate = new TreeSet<Integer>(candidateOccurrence);
		for(int nodeID : candidateOccurrence){
			Node node = source.getFunction(nodeID);
			for(Edge edge : node.incomingEdges)
				extendedCandidate.add(edge.sourceNode.id);
			for(Edge edge : node.outgoingEdges)
				extendedCandidate.add(edge.targetNode.id);
		}
		
		for(int nodeID : extendedCandidate)
			if(memberOccurrence.contains(nodeID)) return true;
		
		for(int nodeID : extendedCandidate)
			if(memberCoccurrence.contains(nodeID)) return true;
		
		source = memberDirectDerivation.source;
		Set<Integer> extendedMember = new TreeSet<Integer>();
		for(int nodeID : memberOccurrence){
			Node node = source.getFunction(nodeID);
			for(Edge edge : node.incomingEdges)
				extendedMember.add(edge.sourceNode.id);
			for(Edge edge : node.outgoingEdges)
				extendedMember.add(edge.targetNode.id);
		}
		
		for(int nodeID : extendedCandidate)
			if(extendedMember.contains(nodeID)) return true;
		
		return false;
	}
	
	public boolean testCandidate(int ruleIndexMember, List<Integer> otherMembersRuleIndices, List<Integer> testedIndependent){
		reversedDerivationGroup1 = reversedDerivationGroup2 = null;
		
		Set<DirectDerivation> memberDirectDerivations = derivationGroup.memberDirectDerivations.get(ruleIndexMember);
		Set<Occurrence> memberOccurrences = derivationGroup.memberOccurrences.get(ruleIndexMember);
		
		// generate new join but without the chosen member
		Pair<List<FunctionStructure>, List<List<DirectDerivation>>> joinsAndDerivationSequences = derivationGroup.joinCache.getJoinsAndDerivationSequences(otherMembersRuleIndices);
		List<FunctionStructure> joins = joinsAndDerivationSequences.getValue0();
		
		RuleSequence ruleSequence = derivationGroup.currentCanonicalForm.ruleSequence;
		
		Set<Occurrence> uncriticalCandidateOccurrences = new TreeSet<Occurrence>(candidateOccurrences);
		Set<Occurrence> uncriticalMemberOccurrences = new TreeSet<Occurrence>(memberOccurrences);
		for(DirectDerivation candidateDirectDerivation : uniqueOccurrenceCandidateDirectDerivations){
			for(DirectDerivation memberDirectDerivation : memberDirectDerivations){
				if(overlap(candidateDirectDerivation, memberDirectDerivation)){ // prerequisite for parallel and sequential dependence
					uncriticalCandidateOccurrences.remove(candidateDirectDerivation.left);
					uncriticalMemberOccurrences.remove(memberDirectDerivation.left);
				}
			}
		}
		
		// try to apply both direct derivation sets in reverse order
		for(FunctionStructure source : joins) {
			
			Pair<Set<Occurrence>, Set<DirectDerivation>> additional = derivationGroup.currentCanonicalForm.rewriter.derivationGroupCache.getCachedDirectDerivations(source, ruleSequence, candidateRuleIndex, candidateOccurrences);
			
			if(additional == null)
				additional = ruleSequence.get(candidateRuleIndex).deriveAdditionals(source, candidateOccurrences, uncriticalCandidateOccurrences);
			Set<Occurrence> candidateOccurrencesMatched = additional.getValue0();
			Set<DirectDerivation> candidateDirectDerivationsAdditional = additional.getValue1();
			if(!candidateDirectDerivationsAdditional.isEmpty() || !candidateOccurrencesMatched.equals(candidateOccurrences)){
				
				for(Iterator<DirectDerivation> iterator = uniqueOccurrenceCandidateDirectDerivations.iterator(); iterator.hasNext();){
					DirectDerivation candidateDirectDerivation = iterator.next();
					if(!candidateOccurrencesMatched.contains(candidateDirectDerivation.left)){
						candidateOccurrences.remove(candidateDirectDerivation.left);
						iterator.remove();
					}
				}
				
				derivationGroup.addUnique(candidateOccurrences, uniqueOccurrenceCandidateDirectDerivations, candidateDirectDerivationsAdditional);
				reversedDerivationGroup1 = new DerivationGroup(new DerivationGroupHost(source, derivationGroup.currentCanonicalForm.ruleSequence, ruleIndexMember), derivationGroup.currentCanonicalForm);
				setFailedCopy(candidateRuleIndex, candidateOccurrences, uniqueOccurrenceCandidateDirectDerivations, ruleIndexMember, memberOccurrences, testedIndependent);
				
				return true;
			} else {
				reversedDerivationGroup1 = new DerivationGroup(new DerivationGroupHost(source, derivationGroup.currentCanonicalForm.ruleSequence, ruleIndexMember), derivationGroup.currentCanonicalForm);
				setFailedCopy(candidateRuleIndex, candidateOccurrences, uniqueOccurrenceCandidateDirectDerivations, ruleIndexMember, memberOccurrences, testedIndependent);
				reversedDerivationGroup2 = reversedDerivationGroup1;
				reversedDerivationGroup1 = null;
			}
			
			for(DirectDerivation candidateDirectDerivation : uniqueOccurrenceCandidateDirectDerivations){
				
				DirectDerivation candidateDirectDerivationReversed = candidateDirectDerivation.rule.apply(source, candidateDirectDerivation);
				// no additional direct derivations may be produced for member
				additional = derivationGroup.currentCanonicalForm.rewriter.derivationGroupCache.getCachedDirectDerivations(candidateDirectDerivationReversed.target, ruleSequence, ruleIndexMember, memberOccurrences);
				if(additional == null)
					additional = ruleSequence.get(ruleIndexMember).deriveAdditionals(candidateDirectDerivationReversed.target, memberOccurrences, uncriticalMemberOccurrences);
				Set<Occurrence> memberOccurrencesMatched = additional.getValue0();
				Set<DirectDerivation> memberDirectDerivationsAdditional = additional.getValue1();
				if(!memberDirectDerivationsAdditional.isEmpty() || !memberOccurrencesMatched.equals(memberOccurrences)){
					
					Set<Occurrence> memberOccurrencesFailedCopy = new TreeSet<Occurrence>(memberOccurrences);
					Set<DirectDerivation> memberDirectDerivationsFailedCopy = new HashSet<DirectDerivation>(memberDirectDerivations);
					
					for(Iterator<DirectDerivation> iterator = memberDirectDerivationsFailedCopy.iterator(); iterator.hasNext();){
						DirectDerivation memberDirectDerivation = iterator.next();
						if(!memberOccurrencesMatched.contains(memberDirectDerivation.left)){
							memberOccurrencesFailedCopy.remove(memberDirectDerivation.left);
							iterator.remove();
						}
					}
					
					derivationGroup.addUnique(memberOccurrencesFailedCopy, memberDirectDerivationsFailedCopy, memberDirectDerivationsAdditional);
					reversedDerivationGroup1 = new DerivationGroup(new DerivationGroupHost(candidateDirectDerivationReversed.target, derivationGroup.currentCanonicalForm.ruleSequence, ruleIndexMember, candidateRuleIndex), derivationGroup.currentCanonicalForm);
					setFailedCopy(ruleIndexMember, memberOccurrencesFailedCopy, memberDirectDerivationsFailedCopy, candidateRuleIndex, candidateOccurrences, testedIndependent);
					
					return true;
				}
				
			}
			
		}
		return false;
	}

	private void setFailedCopy(int ruleIndex, Set<Occurrence> occurrences, Set<DirectDerivation> directDerivations,
			int dependentRuleIndex, Set<Occurrence> dependentOccurrences,
			List<Integer> testedIndependent){
		
		reversedDerivationGroup1.candidateManager.candidateRuleIndex = ruleIndex;
		reversedDerivationGroup1.candidateManager.uniqueOccurrenceCandidateDirectDerivations.addAll(directDerivations);
		reversedDerivationGroup1.candidateManager.candidateOccurrences = new TreeSet<Occurrence>(occurrences);
		
		reversedDerivationGroup1.candidateManager.confirmCandidate();
		
		for(int testedRuleIndex : testedIndependent){
			reversedDerivationGroup1.candidateManager.candidateRuleIndex = testedRuleIndex;
			reversedDerivationGroup1.candidateManager.uniqueOccurrenceCandidateDirectDerivations.addAll(derivationGroup.memberDirectDerivations.get(testedRuleIndex));
			reversedDerivationGroup1.candidateManager.candidateOccurrences = new TreeSet<Occurrence>(derivationGroup.memberOccurrences.get(testedRuleIndex));
			
			reversedDerivationGroup1.candidateManager.confirmCandidate();
		}
	}
	
	public void confirmCandidate(){
		if(!derivationGroup.independentIndices.contains(candidateRuleIndex)){
			derivationGroup.independentIndices.add(candidateRuleIndex);
			derivationGroup.unchanged = false;
			// all members are "normalized" on source for generating joins
			Set<DirectDerivation> directDerivations = derivationGroup.currentCanonicalForm.ruleSequence.get(candidateRuleIndex).apply(derivationGroup.derivationGroupHost.functionStructure, uniqueOccurrenceCandidateDirectDerivations);
			derivationGroup.memberDirectDerivations.put(candidateRuleIndex, directDerivations);
			derivationGroup.memberOccurrences.put(candidateRuleIndex, new TreeSet<Occurrence>(candidateOccurrences));
		}
	}
	
	// remove lower indices from groups since they will not be tested resp. test has just failed
	public void removeFailedAndUntestedMembersOfChildren(int failedRuleIndex){
		for(DerivationGroup group : derivationGroup.children){
			for(Iterator<Integer> iterator = group.independentIndices.iterator(); iterator.hasNext();){
				int ruleIndex = iterator.next();
				if(ruleIndex <= failedRuleIndex){ // each group set index should refer to the same rule index 
					iterator.remove();
					group.memberDirectDerivations.remove(ruleIndex);
					group.memberOccurrences.remove(ruleIndex);
				}
			}
		}
	}
	
}
