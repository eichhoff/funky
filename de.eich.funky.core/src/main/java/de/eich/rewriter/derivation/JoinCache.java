/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.utils.ComparableList;

public class JoinCache {

	private final DerivationGroup derivationGroup;
	
	// all computed joins are cached here, map from rule indices to a triplet consisting of the resulting join, the nodes deleted during derivation, and the applied direct derivations
	private final Map<List<Integer>, Set<Triplet<FunctionStructure, Set<Integer>, List<DirectDerivation>>>> joinsByRuleIndices = new TreeMap<List<Integer>, Set<Triplet<FunctionStructure, Set<Integer>, List<DirectDerivation>>>>();

	public JoinCache(DerivationGroup derivationGroup){
		this.derivationGroup = derivationGroup;
	}
	
	public Pair<List<FunctionStructure>, List<List<DirectDerivation>>> getJoinsAndDerivationSequences(List<Integer> ruleIndices) {
		List<Integer> ruleIndicesCropped = new ComparableList<Integer>();
		for(Integer ruleIndex : ruleIndices){
			if(!derivationGroup.memberDirectDerivations.get(ruleIndex).isEmpty())
				ruleIndicesCropped.add(ruleIndex);
		}
		
		Pair<List<FunctionStructure>, List<List<DirectDerivation>>> joinsAndDerivationSequences = getJoinsAndDerivationSequences2(ruleIndicesCropped);
		
		List<FunctionStructure> joins = joinsAndDerivationSequences.getValue0();
		List<List<DirectDerivation>> derivationSequences = joinsAndDerivationSequences.getValue1();

		// remove isomorphisms within group
		for(int i = 0; i<joins.size(); i++){
			FunctionStructure source1 = joins.get(i);
			for(int j = i + 1; j<joins.size(); j++){
				FunctionStructure source2 = joins.get(j);
				if(source1.isomorph(source2)){
					derivationSequences.remove(j);
					joins.remove(j--);
				}
			}
		}

		return new Pair<List<FunctionStructure>, List<List<DirectDerivation>>>(joins, derivationSequences);
	}
	
