/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.LabeledGraphMapping;
import de.eich.utils.ComparableList;

@SuppressWarnings("serial")
public class Occurrence extends ComparableList<Integer> {

	public boolean appearsIn(FunctionStructure functionStructure){
		for(Integer nodeID : this)
			if(functionStructure.getFunction(nodeID) == null){
				return false;
			}
		return true;
	}
	
	public boolean overlap(Occurrence occurrence){
		for(int nodeID : occurrence)
			if(contains(nodeID)) return true;
		return false;
	}
	
	public Occurrence reinstantiate(LabeledGraphMapping mapping){
		Occurrence copy = new Occurrence();
		for(Integer oldNodeID : this){
			Integer newNodeID;
			if((newNodeID = mapping.nodeIDMapping.get(oldNodeID)) != null)
				copy.add(newNodeID);
			else
				copy.add(oldNodeID);
		}
		return copy;
	}
	
}
