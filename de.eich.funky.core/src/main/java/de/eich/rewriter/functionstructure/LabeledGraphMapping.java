/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.functionstructure;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class LabeledGraphMapping {

	public Map<Integer, Integer> nodeIDMapping = new TreeMap<Integer, Integer>();
	public Map<Edge, Edge> edgeMapping = new TreeMap<Edge, Edge>();
	
	public LabeledGraphMapping getInverse(){
		LabeledGraphMapping inverse = new LabeledGraphMapping();
		for(Entry<Integer, Integer> entry : nodeIDMapping.entrySet()){
			inverse.nodeIDMapping.put(entry.getValue(), entry.getKey());
		}
		for(Entry<Edge, Edge> entry : edgeMapping.entrySet()){
			inverse.edgeMapping.put(entry.getValue(), entry.getKey());
		}
		return inverse;
	}
	
	public String toString(){
		return nodeIDMapping.toString();
	}
	
	public LabeledGraphMapping copy(){
		LabeledGraphMapping copy = new LabeledGraphMapping();
		copy.nodeIDMapping = new TreeMap<Integer, Integer>(nodeIDMapping);
		return copy;
	}
	
}
