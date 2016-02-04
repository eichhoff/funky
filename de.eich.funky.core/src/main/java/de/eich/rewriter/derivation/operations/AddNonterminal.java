/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.Set;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.Edge;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

public class AddNonterminal extends AbstractParametrizedOperation {

	protected AddNonterminal(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		switch(operation){
		case ADD_OUTGOING_NONTERMINAL:
			for(DirectDerivation directDerivation : directDerivations){
				Node node1 = rule.getNode(parameters.get(0), directDerivation, directDerivation.target);
				Node node2 = directDerivation.target.addNonterminalFunction();
				FlowLabel flow = rule.getFlow(parameters.get(2), directDerivation);
				Edge edge = directDerivation.target.addFlow(flow, node1, node2);
				directDerivation.addedFlows.add(edge);
				rule.setNode(parameters.get(1), directDerivation, node2.id);
//				directDerivation.right.add(node1.id); is already added by left-hand side or add function
				directDerivation.right.add(node2.id);
				
				if(FunctionStructure.record){
					directDerivation.target.nodeAddedByRule.put(node2.id, rule.id);
					directDerivation.target.edgeAddedByRule.put(edge, rule.id);
				}
			}
			break;
		case ADD_INCOMING_NONTERMINAL:
			for(DirectDerivation directDerivation : directDerivations){
				Node node1 = directDerivation.target.addNonterminalFunction();
				Node node2 = rule.getNode(parameters.get(1), directDerivation, directDerivation.target);
				FlowLabel flow = rule.getFlow(parameters.get(2), directDerivation);
				Edge edge = directDerivation.target.addFlow(flow, node1, node2);
				directDerivation.addedFlows.add(edge);
				rule.setNode(parameters.get(0), directDerivation, node1.id);
				directDerivation.right.add(node1.id);
//				directDerivation.right.add(node2.id); is already added by left-hand side or add function
				
				if(FunctionStructure.record){
					directDerivation.target.nodeAddedByRule.put(node1.id, rule.id);
					directDerivation.target.edgeAddedByRule.put(edge, rule.id);
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
