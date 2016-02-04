/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.Iterator;
import java.util.Set;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

public class WithoutFlow extends AbstractParametrizedOperation {

	protected WithoutFlow(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		switch(operation){
		case WITHOUT_OUTGOING_FLOW:
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				Node node = rule.getNode(parameters.get(0), directDerivation, directDerivation.source);
				FlowLabel flow = rule.getFlow(parameters.get(1), directDerivation);
				if(!directDerivation.target.withoutOutgoingFlow(flow, node)){
					iterator.remove();
				}
			}
			break;			
		case WITHOUT_INCOMING_FLOW:
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				Node node = rule.getNode(parameters.get(0), directDerivation, directDerivation.source);
				FlowLabel flow = rule.getFlow(parameters.get(1), directDerivation);				
				if(!directDerivation.target.withoutIncomingFlow(flow, node)){
					iterator.remove();
				}
			}
			break;
		default:
			break;
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		invoke(rule, source, directDerivations, null, precedingDirectDerivationOfSameRule);
	}

	public void reset(){};
	
}