	private Pair<List<FunctionStructure>, List<List<DirectDerivation>>> getJoinsAndDerivationSequences2(List<Integer> ruleIndices) {
		Set<Triplet<FunctionStructure, Set<Integer>, List<DirectDerivation>>> cacheEntries;
		
		if (ruleIndices.size() == 0){
			// if no rule index is given no joins are needed, return source instead
			List<FunctionStructure> joins = new ArrayList<FunctionStructure>();
			joins.add(derivationGroup.derivationGroupHost.functionStructure);
			List<List<DirectDerivation>> derivationSequences = new ArrayList<List<DirectDerivation>>();
			return new Pair<List<FunctionStructure>, List<List<DirectDerivation>>>(joins, derivationSequences);
		} else if((cacheEntries = joinsByRuleIndices.get(ruleIndices)) != null) {
			// read from cache if possible
			List<FunctionStructure> joins = new ArrayList<FunctionStructure>();
			List<List<DirectDerivation>> derivationSequences = new ArrayList<List<DirectDerivation>>();
			for(Triplet<FunctionStructure, Set<Integer>, List<DirectDerivation>> cacheEntry : cacheEntries){
				joins.add(cacheEntry.getValue0());
				derivationSequences.add(cacheEntry.getValue2());
			}
			return new Pair<List<FunctionStructure>, List<List<DirectDerivation>>>(joins, derivationSequences);
		} else {
			List<FunctionStructure> orderedJoins = new ArrayList<FunctionStructure>(); 
			List<Set<Integer>> orderedDeleteNodeIDs = new ArrayList<Set<Integer>>();
			List<List<DirectDerivation>> orderedDerivationSequences = new ArrayList<List<DirectDerivation>>();
			int lastRuleIndex = ruleIndices.remove(ruleIndices.size() - 1);
			if((cacheEntries = joinsByRuleIndices.get(ruleIndices)) != null) {
				// extend a cache entry if possible
				Set<DirectDerivation> directDerivations = derivationGroup.memberDirectDerivations.get(lastRuleIndex);
				for(DirectDerivation directDerivation : directDerivations) {
					for(Triplet<FunctionStructure, Set<Integer>, List<DirectDerivation>> cacheEntry : cacheEntries){
						FunctionStructure source = cacheEntry.getValue0();
						List<DirectDerivation> derivationSequence = new ArrayList<DirectDerivation>(cacheEntry.getValue2());
						
						derivationSequence.add(directDerivation);
						orderedDerivationSequences.add(derivationSequence);

						Set<Integer> deleteNodeIDs = new TreeSet<Integer>(cacheEntry.getValue1());
						deleteNodeIDs.addAll(directDerivation.getRemoved());
						orderedDeleteNodeIDs.add(deleteNodeIDs);
						
						orderedJoins.add(source.copy(false).join(directDerivation.target, deleteNodeIDs));
					}
				}
				ruleIndices.add(lastRuleIndex);
			} else {
				ruleIndices.add(lastRuleIndex);
				// in any other case produce joins and cache the results
				
				// must be ordered for referencing
				int previousSize = 0;
				for(int i = 0; i < ruleIndices.size(); i++) {
					Set<DirectDerivation> directDerivations = derivationGroup.memberDirectDerivations.get(ruleIndices.get(i));
					if(directDerivations.isEmpty()){
						System.err.println("error");
					}
					int j = 0;
					for(DirectDerivation directDerivation : directDerivations) {
						if(i == 0) {
							// generate prototype
							List<DirectDerivation> derivationSequence = new ArrayList<DirectDerivation>();
							derivationSequence.add(directDerivation);
							orderedDerivationSequences.add(derivationSequence);

							orderedDeleteNodeIDs.add(new TreeSet<Integer>(directDerivation.getRemoved()));
							
							orderedJoins.add(directDerivation.target.copy(false));
						} else {
							if(++j == directDerivations.size()) { 
								// the last item is used to adapt the prototype itself
								for(int k = 0; k < previousSize; k++) {
									FunctionStructure source = orderedJoins.get(k);
									List<DirectDerivation> derivationSequence = orderedDerivationSequences.get(k);
									
									derivationSequence.add(directDerivation);

									Set<Integer> deleteNodeIDs = orderedDeleteNodeIDs.get(k);
									deleteNodeIDs.addAll(directDerivation.getRemoved());
									
									source.join(directDerivation.target, deleteNodeIDs);
								}
							} else { 
								// for every other item copies of the prototype are produced
								for(int k = 0; k < previousSize; k++) {
									FunctionStructure source = orderedJoins.get(k);
									List<DirectDerivation> derivationSequence = new ArrayList<DirectDerivation>(orderedDerivationSequences.get(k));
									
									derivationSequence.add(directDerivation);
									orderedDerivationSequences.add(derivationSequence);
									
									Set<Integer> deleteNodeIDs = new TreeSet<Integer>(orderedDeleteNodeIDs.get(k));
									deleteNodeIDs.addAll(directDerivation.getRemoved());
									orderedDeleteNodeIDs.add(deleteNodeIDs);
									
									orderedJoins.add(source.copy(false).join(directDerivation.target, deleteNodeIDs));
								}
							}
						}
					}
					previousSize = orderedJoins.size();
				}
			}
			
			cacheEntries = new HashSet<Triplet<FunctionStructure, Set<Integer>, List<DirectDerivation>>>();
			for(int i = 0; i < orderedJoins.size(); i++){
				cacheEntries.add(new Triplet<FunctionStructure, Set<Integer>, List<DirectDerivation>>(orderedJoins.get(i), orderedDeleteNodeIDs.get(i), orderedDerivationSequences.get(i)));
			}
			joinsByRuleIndices.put(new ComparableList<Integer>(ruleIndices), cacheEntries);
			
			return new Pair<List<FunctionStructure>, List<List<DirectDerivation>>>(orderedJoins, orderedDerivationSequences);
		}
	}
	
}
