/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.decomposer.optimization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opt4j.core.Individual;
import org.opt4j.core.common.random.Rand;
import org.opt4j.optimizers.ea.SelectorDefault;

import com.google.inject.Inject;

import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.Rewriter;
import de.eich.rewriter.derivation.DerivationResult;

public class Selector implements org.opt4j.optimizers.ea.Selector{

	@Inject
	private Problem problem;
	
	private SelectorDefault selector; 
	
	@Inject
	public Selector(Rand random){
		selector = new SelectorDefault(random);
	}
	
	public Collection<Individual> getLames(int arg0, Collection<Individual> arg1) {
		Collection<Individual> lames = selector.getLames(arg0, arg1);
		return lames;
	}

	public Collection<Individual> getParents(int arg0, Collection<Individual> all) {
		Collection<Individual> parents = selector.getParents(arg0, all);
		for(AbstractRewriteSystem rewriter : problem.rewriters){
			if(rewriter instanceof Rewriter){
				Set<DerivationResult> derivationResultsParents = new HashSet<DerivationResult>();
				Set<DerivationResult> derivationResultsAll = new HashSet<DerivationResult>();
				for(Individual individual : parents){
					derivationResultsParents.add((DerivationResult) individual.getPhenotype());
				}
				for(Individual individual : all){
					derivationResultsAll.add((DerivationResult) individual.getPhenotype());
				}
				((Rewriter) rewriter).postProcessEvolutionary(derivationResultsParents, derivationResultsAll);
			}
		}
		return parents;
	}

	public void init(int maxsize) {
		selector.init(maxsize);
	}

}
