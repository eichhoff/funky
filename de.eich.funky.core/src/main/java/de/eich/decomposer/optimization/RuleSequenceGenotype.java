/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer.optimization;

import org.opt4j.core.Genotype;
import org.opt4j.core.genotype.CompositeGenotype;
import org.opt4j.core.genotype.IntegerMapGenotype;
import org.opt4j.core.genotype.PermutationGenotype;

public class RuleSequenceGenotype extends CompositeGenotype<Integer, Genotype> {
	
	public void setRuleOrderGenotype(PermutationGenotype<Integer> genotype){
         put(0, genotype);
	}
	public PermutationGenotype<Integer> getRuleOrderGenotype(){ 
		return get(0);
	}
	
	public void setRuleRepetitionGenotype(IntegerMapGenotype<Integer> genotype){
		put(1, genotype);
	}
	public IntegerMapGenotype<Integer> getRuleRepetitionGenotype(){
		 return get(1); 
	}
	
}
