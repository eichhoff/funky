/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.Set;

import de.eich.rewriter.FunctionalBasis.AttributeLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.IdentifiableLabelContent;
import de.eich.rewriter.functionstructure.Node;

public class AddLabel extends AbstractParametrizedOperation {

	protected AddLabel(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		for(DirectDerivation directDerivation : directDerivations){
			Node node = rule.getNode(parameters.get(0), directDerivation, directDerivation.target);
			IdentifiableLabelContent other = AttributeLabel.valueOf(rule.getOther(parameters.get(1)));
			directDerivation.target.addLabel(node, other);
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		invoke(rule, source, directDerivations, null, precedingDirectDerivationOfSameRule);
	}

	public void reset(){};
	
}
