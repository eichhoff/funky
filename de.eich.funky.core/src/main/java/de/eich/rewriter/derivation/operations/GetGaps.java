/**
2 * @author Julian Eichhoff
 *
 * Copyright 2013 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.javatuples.Pair;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.Edge;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Label;
import de.eich.rewriter.functionstructure.Node;

/**
 * 
 */
public class GetGaps extends AbstractParametrizedOperation {

	protected GetGaps(Rule rule, OperationType operation) {
		super(rule, operation);
	}

	private void initDirectDerivation(DirectDerivation directDerivation, Pair<Node, Node> gap){
		Node sourceNode = directDerivation.target.getFunction(gap.getValue0().id);
		Node targetNode = directDerivation.target.getFunction(gap.getValue1().id);
		
		if(FunctionStructure.isNonterminalFunction(sourceNode)){
			Node nonterminal = sourceNode;
			sourceNode = sourceNode.incomingEdges.iterator().next().sourceNode;
			
			directDerivation.left.add(nonterminal.id);
			directDerivation.left.add(sourceNode.id);
			directDerivation.gluing.add(sourceNode.id);
			directDerivation.right.add(sourceNode.id);

			directDerivation.target.removeFunction(nonterminal);
			
			rule.setNode(parameters.get(0), directDerivation, sourceNode.id);
		} else {
			directDerivation.left.add(sourceNode.id);
			directDerivation.gluing.add(sourceNode.id);
			directDerivation.right.add(sourceNode.id);
			rule.setNode(parameters.get(0), directDerivation, sourceNode.id);
		}
		
		if(FunctionStructure.isNonterminalFunction(targetNode)){
			Node nonterminal = targetNode;
			targetNode = targetNode.outgoingEdges.iterator().next().targetNode;
			
			directDerivation.left.add(nonterminal.id);
			directDerivation.left.add(targetNode.id);
			directDerivation.gluing.add(targetNode.id);
			directDerivation.right.add(targetNode.id);
			
			directDerivation.target.removeFunction(nonterminal);
			
			rule.setNode(parameters.get(1), directDerivation, targetNode.id);
			rule.setFlow(parameters.get(2), directDerivation, (FlowLabel) targetNode.labels.get(1).content);
		} else {
			directDerivation.left.add(targetNode.id);
			directDerivation.gluing.add(targetNode.id);
			directDerivation.right.add(targetNode.id);
			rule.setNode(parameters.get(1), directDerivation, targetNode.id);
			rule.setFlow(parameters.get(2), directDerivation, (FlowLabel) targetNode.labels.get(1).content);
		}
	}
	
	private void getDirectDerivationsForNodes(DirectDerivation prototypeDirectDerivation, Set<Pair<Node, Node>> gaps, Set<DirectDerivation> directDerivations, boolean addPrototype){
		int i = 0;
		DirectDerivation directDerivation;
		for(Pair<Node, Node> gap : gaps){
			if(++i == gaps.size()){
				directDerivation = prototypeDirectDerivation;
				if(addPrototype) directDerivations.add(directDerivation);
			} else {
				directDerivations.add(directDerivation = prototypeDirectDerivation.copy());
			}
			initDirectDerivation(directDerivation, gap);
			addToCache(directDerivation, gaps, gap);
		}
	}
	
	private Map<DirectDerivation, Set<Pair<Node, Node>>> nextGapsCache = new HashMap<DirectDerivation, Set<Pair<Node, Node>>>();
	
	private synchronized void addToCache(DirectDerivation directDerivation, Set<Pair<Node, Node>> gaps, Pair<Node, Node> usedGap){
		Set<Pair<Node, Node>> nextGaps = new TreeSet<Pair<Node, Node>>(gaps);
		for(Iterator<Pair<Node, Node>> iterator = nextGaps.iterator(); iterator.hasNext();){
			Pair<Node, Node> nextGap = iterator.next();
			if(nextGap.getValue0() == usedGap.getValue0() || nextGap.getValue1() == usedGap.getValue1())
				iterator.remove();
		}
		nextGapsCache.put(directDerivation, nextGaps);
	}
	
