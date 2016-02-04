/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.Iterator;
import java.util.Set;

import de.eich.rewriter.FunctionalBasis.AttributeLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.IdentifiableLabelContent;
import de.eich.rewriter.functionstructure.Node;

public class HasLabel extends AbstractParametrizedOperation {

	protected HasLabel(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
			DirectDerivation directDerivation = iterator.next();
			Node node = rule.getNode(parameters.get(0), directDerivation, directDerivation.source);
			IdentifiableLabelContent other = AttributeLabel.valueOf(rule.getOther(parameters.get(1)));
			if(!directDerivation.target.hasLabel(node, other)){
				iterator.remove();
			}
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		invoke(rule, source, directDerivations, null, precedingDirectDerivationOfSameRule);
	}

	public void reset(){};
	
}
