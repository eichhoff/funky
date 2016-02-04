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

public class RelatedLabels extends AbstractParametrizedOperation {

	protected RelatedLabels(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
			DirectDerivation directDerivation = iterator.next();
			if(parameters.get(0).getValue0() == ParameterType.FUNCTION_VARIABLE || parameters.get(0).getValue0() == ParameterType.FUNCTION_CONSTANT){
				FunctionLabel function1 = rule.getFunction(parameters.get(0), directDerivation);
				FunctionLabel function2 = rule.getFunction(parameters.get(1), directDerivation);
				if(!FunctionLabel.related(function1, function2)){
					iterator.remove();
				}
			} else if(parameters.get(0).getValue0() == ParameterType.FLOW_VARIABLE || parameters.get(0).getValue0() == ParameterType.FLOW_CONSTANT){
				FlowLabel flow1 = rule.getFlow(parameters.get(0), directDerivation);
				FlowLabel flow2 = rule.getFlow(parameters.get(1), directDerivation);
				if(!FlowLabel.related(flow1, flow2)){
					iterator.remove();
				}
			}
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		invoke(rule, source, directDerivations, null, precedingDirectDerivationOfSameRule);
	}

	public void reset(){};
	
}