	public void filterAvoidOccurrences(Set<Pair<Node, Node>> nodes, DirectDerivation directDerivation, Set<Occurrence> avoidOccurrences){ // TODO untested
		if(avoidOccurrences == null) return;
		
		if(directDerivation == null){
			for (Iterator<Pair<Node, Node>> iterator = nodes.iterator(); iterator.hasNext();) {
				Pair<Node, Node> nodePair = iterator.next();
				Node node1 = nodePair.getValue0();
				Node node2 = nodePair.getValue1();
				int i = FunctionStructure.isNonterminalFunction(node1) ? 2 : 1;
				int size = FunctionStructure.isNonterminalFunction(node2) ? i + 2 : i + 1;
				for(Occurrence occurrence : avoidOccurrences)
					if(occurrence.size() == size && occurrence.get(0) == node1.id && occurrence.get(i) == node2.id)
						iterator.remove();
			}
		} else {
			for (Iterator<Pair<Node, Node>> iterator = nodes.iterator(); iterator.hasNext();) {
				Pair<Node, Node> nodePair = iterator.next();
				int added = 2;
				Node node1 = nodePair.getValue0();
				directDerivation.left.add(node1.id);
				if(FunctionStructure.isNonterminalFunction(node1)){
					directDerivation.left.add(node1.incomingEdges.iterator().next().sourceNode.id);
					added++;
				}
				Node node2 = nodePair.getValue1();
				directDerivation.left.add(node2.id);
				if(FunctionStructure.isNonterminalFunction(node2)){
					directDerivation.left.add(node2.outgoingEdges.iterator().next().targetNode.id);
					added++;
				}
				if(avoidOccurrences.contains(directDerivation.left))
					iterator.remove();
				for(int i = 0; i < added; i++)
					directDerivation.left.remove(directDerivation.left.size() -1);
			}
		}
	}
	
	private synchronized Set<Pair<Node, Node>> getGaps(FunctionStructure target, FlowLabel flow, DirectDerivation precedingDirectDerivationOfSameRule){
		Set<Pair<Node, Node>> gaps;
		if(precedingDirectDerivationOfSameRule != null){
			gaps = nextGapsCache.get(precedingDirectDerivationOfSameRule);
			if(gaps != null) return gaps;
		}
		
		gaps = new TreeSet<Pair<Node, Node>>();
		
//		Set<Node> nodes = new TreeSet<Node>();
//		Set<Node> nonterminalNodes = new TreeSet<Node>();
//		for(Node node : target.graph.nodes.values()) 
//			if(target.isNonterminalFunction(node))
//				nonterminalNodes.add(node);
//			else
//				nodes.add(node);
		
		Collection<Node> allNodes = target.graph.nodes.values();
		
		for(Node sourceNode : allNodes){
			
			Label sourceNodeLabel = null;
//			Node a = null;
			
			if(FunctionStructure.isNonterminalFunction(sourceNode)){
				if(sourceNode.incomingEdges.isEmpty()) continue;
				
				Edge edge = sourceNode.incomingEdges.iterator().next();
				sourceNodeLabel = edge.labels.get(0);
				
//				a = edge.sourceNode;
				
			} else {
				if(sourceNode.labels.get(0).content == FunctionLabel.EXPORT) continue;
				
				sourceNodeLabel = sourceNode.labels.get(2);
				
				if(!sourceNode.outgoingEdges.isEmpty()) {
					boolean outgoingEdgeWithLabel = false;
					for(Edge edge : sourceNode.outgoingEdges){
						if(edge.labels.contains(sourceNodeLabel)){
							outgoingEdgeWithLabel = true;
							break;
						}
					}
					if(outgoingEdgeWithLabel) continue;
				}
				
//				a = sourceNode;
			}
			
			for(Node targetNode : allNodes){
				if(targetNode == sourceNode) continue;
				
				Label targetNodeLabel = null;
//				Node b = null;
				
				if(FunctionStructure.isNonterminalFunction(targetNode)){
					if(targetNode.outgoingEdges.isEmpty()) continue;
					
					Edge edge = targetNode.outgoingEdges.iterator().next();
					targetNodeLabel = edge.labels.get(0);
					
					if(!targetNodeLabel.equals(sourceNodeLabel)) continue;
					
//					b = edge.targetNode;
					
				} else {
					if(targetNode.labels.get(0).content == FunctionLabel.IMPORT) continue;
					
					targetNodeLabel = targetNode.labels.get(1);
					
					if(!targetNodeLabel.equals(sourceNodeLabel)) continue;
					
					if(!targetNode.incomingEdges.isEmpty()) {
						boolean incomingEdgeWithLabel = false;
						for(Edge edge : targetNode.incomingEdges){
							if(edge.labels.contains(targetNodeLabel)){
								incomingEdgeWithLabel = true;
								break;
							}
						}
						if(incomingEdgeWithLabel) continue;
					}
					
//					b = targetNode;
				}
				
//				if(target.getFlow(a,b,(FlowLabel) sourceNodeLabel.content) != null) continue;
//				if(target.getFlow(b,a,(FlowLabel) sourceNodeLabel.content) != null) continue;
				
				gaps.add(new Pair<Node, Node>(sourceNode, targetNode));
			}

		}
		
//		System.out.println(gaps);
		
		return gaps;
	}
	
