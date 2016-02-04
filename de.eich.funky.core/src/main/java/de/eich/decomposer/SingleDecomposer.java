/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer;

import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.derivation.DerivationResult;

public class SingleDecomposer extends AbstractFunctionalDecomposer {

	public SingleDecomposer(AbstractRewriteSystem rewriter) {
		super(rewriter);
	}

	@Override
	protected void decompose() {
		functionStructures.clear();

		for (AbstractRewriteSystem rewriter : rewriters)
			rewriter.reset();

		// derivation result is taken from last rewriter in list
		int j = 0;
		DerivationResult derivationResult = null;
		for (AbstractRewriteSystem rewriter : rewriters) {
			derivationResult = rewriter.derive(blackbox.getFunctionStructure()
					.copy(true), ruleIDs);
			
			directDerivationCounters.set(j, directDerivationCounters.get(j++)
					+ AbstractRewriteSystem.directDerivationCounter);
			
			functionStructures.addAll(derivationResult.functionStructures);
			invalidFunctionStructures.addAll(derivationResult.invalidFunctionStructures);
			
			if(derivationResult.notApplicableAtAllRuleIndex == -1){
				successfulRuleIDs.add(derivationResult.ruleIDs);
			}
		}
		functionStructures.addAll(derivationResult.functionStructures);

		observable.setChangedAndNotifyObservers(functionStructures);
	}

}
