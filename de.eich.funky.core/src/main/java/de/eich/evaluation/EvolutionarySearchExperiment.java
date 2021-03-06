/**
 * @author Julian Eichhoff
 *
 * Copyright 2013 Julian Eichhoff
 */

package de.eich.evaluation;


import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eich.decomposer.Blackbox;
import de.eich.decomposer.EvolutionaryDecomposer;
import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.Rewriter;
import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.Rewriter.RewriteMode;
import de.eich.rewriter.derivation.Rule;
import de.eich.utils.ComparableList;

public class EvolutionarySearchExperiment {

	private static final Logger log = LoggerFactory.getLogger(EvolutionarySearchExperiment.class);
	
	public static void main(String[] args) {
		log.info("Strating evolutionary search experiment");
		test(400);
	}

	public static void test(final int runs){
		try {
			final int generations = 200;
			final int populationSize = 100;
			final int parentsSize = 25;
			
			Rule.closeGapsRuleID = 16;
			
			final List<Integer> ruleIDs = new ComparableList<Integer>();
			
			// Propagation rules:
			ruleIDs.add(26);
			ruleIDs.add(4);
			ruleIDs.add(29);
			ruleIDs.add(24);
			ruleIDs.add(27);
			ruleIDs.add(5);
			ruleIDs.add(25);
			ruleIDs.add(20);
			
			// Termination rules:
			ruleIDs.add(3);
			ruleIDs.add(6); 
			ruleIDs.add(17);
			ruleIDs.add(33);
			
			final Blackbox blackbox = new Blackbox();
			blackbox.addImport(FlowLabel.HUMAN);
			blackbox.addExport(FlowLabel.HUMAN);
			blackbox.addImport(FlowLabel.HUMANENERGY);  
			blackbox.addExport(FlowLabel.HUMANENERGY);
			
			blackbox.addImport(FlowLabel.ELECTRICALENERGY);
			blackbox.addImport(FlowLabel.SOLID);
			blackbox.addExport(FlowLabel.SOLID);
			blackbox.addExport(FlowLabel.SOLID);
			blackbox.addReactionForceExport(FlowLabel.MECHANICALENERGY);
			
			blackbox.addPrimaryFunction(FunctionLabel.SEPARATE, FlowLabel.SOLID, FlowLabel.SOLID);

			AbstractRewriteSystem.directDerivationCounter = 0;
			AbstractRewriteSystem.directDerivationCounter2 = 0;
			
			// Dynamic rule independence analysis:
			final Rewriter rewriter1 = new Rewriter();
			rewriter1.rewriteMode = RewriteMode.TEST_BEST;
			
			// Common configuration analysis:
			final Rewriter rewriter2 = new Rewriter();
			rewriter2.rewriteMode = RewriteMode.NO_TESTING;
			
			for(int ruleID : ruleIDs)
				rewriter1.add(new Rule(ruleID));
			rewriter1.add(new Rule(Rule.closeGapsRuleID));
			rewriter1.readFromXML("data/ruleschema.xsd", "data/ruleset.xml");

			for(int ruleID : ruleIDs)
				rewriter2.add(new Rule(ruleID));
			rewriter2.add(new Rule(Rule.closeGapsRuleID));
			rewriter2.readFromXML("data/ruleschema.xsd", "data/ruleset.xml");
			
			final EvolutionaryDecomposer functionalDecomposer = new EvolutionaryDecomposer(rewriter1);
			functionalDecomposer.setBlackbox(blackbox);
			functionalDecomposer.setRuleIDs(ruleIDs);

			functionalDecomposer.setGenerations(generations);
			functionalDecomposer.setPopulationSize(populationSize);
			functionalDecomposer.setParentsSize(parentsSize);

			functionalDecomposer.addRewriter(rewriter2);
			functionalDecomposer.setWriteToFile(true);

			functionalDecomposer.getObservable().addObserver(new Observer() {
				public void update(Observable arg0, Object arg1) {
					System.out.println("DONE.");
					AbstractRewriteSystem.directDerivationCounter = 0;
					if(runs > 1) test(runs -1);
				}
			});
			
			functionalDecomposer.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
