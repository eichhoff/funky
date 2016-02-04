/**
 * @author Julian Eichhoff
 *
 * Copyright 2013 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

/**
 * 
 */
public class GetFunctions extends AbstractParametrizedOperation {

	protected GetFunctions(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	private void initDirectDerivation(DirectDerivation directDerivation, Node node){
		directDerivation.left.add(node.id);
		directDerivation.gluing.add(node.id);
		directDerivation.right.add(node.id);

		node = directDerivation.target.getFunction(node.id);
		
		rule.setNode(parameters.get(0), directDerivation, node.id);
		rule.setFunction(parameters.get(1), directDerivation, (FunctionLabel) node.labels.get(0).content);
		rule.setFlow(parameters.get(2), directDerivation, (FlowLabel) node.labels.get(1).content);
		if(node.labels.size() == 2) 
			System.out.println(node);
		rule.setFlow(parameters.get(3), directDerivation, (FlowLabel) node.labels.get(2).content);
	}
	
	private void getDirectDerivationsForNodes(DirectDerivation prototypeDirectDerivation, Set<Node> nodes, Set<DirectDerivation> directDerivations, boolean addPrototype){
		int i = 0;
		DirectDerivation directDerivation;
		for(Node node : nodes){
			if(++i == nodes.size()){
				directDerivation = prototypeDirectDerivation;
				if(addPrototype) directDerivations.add(directDerivation);
			} else {
				directDerivations.add(directDerivation = prototypeDirectDerivation.copy());
			}
			initDirectDerivation(directDerivation, node);
			
			if(FunctionStructure.record)
				directDerivation.target.nodeUsedByRule.put(node.id, rule.id);
		}
	}
	
	public void filterAvoidOccurrences(Set<Node> nodes, DirectDerivation directDerivation, Set<Occurrence> avoidOccurrences){
		if(avoidOccurrences == null) return;
		
		if(directDerivation == null){
			for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
				Node node = iterator.next();
				for(Occurrence occurrence : avoidOccurrences)
					if(occurrence.size() == 1 && occurrence.get(0) == node.id)
						iterator.remove();
			}
		} else {
			for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
				Node node = iterator.next();
				directDerivation.left.add(node.id);
				if(avoidOccurrences.contains(directDerivation.left))
					iterator.remove();
				directDerivation.left.remove(directDerivation.left.size() -1);
			}
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		if(source != null){
			FunctionStructure target = source.copy(false);
			FunctionLabel function = rule.getFunction(parameters.get(1), null);
			FlowLabel flow1 = rule.getFlow(parameters.get(2), null);
			FlowLabel flow2 = rule.getFlow(parameters.get(3), null);
			Set<Node> nodes = target.getFunctions(function, flow1, flow2);
//			System.out.println(function + " " + flow1  + " " + flow2 + nodes);
			filterAvoidOccurrences(nodes, null, avoidOccurrences);
			if(!nodes.isEmpty()){
				DirectDerivation directDerivation = new DirectDerivation(rule, source, target); 
				getDirectDerivationsForNodes(directDerivation, nodes, directDerivations, true);
			}
		} else {
			Set<DirectDerivation> additionalDirectDerivations = new HashSet<DirectDerivation>();
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				FunctionLabel function = rule.getFunction(parameters.get(1), directDerivation);
				FlowLabel flow1 = rule.getFlow(parameters.get(2), directDerivation);
				FlowLabel flow2 = rule.getFlow(parameters.get(3), directDerivation);
				Set<Node> nodes = directDerivation.target.getFunctions(function, flow1, flow2);
//				System.out.println(function + " " + flow1  + " " + flow2 + nodes);
				filterAvoidOccurrences(nodes, directDerivation, avoidOccurrences);
				if(!nodes.isEmpty()){
					getDirectDerivationsForNodes(directDerivation, nodes, additionalDirectDerivations, false);
				} else {
					iterator.remove();
				}
			}
			directDerivations.addAll(additionalDirectDerivations);
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) {
		if(source != null){
			if(occurrence.size() < 1) return;
			FunctionStructure target = source.copy(false);
			int nodeID = occurrence.get(0);
			Node node = target.getFunction(nodeID);
			if(node != null){
				Set<Node> nodes = new TreeSet<Node>();
				nodes.add(node);
				DirectDerivation directDerivation = new DirectDerivation(rule, source, target); 
				directDerivation.occurrenceIndex = 1;
				getDirectDerivationsForNodes(directDerivation, nodes, directDerivations, true);
			}
		} else {
			Set<DirectDerivation> additionalDirectDerivations = new HashSet<DirectDerivation>();
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				if(directDerivation.occurrenceIndex >= occurrence.size()){
					iterator.remove();
					continue;
				}
				int nodeID = occurrence.get(directDerivation.occurrenceIndex++);
				Node node = directDerivation.target.getFunction(nodeID);
				if(node != null){
					Set<Node> nodes = new TreeSet<Node>();
					nodes.add(node);
					getDirectDerivationsForNodes(directDerivation, nodes, additionalDirectDerivations, false);
				} else {
					iterator.remove();
				}
			}
			directDerivations.addAll(additionalDirectDerivations);
		}
	}
	
	public void reset(){};

}
