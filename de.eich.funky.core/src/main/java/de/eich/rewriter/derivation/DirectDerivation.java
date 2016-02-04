/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.functionstructure.Edge;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.LabeledGraphMapping;
import de.eich.rewriter.functionstructure.Node;

public class DirectDerivation {

	public final Rule rule;
	public List<Integer> option;
	
	public Occurrence left;
	public Occurrence gluing;
	public Occurrence right;
	
	public FunctionStructure source;
	public FunctionStructure target;
	
	public Set<Edge> addedFlows;
	
	public int occurrenceIndex;
	
	public Map<Integer, Integer> nodeVariables = new TreeMap<Integer, Integer>();
	public Map<Integer, FunctionLabel> functionVariables = new TreeMap<Integer, FunctionLabel>();
	public Map<Integer, FunctionLabel> functionAnonymousVariables = new TreeMap<Integer, FunctionLabel>();
	public Map<Integer, FlowLabel> flowVariables = new TreeMap<Integer, FlowLabel>();
	public Map<Integer, FlowLabel> flowAnonymousVariables = new TreeMap<Integer, FlowLabel>();
	
	public DirectDerivation(Rule rule, FunctionStructure source, FunctionStructure target){
		this.rule = rule;
		
		this.option = new ArrayList<Integer>();
		
		this.left = new Occurrence();
		this.gluing = new Occurrence();
		this.right = new Occurrence();
		
		this.source = source;
		this.target = target;
		
		this.addedFlows = new TreeSet<Edge>();
	}
	
	private DirectDerivation(Rule rule){
		this.rule = rule;
	}
	
	public List<Integer> getRemoved(){
		@SuppressWarnings("unchecked")
		List<Integer> removed = (List<Integer>) left.clone();
		removed.removeAll(gluing);
		return removed;
	}

	public DirectDerivation copy(){
		DirectDerivation copy = new DirectDerivation(rule);
		
		copy.option = new ArrayList<Integer>(option);
		
		copy.left = (Occurrence) left.clone();
		copy.gluing = (Occurrence) gluing.clone();
		copy.right = (Occurrence) right.clone();
		
		copy.source = source;
		copy.target = target.copy(false);
		
		copy.addedFlows = new TreeSet<Edge>(addedFlows);
		
		copy.nodeVariables.putAll(nodeVariables);
		copy.functionVariables.putAll(functionVariables);
		copy.functionAnonymousVariables.putAll(functionAnonymousVariables);
		copy.flowVariables.putAll(flowVariables);
		copy.flowAnonymousVariables.putAll(flowAnonymousVariables);
		
		return copy;
	}
	
	public DirectDerivation reinstantiate(FunctionStructure source, LabeledGraphMapping mapping){
		DirectDerivation copy = new DirectDerivation(rule);
		
		copy.option = new ArrayList<Integer>(option);
		
		
		copy.source = source;
		copy.target = target.reinstantiate(source.graph.nodeIDGenerator, mapping); // maybe it should get its id from source generator

		copy.left = left.reinstantiate(mapping);
		copy.gluing = gluing.reinstantiate(mapping);
		copy.right = right.reinstantiate(mapping);
		
		copy.addedFlows = new TreeSet<Edge>();
		for(Edge edge : addedFlows){
			Integer sourceNodeID = mapping.nodeIDMapping.get(edge.sourceNode.id);
			if(sourceNodeID == null)
				sourceNodeID = edge.sourceNode.id;
			
			Integer targetNodeID = mapping.nodeIDMapping.get(edge.targetNode.id);
			if(targetNodeID == null)
				targetNodeID = edge.targetNode.id;
			
			Node sourceNode = copy.target.getFunction(sourceNodeID);
			Node targetNode = copy.target.getFunction(targetNodeID);
			
			Edge newEdge;
			if(edge.labels.isEmpty())
				newEdge = copy.target.getFlow(sourceNode, targetNode);
			else
				newEdge = copy.target.getFlow(sourceNode, targetNode, (FlowLabel) edge.labels.get(0).content);
			
			copy.addedFlows.add(newEdge);
		}
		
		for(Entry<Integer, Integer> entry : nodeVariables.entrySet()){
			Integer oldNodeID = entry.getValue();
			Integer newNodeID;
			if((newNodeID = mapping.nodeIDMapping.get(oldNodeID)) != null)
				copy.nodeVariables.put(entry.getKey(), newNodeID);
			else
				copy.nodeVariables.put(entry.getKey(), oldNodeID);
		}
		
		copy.functionVariables.putAll(functionVariables);
		copy.functionAnonymousVariables.putAll(functionAnonymousVariables);
		copy.flowVariables.putAll(flowVariables);
		copy.flowAnonymousVariables.putAll(flowAnonymousVariables);
		
		return copy;
	}
	
	public String toString(){
		return "\nOccurrence" + left.toString() + "\n" + target.toString() + "\n";
	}
	
}
