/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.utils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MathUtils {

	public static Set<List<Integer>> getPermutations(List<Integer> integers, boolean withRepetitions){
		Set<List<Integer>> permutations = new TreeSet<List<Integer>>();
		getPermutationsRecursive(new ComparableList<Integer>(), integers, permutations, withRepetitions);
		return permutations;
	}
	
	private static void getPermutationsRecursive(List<Integer> trace, List<Integer> integers, Set<List<Integer>> permutations, boolean withRepetitions){
		if(trace.size() == integers.size()){
			List<Integer> permutation = new ComparableList<Integer>();
			for(int j : trace)
				permutation.add(integers.get(j));
			permutations.add(permutation);
			
			return;
		}
		
		for(int i=0; i<integers.size(); i++){
			if(!withRepetitions && trace.contains(i)) continue;

			List<Integer> extendedTrace = new ComparableList<Integer>(trace);
			extendedTrace.add(i);
			
			getPermutationsRecursive(extendedTrace, integers, permutations, withRepetitions);
		}
	}
	
	public static Set<List<Integer>> getVariations(List<Integer> integers, boolean withRepetitions){
		Set<List<Integer>> variations = new TreeSet<List<Integer>>();
		getVariationsRecursive(new ComparableList<Integer>(), integers, variations, withRepetitions);
		return variations;
	}
	
	private static void getVariationsRecursive(List<Integer> trace, List<Integer> integers, Set<List<Integer>> variations, boolean withRepetitions){
		if(trace.size() == integers.size()) return;
		
		for(int i=0; i<integers.size(); i++){
			if(!withRepetitions && trace.contains(i)) continue;

			List<Integer> extendedTrace = new ComparableList<Integer>(trace);
			extendedTrace.add(i);
			
			List<Integer> variation = new ComparableList<Integer>();
			for(int j : extendedTrace)
				variation.add(integers.get(j));
			variations.add(variation);
			
			getVariationsRecursive(extendedTrace, integers, variations, withRepetitions);
		}
	}
	
}
