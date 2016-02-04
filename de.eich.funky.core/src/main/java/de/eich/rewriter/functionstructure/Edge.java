/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.functionstructure;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import de.eich.rewriter.FunctionalBasis;

public class Edge implements Comparable<Edge> {

	protected final LabeledGraph graph;
	public final Node sourceNode;
	public final Node targetNode;
	public final List<Label> labels = new ArrayList<Label>(1);
	public String labelString = labels.toString();
	
	protected Edge(LabeledGraph graph, Node sourceNode, Node targetNode){
		this.graph = graph;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		sourceNode.outgoingEdges.add(this);
		targetNode.incomingEdges.add(this);
		graph.edges.add(this);
		// adapt degree histogram
	//		int newDegree = sourceNode.getDegree();
	//		int oldDegree = newDegree -1;
	//		graph.degreeHistogram[oldDegree]--;
	//		graph.degreeHistogram[newDegree]++;
		//		graph.updateMaxDegree();
		// add function labels of nodes to each others histograms
		if(!targetNode.labels.isEmpty()) 
			sourceNode.labelHistogram[targetNode.labels.get(0).id]++;
		if(!sourceNode.labels.isEmpty()) 
			targetNode.labelHistogram[sourceNode.labels.get(0).id]++;
	}
	
	public void addLabel(Label label){
		labels.add(label);
		label.inUse++;
		labelString = labels.toString();
		// update label histograms of nodes
		sourceNode.labelHistogram[label.id]++;
		targetNode.labelHistogram[label.id]++;
	}
	public void addLabel(int i, Label label){
		labels.add(i, label);
		label.inUse++;
		labelString = labels.toString();
		// update label histograms of nodes
		sourceNode.labelHistogram[label.id]++;
		targetNode.labelHistogram[label.id]++;
	}
	
	public void removeLabel(Label label){
		labels.remove(label);
		label.inUse--;
		labelString = labels.toString();
		// update label histograms of nodes
		sourceNode.labelHistogram[label.id]--;
		targetNode.labelHistogram[label.id]--;
	}
	
	public void removeLabel(int i){
		Label label = labels.remove(i);
		label.inUse--;
		labelString = labels.toString();
		// update label histograms of nodes
		sourceNode.labelHistogram[label.id]--;
		targetNode.labelHistogram[label.id]--;
	}
	
	public void delete(){
		sourceNode.outgoingEdges.remove(this);
		targetNode.incomingEdges.remove(this);
		graph.edges.remove(this);
		for(Label label : labels){
			label.inUse--;
			sourceNode.labelHistogram[label.id]--;
			targetNode.labelHistogram[label.id]--;
		}
		// adapt degree histogram
	//		int newDegree = sourceNode.getDegree();
	//		int oldDegree = newDegree +1;
	//		graph.degreeHistogram[oldDegree]--;
	//		graph.degreeHistogram[newDegree]++;
		//		graph.updateMaxDegree();
		// remove function labels of nodes from each others histograms
		if(!targetNode.labels.isEmpty()) 
			sourceNode.labelHistogram[targetNode.labels.get(0).id]--;
		if(!sourceNode.labels.isEmpty()) 
			targetNode.labelHistogram[sourceNode.labels.get(0).id]--;
	}
	
	public void delete(Node node){
		// spare requesting node - otherwise concurrent modification exception
		if(sourceNode != node) 
			sourceNode.outgoingEdges.remove(this);
		if(targetNode != node)
			targetNode.incomingEdges.remove(this);
		graph.edges.remove(this);
		for(Label label : labels){
			label.inUse--;
			sourceNode.labelHistogram[label.id]--;
			targetNode.labelHistogram[label.id]--;
		}
		// adapt degree histogram
	//		int newDegree = sourceNode.getDegree();
	//		int oldDegree = newDegree +1;
	//		graph.degreeHistogram[oldDegree]--;
	//		graph.degreeHistogram[newDegree]++;
		//		graph.updateMaxDegree();
		// remove function labels of nodes from each others histograms
		if(!targetNode.labels.isEmpty()) 
			sourceNode.labelHistogram[targetNode.labels.get(0).id]--;
		if(!sourceNode.labels.isEmpty()) 
			targetNode.labelHistogram[sourceNode.labels.get(0).id]--;
	}
	
	public boolean equals(Object object){
		if(object instanceof Edge){
			Edge otherEdge = (Edge) object;
			if(otherEdge.labels.size() != labels.size()) return false;
			for(int i=0; i<otherEdge.labels.size(); i++)
				if(!otherEdge.labels.get(i).equals(labels.get(i))) return false;
			if(!otherEdge.sourceNode.equals(sourceNode)) return false;
			if(!otherEdge.targetNode.equals(targetNode)) return false;
			return true;
		}
		return super.equals(object);
	}
	
	public boolean isomorph(Edge edge){
		if(edge.labels.size() != labels.size()) return false;
		for(int i=0; i<edge.labels.size(); i++)
			if(!edge.labels.get(i).equals(labels.get(i))) return false;
		if(!edge.sourceNode.isomorph(sourceNode)) return false;
		if(!edge.targetNode.isomorph(targetNode)) return false;
		return true;
	}
	
	public String toString(){
		return "(" + sourceNode.id + "->" + targetNode.id + " " + labels.toString() +")";
	}
	
	public Pair<Integer,Integer> toIntegerPair(){
		return new Pair<Integer,Integer>(sourceNode.id, targetNode.id);
	}

	public int compareTo(Edge edge) {
		if(sourceNode.id > edge.sourceNode.id) return 1;
		else if(sourceNode.id < edge.sourceNode.id) return -1;
		
		if(targetNode.id > edge.targetNode.id) return 1;
		else if(targetNode.id < edge.targetNode.id) return -1;
		
		return 0;
	}
	
	public double getDirectEdgeSimilarity(Edge edge){
		double directNodeSimilarity = 0.0;
		
		Edge smallEdge, bigEdge;
		boolean thisSmaller = labels.size() < edge.labels.size();
		if(thisSmaller){
			smallEdge = this;
			bigEdge = edge;
		} else {
			smallEdge = edge;
			bigEdge = this;
		}
		
		for(int i=0; i<smallEdge.labels.size(); i++){
			double difference = Math.abs(smallEdge.labels.get(i).id - bigEdge.labels.get(i).id);
			difference /= i == 0 ? FunctionalBasis.FlowLabel.values().length : FunctionalBasis.AttributeLabel.values().length;
			directNodeSimilarity += 1.0 - difference;
		}
		directNodeSimilarity /= bigEdge.labels.size();
		
		return directNodeSimilarity;
	}
	
}