	public void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule) {
		if(source != null){
			FunctionStructure target = source.copy(false);
			FlowLabel flow = rule.getFlow(parameters.get(2), null);
			Set<Pair<Node, Node>> gaps = getGaps(target, flow, precedingDirectDerivationOfSameRule);
			filterAvoidOccurrences(gaps, null, avoidOccurrences);
			// TODO: sometimes nullpointer gaps is null! (multithreaded)
			if(!gaps.isEmpty()){
				DirectDerivation directDerivation = new DirectDerivation(rule, source, target); 
				getDirectDerivationsForNodes(directDerivation, gaps, directDerivations, true);
			}
		} else {
			Set<DirectDerivation> additionalDirectDerivations = new HashSet<DirectDerivation>();
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				FlowLabel flow = rule.getFlow(parameters.get(2), directDerivation);
				Set<Pair<Node, Node>> gaps = getGaps(directDerivation.target, flow, precedingDirectDerivationOfSameRule);
				filterAvoidOccurrences(gaps, directDerivation, avoidOccurrences);
				if(!gaps.isEmpty()){
					getDirectDerivationsForNodes(directDerivation, gaps, additionalDirectDerivations, false);
				} else {
					iterator.remove();
				}
			}
			directDerivations.addAll(additionalDirectDerivations);
		}
	}
	
	public void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule) { // TODO untested
		if(source != null){
			if(occurrence.size() < 2) return;
			
			int nodeID1 = occurrence.get(0);
			FunctionStructure target = source.copy(false);
			Node sourceNode = target.getFunction(nodeID1);
			if(sourceNode == null) return;
			boolean sourceNodeIsNonterminal = FunctionStructure.isNonterminalFunction(sourceNode);

			if(sourceNodeIsNonterminal && occurrence.size() < 3) return;
			
			int nodeID2 = occurrence.get(sourceNodeIsNonterminal ? 2 : 1);
			Node targetNode = target.getFunction(nodeID2);
			if(targetNode == null) return;
			boolean targetNodeIsNonterminal = FunctionStructure.isNonterminalFunction(targetNode);
			
			if(sourceNodeIsNonterminal && targetNodeIsNonterminal && occurrence.size() < 4) return;
			else if(targetNodeIsNonterminal && occurrence.size() < 3) return;
			
			Set<Pair<Node, Node>> gaps = new TreeSet<Pair<Node, Node>>();
			gaps.add(new Pair<Node,Node>(sourceNode, targetNode));
			DirectDerivation directDerivation = new DirectDerivation(rule, source, target); 
			directDerivation.occurrenceIndex = sourceNodeIsNonterminal && targetNodeIsNonterminal ? 4 : sourceNodeIsNonterminal || targetNodeIsNonterminal ? 3 : 2;
			getDirectDerivationsForNodes(directDerivation, gaps, directDerivations, true);
		} else {
			Set<DirectDerivation> additionalDirectDerivations = new HashSet<DirectDerivation>();
			for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
				DirectDerivation directDerivation = iterator.next();
				if(directDerivation.occurrenceIndex +1 >= occurrence.size()){ 
					iterator.remove();
					continue;
				}
				
				int nodeID1 = occurrence.get(directDerivation.occurrenceIndex++);
				Node sourceNode = directDerivation.target.getFunction(nodeID1);
				if(sourceNode == null){
					iterator.remove();
					continue;
				}
				if(FunctionStructure.isNonterminalFunction(sourceNode)){
					if(directDerivation.occurrenceIndex +1 >= occurrence.size()){ 
						iterator.remove();
						continue;
					}
					directDerivation.occurrenceIndex++;
				}
				
				int nodeID2 = occurrence.get(directDerivation.occurrenceIndex++);
				Node targetNode = directDerivation.target.getFunction(nodeID2);
				if(targetNode == null){
					iterator.remove();
					continue;
				}
				if(FunctionStructure.isNonterminalFunction(targetNode)){
					if(directDerivation.occurrenceIndex +1 >= occurrence.size()){ 
						iterator.remove();
						continue;
					}
					directDerivation.occurrenceIndex++;
				}
				
				Set<Pair<Node, Node>> gaps = new TreeSet<Pair<Node, Node>>();
				gaps.add(new Pair<Node,Node>(sourceNode, targetNode));
				getDirectDerivationsForNodes(directDerivation, gaps, additionalDirectDerivations, false);
			}
			directDerivations.addAll(additionalDirectDerivations);
		}
	}

	public synchronized void reset(){
		nextGapsCache.clear();
	};
	
}
