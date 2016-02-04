/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.functionstructure;

import java.util.Set;
import java.util.TreeSet;

public class Label implements Comparable<Label>{

	protected final LabeledGraph graph;
	protected final Set<Node> nodes = new TreeSet<Node>();
	public final IdentifiableLabelContent content;
	public final int id;
	protected int inUse = 0;
	
	protected Label(LabeledGraph graph, IdentifiableLabelContent content){
		this.graph = graph;
		this.content = content;
		this.id = content.getID();
		graph.labels.put(id, this);
	}

	public int getInUse(){
		return inUse;
	}
	
	public void deleteIfUnused(){
		if(inUse == 0) graph.labels.remove(id);
	}
	
	public boolean equals(Object object){
		if(object instanceof Label)
			return ((Label) object).id == id;
		return super.equals(object);
	}
	
	public String toString(){
		return content.toString();
	}
	
	public int compareTo(Label label) {
		return id - label.id;
	}
	
}
