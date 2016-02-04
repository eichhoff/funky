/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

public interface ExternalHandlerInterface {
	
	public boolean applicable(String parameter, DirectDerivation directDerivation);
	
}
