/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.functionstructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.javatuples.Pair;

import de.eich.rewriter.FunctionalBasis;

public class Node implements Comparable<Node>{

	protected final LabeledGraph graph;
	public final Set<Edge> outgoingEdges = new TreeSet<Edge>();
	public final Set<Edge> incomingEdges = new TreeSet<Edge>();
	public final List<Label> labels = new ArrayList<Label>(3);
	public final Integer id;
	public final int[] labelHistogram = new int[FunctionalBasis.AttributeLabel.values().length
	                                            + FunctionalBasis.FunctionLabel.values().length
	                                            + FunctionalBasis.FlowLabel.values().length];
	public String labelString = labels.toString();
	
	protected Node(LabeledGraph graph){
		this.graph = graph;
		this.id = graph.nodeIDGenerator.getNextNodeID();
		graph.nodes.put(id, this);
		//		graph.degreeHistogram[0]++;
	}
	
	protected Node(LabeledGraph graph, Integer id){
		this.graph = graph;
		this.id = id;
		graph.nodes.put(id, this);
		graph.nodeIDGenerator.setHigherThan(this);
		//		graph.degreeHistogram[0]++;
	}

	public void addLabel(Label label){
		labels.add(label);
		label.nodes.add(this);
		label.inUse++;
		labelString = labels.toString();
//		labelHistogram[label.id]++;
	}
	
	public void addFirstLabel(Label label){
		labels.add(0, label);
		label.nodes.add(this);
		label.inUse++;
		labelString = labels.toString();
//		labelHistogram[label.id]++;
	}
	
	public void addLabel(int i, Label label){
		labels.add(i, label);
		label.nodes.add(this);
		label.inUse++;
		labelString = labels.toString();
//		labelHistogram[label.id]++;
	}
	
	public void removeLabel(Label label){
		labels.remove(label);
		label.nodes.remove(this);
		label.inUse--;
		labelString = labels.toString();
//		labelHistogram[label.id]--;
	}
	
	public void removeLabel(int i){
		Label label = labels.remove(i);
		label.nodes.remove(this);
		label.inUse--;
		labelString = labels.toString();
//		labelHistogram[label.id]--;
	}
	
	public int getDegree(){
		return outgoingEdges.size();
	}
	
	public void delete(){
//		int oldDegree = getDegree();
		for(Iterator<Edge> iterator = incomingEdges.iterator(); iterator.hasNext();){
			iterator.next().delete(this);
			iterator.remove();
		}
		for(Iterator<Edge> iterator = outgoingEdges.iterator(); iterator.hasNext();){
			iterator.next().delete(this);
			iterator.remove();
		}
		for(Label label : labels){
			label.nodes.remove(this);
			label.inUse--;
		}
		graph.nodes.remove(id);
//		if(oldDegree == graph.maxDegree)
//			graph.updateMaxDegree();
	}
	
	public boolean equals(Object object){
		if(object instanceof Node) {
			Node otherNode = (Node) object;
			if(otherNode.id != id) return false;
			if(otherNode.labels.size() != labels.size()) return false;
			for(int i=0; i<otherNode.labels.size(); i++)
				if(!otherNode.labels.get(i).equals(labels.get(i))) return false;
			return true;
		}
			
		return super.equals(object);
	}
	
	public boolean isomorph(Node node){
		if(node.labels.size() != labels.size()) return false;
		for(int i=0; i<node.labels.size(); i++)
			if(!node.labels.get(i).equals(labels.get(i))) return false;
		return true;
	}
	
	public String toString(){
		return "(" + id + " " + labels.toString() +")";
	}

	public int compareTo(Node node) {
		return id - node.id;
	}
	
	public void initSimilarity(Node node, Map<Pair<Node,Node>,Double> similarities){
//		double differenceSum = 0.0;
//		for(int i = 0; i < labelHistogram.length; i++){
//			if(labelHistogram[i] == 0 && node.labelHistogram[i] == 0) continue;
//			differenceSum += Math.abs(labelHistogram[i] - node.labelHistogram[i]) / (Math.max(labelHistogram[i], node.labelHistogram[i]));
//		}
//		similarities.put(new Pair<Node,Node>(this,node), 1.0 - (differenceSum / labelHistogram.length));
		similarities.put(new Pair<Node,Node>(this,node), getDirectNodeSimilarity(node));
	}
//	public void initSimilarity(Node node, Map<Pair<Node,Node>,Double> similarities){
//		StringMatcher stringMatcher = new StringMatcher();
//		double textualSimilartiy = stringMatcher.computeStringSimilarity(this.labelString, node.labelString);
//		similarities.put(new Pair<Node,Node>(this,node), textualSimilartiy);
//	}
//	
//	public double getDirectNodeSimilarity(Node node){
//		StringMatcher stringMatcher = new StringMatcher();
//		double textualSimilartiy = stringMatcher.computeStringSimilarity(this.labelString, node.labelString);
//		return textualSimilartiy;
//	}
	
