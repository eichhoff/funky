/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer.optimization;

import org.opt4j.core.optimizer.Optimizer;
import org.opt4j.core.optimizer.OptimizerIterationListener;
import org.opt4j.core.optimizer.OptimizerStateListener;

public class Output implements OptimizerIterationListener, OptimizerStateListener {
	
	public void iterationComplete(int iteration) {
		System.out.print(".");
	}

	public void optimizationStarted(Optimizer arg0) {
		System.out.print("SEARCHING FUNCTION STRUCTURES");
	}

	public void optimizationStopped(Optimizer arg0) {
		System.out.println("DONE");
	}
	
}
