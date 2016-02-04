/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.Edge;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

public class UseNonterminals extends AbstractParametrizedOperation {

	protected UseNonterminals(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	private void initDirectDerivation(DirectDerivation directDerivation, Edge edge){
		directDerivation.left.add(edge.sourceNode.id);
		directDerivation.left.add(edge.targetNode.id);

		Node sourceNode = directDerivation.target.getFunction(edge.sourceNode.id);
		Node targetNode = directDerivation.target.getFunction(edge.targetNode.id);
		
		rule.setNode(parameters.get(0), directDerivation, edge.sourceNode.id);
		rule.setNode(parameters.get(1), directDerivation, edge.targetNode.id);
		rule.setFlow(parameters.get(2), directDerivation, (FlowLabel) edge.labels.get(0).content);

		if(operation == OperationType.USE_OUTGOING_NONTERMINALS){
			directDerivation.target.removeFunction(targetNode);
			directDerivation.gluing.add(sourceNode.id);
			directDerivation.right.add(sourceNode.id);
		} else {
			directDerivation.target.removeFunction(sourceNode);
			directDerivation.gluing.add(targetNode.id);
			directDerivation.right.add(targetNode.id);
		}
	}
	
	private void getDirectDerivationsForEdges(DirectDerivation prototypeDirectDerivation, Set<Edge> edges, Set<DirectDerivation> directDerivations, boolean addPrototype){
		int i = 0;
		DirectDerivation directDerivation;
		for(Edge edge : edges){
			if(++i == edges.size()){
				directDerivation = prototypeDirectDerivation;
				if(addPrototype) directDerivations.add(directDerivation);
				initDirectDerivation(directDerivation, edge);
			} else {
				directDerivations.add(directDerivation = prototypeDirectDerivation.copy());
				initDirectDerivation(directDerivation, edge);
			}
			
			if(FunctionStructure.record){
				directDerivation.target.nodeUsedByRule.put(edge.sourceNode.id, rule.id);
				directDerivation.target.nodeUsedByRule.put(edge.targetNode.id, rule.id);
				directDerivation.target.edgeUsedByRule.put(edge, rule.id);
			}
		}
	}
	
	public void filterAvoidOccurrences(Set<Edge> edges, DirectDerivation directDerivation, Set<Occurrence> avoidOccurrences){
		if(avoidOccurrences == null) return;
		
		if(directDerivation == null){
			for (Iterator<Edge> iterator = edges.iterator(); iterator.hasNext();) {
				Edge edge = iterator.next();
				for(Occurrence occurrence : avoidOccurrences)
					if(occurrence.size() == 2 && occurrence.get(0) == edge.sourceNode.id && occurrence.get(1) == edge.targetNode.id)
						iterator.remove();
			}
		} else {
			for (Iterator<Edge> iterator = edges.iterator(); iterator.hasNext();) {
				Edge edge = iterator.next();
				directDerivation.left.add(edge.sourceNode.id);
				directDerivation.left.add(edge.targetNode.id);
				if(avoidOccurrences.contains(directDerivation.left))
					iterator.remove();
				directDerivation.left.remove(directDerivation.left.size() -1);
				directDerivation.left.remove(directDerivation.left.size() -1);
			}
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		if(source != null){
			FunctionStructure target = source.copy(false);
			FlowLabel flow = rule.getFlow(parameters.get(2), null);
			Set<Edge> edges = operation == OperationType.USE_OUTGOING_NONTERMINALS ? target.getOutgoingNonterminalFlows(flow) : target.getIncomingNonterminalFlows(flow);
			filterAvoidOccurrences(edges, null, avoidOccurrences);
			if(!edges.isEmpty()){
				DirectDerivation directDerivation = new DirectDerivation(rule, source, target); 
				getDirectDerivationsForEdges(directDerivation, edges, directDerivations, true);
			}
		} else {
			Set<DirectDerivation> additionalDirectDerivations = new HashSet<DirectDerivation>();
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				FlowLabel flow = rule.getFlow(parameters.get(2), directDerivation);
				Set<Edge> edges = operation == OperationType.USE_OUTGOING_NONTERMINALS ? directDerivation.target.getOutgoingNonterminalFlows(flow) : directDerivation.target.getIncomingNonterminalFlows(flow);
				filterAvoidOccurrences(edges, directDerivation, avoidOccurrences);
				if(!edges.isEmpty()){
					// remove edge and node from target
					getDirectDerivationsForEdges(directDerivation, edges, additionalDirectDerivations, false);
				} else {
					iterator.remove();
				}
			}
			directDerivations.addAll(additionalDirectDerivations);
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		if(source != null){
			if(occurrence.size() < 2) return;
			int nodeID1 = occurrence.get(0);
			int nodeID2 = occurrence.get(1);
			FunctionStructure target = source.copy(false);
			Node sourceNode = target.getFunction(nodeID1);
			if(sourceNode == null) return;
			Node targetNode = target.getFunction(nodeID2);
			if(targetNode == null) return;
			Set<Edge> edges = new TreeSet<Edge>();
			if(target.getFlow(sourceNode, targetNode) == null){
				System.out.println(sourceNode +" "+ targetNode);
				System.out.println(target);
			}
			edges.add(target.getFlow(sourceNode, targetNode));
			if(!edges.isEmpty()){
				DirectDerivation directDerivation = new DirectDerivation(rule, source, target); 
				directDerivation.occurrenceIndex = 2;
				getDirectDerivationsForEdges(directDerivation, edges, directDerivations, true);
			}
		} else {
			Set<DirectDerivation> additionalDirectDerivations = new HashSet<DirectDerivation>();
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				if(directDerivation.occurrenceIndex +1 >= occurrence.size()){ 
					iterator.remove();
					continue;
				}
				int nodeID1 = occurrence.get(directDerivation.occurrenceIndex++);
				int nodeID2 = occurrence.get(directDerivation.occurrenceIndex++);
				Node sourceNode = directDerivation.target.getFunction(nodeID1);
				if(sourceNode == null){
					iterator.remove();
					continue;
				}
				Node targetNode = directDerivation.target.getFunction(nodeID2);
				if(targetNode == null){
					iterator.remove();
					continue;
				}
				Set<Edge> edges = new TreeSet<Edge>();
				edges.add(directDerivation.target.getFlow(sourceNode, targetNode));
				if(!edges.isEmpty()){
					// remove edge and node from target
					getDirectDerivationsForEdges(directDerivation, edges, additionalDirectDerivations, false);
				} else {
					iterator.remove();
				}
			}
			directDerivations.addAll(additionalDirectDerivations);
		}
	}

	public void reset(){};
	
}
