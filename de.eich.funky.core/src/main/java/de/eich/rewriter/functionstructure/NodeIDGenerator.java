/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.functionstructure;

public class NodeIDGenerator {

	private int nextNodeID = 0;
	
	public void setHigherThan(Node node){
		if(node.id >= nextNodeID)
			nextNodeID = node.id + 1;
	}
	
	public int getBase(){
		// no node has the id nextNodeID yet, so this can be used as base for comparison
		return nextNodeID;
	}
	
	public int getNextNodeID(){
		return nextNodeID++;
	}

	public NodeIDGenerator copy(){
		NodeIDGenerator copy = new NodeIDGenerator();
		copy.nextNodeID = nextNodeID;
		return copy;
	}
	
}
