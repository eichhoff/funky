/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.Iterator;
import java.util.Set;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

public class NotEqual extends AbstractParametrizedOperation {

	protected NotEqual(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
			DirectDerivation directDerivation = iterator.next();
			switch(parameters.get(0).getValue0()){
			case FUNCTION_VARIABLE:
			case FUNCTION_CONSTANT:
			case FUNCTION_ANONYMOUS_VARIABLE:
				FunctionLabel function1 = rule.getFunction(parameters.get(0), directDerivation);
				FunctionLabel function2 = rule.getFunction(parameters.get(1), directDerivation);
				if(function1 == function2){
					iterator.remove();
				}
				break;
			case FLOW_VARIABLE:
			case FLOW_CONSTANT:
			case FLOW_ANONYMOUS_VARIABLE:
				FlowLabel flow1 = rule.getFlow(parameters.get(0), directDerivation);
				FlowLabel flow2 = rule.getFlow(parameters.get(1), directDerivation);
				if(flow1 == flow2){
					iterator.remove();
				}
				break;
			case NODE_VARIABLE:
				Node node1 = rule.getNode(parameters.get(0), directDerivation, directDerivation.source);
				Node node2 = rule.getNode(parameters.get(1), directDerivation, directDerivation.source);
				if(node1 == node2){
					iterator.remove();
				}
				break;
			default:
				break;
			}
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		invoke(rule, source, directDerivations, null, precedingDirectDerivationOfSameRule);
	}

	public void reset(){};
	
}
