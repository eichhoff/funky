/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer.optimization;


import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;

import com.google.inject.Provides;

import de.eich.decomposer.Blackbox;
import de.eich.rewriter.AbstractRewriteSystem;


public class ProblemModule extends org.opt4j.core.problem.ProblemModule {

	private Set<Integer> ruleIDs;
	private List<AbstractRewriteSystem> rewriters;
	private List<Integer> directDerivationCounters;
	private Blackbox blackbox;
	private Writer writer;
	private Set<List<Integer>> promisingRuleIDs;
	private Map<Integer,Pair<Integer,Integer>> repetitionLimits;
	
	public void setRuleIDs(Set<Integer> ruleIDs) {
		this.ruleIDs = ruleIDs;
	}

	public void setRewriters(List<AbstractRewriteSystem> rewriters) {
		this.rewriters = rewriters;
	}
	
	public void setDirectDerivationCounters(List<Integer> directDerivationCounters) {
		this.directDerivationCounters = directDerivationCounters;
	}
	
	public void setBlackbox(Blackbox blackbox) {
		this.blackbox = blackbox;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void setPromisingRuleIDs(Set<List<Integer>> promisingRuleIDs) {
		this.promisingRuleIDs = promisingRuleIDs;
	}
	
	public void setRepetitionLimits(Map<Integer,Pair<Integer,Integer>> maxRepetition) {
		this.repetitionLimits = maxRepetition;
	}
	
	@Provides
	public Problem provideProblem() {
		return new Problem(ruleIDs, rewriters, directDerivationCounters, blackbox, writer, promisingRuleIDs, repetitionLimits);
	}

	@Override
	public void config() {
		bind(Output.class).in(SINGLETON);
		this.addOptimizerIterationListener(Output.class);
		this.addOptimizerStateListener(Output.class);
		
		bindProblem(Creator.class, Decoder.class, Evaluator.class);
	}

}
