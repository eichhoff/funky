/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.Set;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

public class AddFunction extends AbstractParametrizedOperation {

	protected AddFunction(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		for(DirectDerivation directDerivation : directDerivations){
			FunctionLabel function = rule.getFunction(parameters.get(1), directDerivation);
			FlowLabel flow1 = rule.getFlow(parameters.get(2), directDerivation);
			FlowLabel flow2 = rule.getFlow(parameters.get(3), directDerivation);
			Node node = directDerivation.target.addFunction(function, flow1, flow2);
			rule.setNode(parameters.get(0), directDerivation, node.id);
			directDerivation.right.add(node.id);
			
			if(FunctionStructure.record)
				directDerivation.target.nodeAddedByRule.put(node.id, rule.id);
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		invoke(rule, source, directDerivations, null, precedingDirectDerivationOfSameRule);
	}

	public void reset(){};
	
}
