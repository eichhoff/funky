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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.derivation.DerivationResult;
import de.eich.utils.ComparableList;

public class RandomDecomposer extends AbstractFunctionalDecomposer {

	private static final Logger log = LoggerFactory.getLogger(RandomDecomposer.class);
	
	private int n;
	
	public RandomDecomposer(AbstractRewriteSystem rewriter) {
		super(rewriter);
	}
	
	@Override
	protected void decompose() {
		functionStructures.clear();
		
		for(AbstractRewriteSystem rewriter : rewriters)
			rewriter.reset();
		
		Writer writer = null;
		if(writeToFile){
			try {
				writer = new BufferedWriter(new FileWriter(new File("eval/random" + System.currentTimeMillis() + ".csv")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Set<List<Integer>> testedRuleIDs = new TreeSet<List<Integer>>();
		for (int i = 0; i < n; i++) {
			Collections.shuffle(ruleIDs);
			if (testedRuleIDs.contains(ruleIDs)) {
				i--;
				continue;
			}
			testedRuleIDs.add(new ComparableList<Integer>(ruleIDs));
			// derivation result is taken from last rewriter in list
			int j = 0;
			Integer na = null;
			DerivationResult derivationResult = null;
			for(AbstractRewriteSystem rewriter : rewriters){
				AbstractRewriteSystem.directDerivationCounter = 0;
				derivationResult = rewriter.derive(blackbox.getFunctionStructure().copy(true), ruleIDs);
				
				directDerivationCounters.set(j, 
						directDerivationCounters.get(j++) 
						+ AbstractRewriteSystem.directDerivationCounter);
				
				functionStructures.addAll(derivationResult.functionStructures);
				invalidFunctionStructures.addAll(derivationResult.invalidFunctionStructures);
				
				if(derivationResult.notApplicableAtAllRuleIndex == -1){
					successfulRuleIDs.add(derivationResult.ruleIDs);
				}
				
				if(writeToFile){
					if(na == null)
						na = derivationResult.canonicalForm.notApplicableAtAllRuleIndex;
					else if(na != derivationResult.canonicalForm.notApplicableAtAllRuleIndex){
						log.error("DIFFERENT N/A " + ruleIDs);
						System.exit(0);
					}
				}
			}
			functionStructures.addAll(derivationResult.functionStructures);
			
			if(writeToFile){
				if(rewriters.size() == 2){
					log.info(ruleIDs + " " + directDerivationCounters + " DELTA: " + (directDerivationCounters.get(0) - directDerivationCounters.get(1)) + " TESTED: " + testedRuleIDs.size());
					try {
						writer.write(directDerivationCounters.get(0) + ";" + directDerivationCounters.get(1) + "\n");
						writer.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					log.info(i + " " + directDerivationCounters);
				}
			}
		}
		
		observable.setChangedAndNotifyObservers(functionStructures);
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

}
