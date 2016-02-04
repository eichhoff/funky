/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import de.eich.rewriter.functionstructure.FunctionStructure;

public class DerivationResult {

	public Derivation canonicalForm;
	public List<Integer> ruleIDs;
	// 3rd arg is the final function structure, 2nd arg is the source function structure of the last direct derivation	
	public Set<Triplet<List<Integer>, FunctionStructure, FunctionStructure>> functionStructures = new HashSet<Triplet<List<Integer>, FunctionStructure, FunctionStructure>>();
	public Set<Triplet<List<Integer>, FunctionStructure, FunctionStructure>> invalidFunctionStructures = new HashSet<Triplet<List<Integer>, FunctionStructure, FunctionStructure>>();
	public int notApplicableAtAllRuleIndex = -1;
	public double percentOfRuleSequenceAchieved = 0.0;
	public double distanceToValidity = 1.0;
	public double percentOfRuleSetAchieved = 0.0;
	
}
