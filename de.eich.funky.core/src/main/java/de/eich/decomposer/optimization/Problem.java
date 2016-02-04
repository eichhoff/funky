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

import de.eich.decomposer.Blackbox;
import de.eich.rewriter.AbstractRewriteSystem;

public class Problem {

	protected Set<Integer> ruleIDs;
	protected List<AbstractRewriteSystem> rewriters;
	protected Blackbox blackbox;
	protected List<Integer> directDerivationCounters;
	protected Writer writer = null;
	protected Set<List<Integer>> promisingRuleIDs;
	protected Map<Integer, Pair<Integer,Integer>> repetitionLimits;
	
	public Problem(Set<Integer> ruleIDs, List<AbstractRewriteSystem> rewriters, List<Integer> directDerivationCounters, 
			Blackbox blackbox, Writer writer, Set<List<Integer>> promisingRuleIDs, Map<Integer, Pair<Integer,Integer>> maxRepetition) {
		this.ruleIDs = ruleIDs;
		this.rewriters = rewriters;
		this.directDerivationCounters = directDerivationCounters;
		this.blackbox = blackbox;
		this.writer = writer;
		this.promisingRuleIDs = promisingRuleIDs;
		this.repetitionLimits = maxRepetition;
	}

}
