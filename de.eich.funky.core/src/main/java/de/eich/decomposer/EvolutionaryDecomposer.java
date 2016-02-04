/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.javatuples.Pair;
import org.opt4j.core.Individual;
import org.opt4j.core.optimizer.Archive;
import org.opt4j.core.start.Opt4JTask;
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule;
import org.opt4j.viewer.ViewerModule;

import com.google.inject.Module;

import de.eich.decomposer.optimization.ProblemModule;
import de.eich.decomposer.optimization.SelectorModule;
import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.derivation.DerivationResult;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.utils.ComparableList;

public class EvolutionaryDecomposer extends AbstractFunctionalDecomposer {

	public EvolutionaryDecomposer(AbstractRewriteSystem rewriter) {
		super(rewriter);
	}

	private int populationSize;
	private int parentsSize;
	private int generations;

	private Set<List<Integer>> promisingRuleIDs = new TreeSet<List<Integer>>();
	private Map<Integer,Pair<Integer,Integer>> repetitionLimits = new HashMap<Integer,Pair<Integer,Integer>>();
	
	@Override
	protected void decompose() {
		functionStructures.clear();

		for(AbstractRewriteSystem rewriter : rewriters)
			rewriter.reset();
		
		Writer writer = null;
		if(writeToFile){
			try {
				writer = new BufferedWriter(new FileWriter(new File("eval/evolutionary" + System.currentTimeMillis() + ".csv")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		EvolutionaryAlgorithmModule ea = new EvolutionaryAlgorithmModule();
		ea.setGenerations(generations);
		ea.setAlpha(populationSize);
		ea.setMu(parentsSize);
		
		ViewerModule gui = new ViewerModule();
		gui.setCloseOnStop(false);

		ProblemModule problem = new ProblemModule();
		problem.setRuleIDs(new TreeSet<Integer>(ruleIDs));
		problem.setRewriters(rewriters);
		problem.setDirectDerivationCounters(directDerivationCounters);
		problem.setBlackbox(blackbox);
		problem.setWriter(writer);
		problem.setPromisingRuleIDs(promisingRuleIDs);
		problem.setRepetitionLimits(repetitionLimits);
		
		SelectorModule selector = new SelectorModule();
		
		List<Module> modules = new ArrayList<Module>();
		modules.add(ea);
		modules.add(problem);
		modules.add(selector);
		modules.add(gui);

		Opt4JTask task = new Opt4JTask(false);
		task.init(modules);
		
		successfulRuleIDs.clear();
		
		try {
			task.execute();
			Archive archive = task.getInstance(Archive.class);
			for (Individual individual : archive) {
				DerivationResult derivationResult = (DerivationResult) individual.getPhenotype();
				
				functionStructures.addAll(derivationResult.functionStructures);
				invalidFunctionStructures.addAll(derivationResult.invalidFunctionStructures);
				
				if(derivationResult.notApplicableAtAllRuleIndex == -1){
					successfulRuleIDs.add(derivationResult.ruleIDs);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			task.close();
		}
		
		observable.setChangedAndNotifyObservers(functionStructures);
	}
	
	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}
	
	public int getParentsSize() {
		return parentsSize;
	}

	public void setParentsSize(int parentsSize) {
		this.parentsSize = parentsSize;
	}

	public int getGenerations() {
		return generations;
	}

	public void setGenerations(int generations) {
		this.generations = generations;
	}
	
	public void addPromisingRuleIDs(Set<List<Integer>> promisingRuleIDs){
		this.promisingRuleIDs.addAll(promisingRuleIDs);
	}
	
	public void addRepetitionLimits(int ruleID, Pair<Integer,Integer> repetitionLimits){
		this.repetitionLimits.put(ruleID, repetitionLimits);
	}

}