	public double getDirectNodeSimilarity(Node node){
		double directNodeSimilarity = 0.0;
		
		// both nodes do not have labels at all - they got to be non-terminals
		if(labels.size() == 0 && node.labels.size() == 0){
			directNodeSimilarity = 1.0;
			
		// one of the nodes does not have labels - at least one node is a non-terminal - only compare attribute labels
		} else if(labels.size() < 3 || node.labels.size() < 3){
			boolean thisSmaller = labels.size() < node.labels.size();
			if(thisSmaller){
				for(int i=0; i<labels.size(); i++){
					for(int j=0; j<node.labels.size(); j++){
						if(labels.get(i).content instanceof FunctionalBasis.AttributeLabel 
								&& node.labels.get(j).content instanceof FunctionalBasis.AttributeLabel){
							double difference = Math.abs(labels.get(i).id - node.labels.get(j).id);
							difference /= FunctionalBasis.AttributeLabel.values().length;
							directNodeSimilarity += 1.0 - difference;
						}
					}
				}
				directNodeSimilarity /= node.labels.size();
			} else {
				for(int j=0; j<node.labels.size(); j++){
					for(int i=0; i<labels.size(); i++){
						if(labels.get(i).content instanceof FunctionalBasis.AttributeLabel 
								&& node.labels.get(j).content instanceof FunctionalBasis.AttributeLabel){
							double difference = Math.abs(labels.get(i).id - node.labels.get(j).id);
							difference /= FunctionalBasis.AttributeLabel.values().length;
							directNodeSimilarity += 1.0 - difference;
						}
					}
				}
				directNodeSimilarity /= labels.size();
			}
			
		// both nodes have labels
		} else {
			boolean thisSmaller = labels.size() < node.labels.size();
			if(thisSmaller){
				for(int i=0; i<labels.size(); i++){
					double difference = Math.abs(labels.get(i).id - node.labels.get(i).id);
					difference /= i == 0 ? FunctionalBasis.FunctionLabel.values().length : i <= 2 ? FunctionalBasis.FlowLabel.values().length : FunctionalBasis.AttributeLabel.values().length;
					directNodeSimilarity += 1.0 - difference;
				}
				directNodeSimilarity /= node.labels.size();
			} else {
				for(int i=0; i<node.labels.size(); i++){
					double difference = Math.abs(labels.get(i).id - node.labels.get(i).id);
					difference /= i == 0 ? FunctionalBasis.FunctionLabel.values().length : i <= 2 ? FunctionalBasis.FlowLabel.values().length : FunctionalBasis.AttributeLabel.values().length;
					directNodeSimilarity += 1.0 - difference;
				}
				directNodeSimilarity /= labels.size();
			}
		}
		
		return directNodeSimilarity;
	}
	
//	private List<Edge> temp = new ArrayList<Edge>();
	public double getIncomingSimilarity(Node node, Map<Pair<Node,Node>,Double> similarities){
		double incomingSimilarity = 0.0;
		List<Edge> temp = new ArrayList<Edge>();
		if(incomingEdges.size() == 0 && node.incomingEdges.size() == 0){
			incomingSimilarity = 1.0;
		} else {
			boolean thisSmaller = incomingEdges.size() < node.incomingEdges.size();
			Collection<Edge> a;
			List<Edge> b;
			if(thisSmaller){ // find matches for all elements of smaller set
				a = incomingEdges;
				temp.clear();
				temp.addAll(node.incomingEdges);
				b = temp;
			} else {
				a = node.incomingEdges;
				temp.clear();
				temp.addAll(incomingEdges);
				b = temp;
			}
			
			int size = b.size();
			
			// match neighbors
			for(Edge edge1 : a){
				double maxSimilarity = Double.NEGATIVE_INFINITY;
//				Edge maxSimilarEdge = null;
				int maxSimilarEdgeIndex = -1;
        		for(int j=0; j<b.size(); j++){
        			Edge edge2 = b.get(j);
					Pair<Node,Node> key = thisSmaller ? new Pair<Node,Node>(edge1.sourceNode,edge2.sourceNode) : new Pair<Node,Node>(edge2.sourceNode,edge1.sourceNode);
					double similarity = similarities.get(key);
					if(similarity > maxSimilarity) {
						maxSimilarity = similarity;
//						maxSimilarEdge = edge2;
						maxSimilarEdgeIndex = j;
					}
				}
				incomingSimilarity += maxSimilarity;
				b.remove(maxSimilarEdgeIndex);
			}
			
			incomingSimilarity /= size;
		}
		
		return incomingSimilarity;
	}
	
	public double getOutgoingSimilarity(Node node, Map<Pair<Node,Node>,Double> similarities){
		double outgoingSimilarity = 0.0;
		List<Edge> temp = new ArrayList<Edge>();
		if(outgoingEdges.size() == 0 && node.outgoingEdges.size() == 0){
			outgoingSimilarity = 1.0;
		} else {
			boolean thisSmaller = outgoingEdges.size() < node.outgoingEdges.size();
			Collection<Edge> a;
			List<Edge> b;
			if(thisSmaller){ // find matches for all elements of smaller set
				a = outgoingEdges;
				temp.clear();
				temp.addAll(node.outgoingEdges);
				b = temp;
			} else {
				a = node.outgoingEdges;
				temp.clear();
				temp.addAll(outgoingEdges);
				b = temp;
			}
			
			int size = b.size();
			
			// match neighbors
			for(Edge edge1 : a){
				double maxSimilarity = Double.NEGATIVE_INFINITY;
//				Edge maxSimilarEdge = null;
				int maxSimilarEdgeIndex = -1;
        		for(int j=0; j<b.size(); j++){
        			Edge edge2 = b.get(j);
					Pair<Node,Node> key = thisSmaller ? new Pair<Node,Node>(edge1.targetNode,edge2.targetNode) : new Pair<Node,Node>(edge2.targetNode,edge1.targetNode);
					double similarity = similarities.get(key);
					if(similarity > maxSimilarity) {
						maxSimilarity = similarity;
//						maxSimilarEdge = edge2;
						maxSimilarEdgeIndex = j;
					}
				}
				outgoingSimilarity += maxSimilarity;
				b.remove(maxSimilarEdgeIndex);
			}
			outgoingSimilarity /= size;
		}
		
		return outgoingSimilarity;
	}
	
}
