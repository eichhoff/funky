/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.utils.MyObservable;

public abstract class AbstractFunctionalDecomposer extends Thread {

	protected final MyObservable observable = new MyObservable();

	protected final List<AbstractRewriteSystem> rewriters = new ArrayList<AbstractRewriteSystem>();
	protected Blackbox blackbox;
	protected List<Integer> ruleIDs;
	protected Set<Triplet<List<Integer>, FunctionStructure, FunctionStructure>> functionStructures = new HashSet<Triplet<List<Integer>, FunctionStructure, FunctionStructure>>();
	protected Set<Triplet<List<Integer>, FunctionStructure, FunctionStructure>> invalidFunctionStructures = new HashSet<Triplet<List<Integer>, FunctionStructure, FunctionStructure>>();
	protected Set<List<Integer>> successfulRuleIDs = new TreeSet<List<Integer>>();

	protected boolean writeToFile = false;
	
	public final List<Integer> directDerivationCounters = new ArrayList<Integer>();
	
	protected AbstractFunctionalDecomposer(AbstractRewriteSystem rewriter) {
		this.rewriters.add(rewriter);
		this.directDerivationCounters.add(0);
	}

	public void addRewriter(AbstractRewriteSystem rewriter){
		this.rewriters.add(rewriter);
		this.directDerivationCounters.add(0);
	}

	public Observable getObservable() {
		return observable;
	}

	public Blackbox getBlackbox() {
		return blackbox;
	}

	public void setBlackbox(Blackbox blackbox) {
		this.blackbox = blackbox;
	}

	public Set<Triplet<List<Integer>, FunctionStructure, FunctionStructure>> getFunctionStructures() {
		return functionStructures;
	}
	
	public Set<Triplet<List<Integer>, FunctionStructure, FunctionStructure>> getInvalidFunctionStructures() {
		return invalidFunctionStructures;
	}
	
	public List<Integer> getRuleIDs() {
		return ruleIDs;
	}

	public void setRuleIDs(List<Integer> ruleIDs) {
		this.ruleIDs = ruleIDs;
	}

	public void setWriteToFile(boolean writeToFile){
		this.writeToFile = writeToFile;
	}
	
	public Set<List<Integer>> getSuccessfulRuleIDs() {
		return successfulRuleIDs;
	}
	
	public void run() {
		decompose();
	}
	
	protected abstract void decompose();

}
