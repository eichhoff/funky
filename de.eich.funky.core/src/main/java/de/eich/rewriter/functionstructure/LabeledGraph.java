/**

 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.functionstructure;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.javatuples.Pair;

import de.eich.utils.ComparableList;

public class LabeledGraph {

	public final Map<Integer, Node> nodes = new TreeMap<Integer, Node>();
	public final Set<Edge> edges = new TreeSet<Edge>();
	public final Map<Integer, Label> labels = new TreeMap<Integer, Label>();
	public final NodeIDGenerator nodeIDGenerator;
//	public final int[] degreeHistogram = new int[100];
//	public int maxDegree = 0;
	
	private final List<Node> temp1 = new ComparableList<Node>();
	private final List<Node> temp2 = new ComparableList<Node>();
	private final Set<Edge> temp3 = new TreeSet<Edge>();
	
	public LabeledGraph(NodeIDGenerator nodeIDGenerator){
		this.nodeIDGenerator = nodeIDGenerator;
	}
	
	public Node addNode(){
		return new Node(this);
	}
	
	public Edge addEdge(Node sourceNode, Node targetNode){
		return new Edge(this, sourceNode, targetNode);
	}
	
	public Label addLabel(IdentifiableLabelContent content){
		Label label;
//		try{ TODO : fix this error
		if((label = labels.get(content.getID())) != null) return label;
//		} catch (Exception e){
//			e.printStackTrace();
//			System.exit(0);
//		}
		return new Label(this, content);
	}
	
//	public void updateMaxDegree(){
//		maxDegree = 0;
//		for(Node node : nodes.values())
//			maxDegree = Math.max(maxDegree, node.getDegree());
//	}
	
	public String toString(){
//		return "\nNodes: " + nodes.values().toString() + "\nEdges: " + edges.toString();
		return "\nNodes: " + nodes.values().toString() + "\nEdges: " + edges.toString() + "\nLabels: " + labels.values().toString();
	}
	
	private Node copyNodeAndNeighborsToComponent(Node node, LabeledGraph component){
		Node newNode = new Node(component, node.id);
		for(Label label : node.labels){
			Label newLabel = component.labels.get(label.id);
			if(newLabel == null)
				newLabel = component.addLabel(label.content);
			newNode.addLabel(newLabel);
		}
		
		for(Edge incomingEdge : node.incomingEdges)
			if(!component.nodes.containsKey(incomingEdge.sourceNode.id))
				copyNodeAndNeighborsToComponent(incomingEdge.sourceNode, component);
		
		for(Edge outgoingEdge : node.outgoingEdges){
			Node newNeighbor = component.nodes.get(outgoingEdge.targetNode.id);
			if(newNeighbor == null)
				newNeighbor = copyNodeAndNeighborsToComponent(outgoingEdge.targetNode, component);
			
			Edge newEdge = component.addEdge(newNode, newNeighbor);
			for(Label label : outgoingEdge.labels){
				Label newLabel = component.labels.get(label.id);
				if(newLabel == null)
					newLabel = component.addLabel(label.content);
				newEdge.addLabel(newLabel);
			}
		}
		return newNode;
	}
	
	public Set<LabeledGraph> connectedComponents(){
		Set<LabeledGraph> components = new HashSet<LabeledGraph>();
		seedNodeLoop: for(Node seedNode : nodes.values()){
			for(LabeledGraph component : components)
				if(component.nodes.containsKey(seedNode.id)) continue seedNodeLoop;
			
			LabeledGraph newComponent = new LabeledGraph(nodeIDGenerator);
			components.add(newComponent);

			copyNodeAndNeighborsToComponent(seedNode, newComponent);
		}
		return components;
	}
	
	public boolean different(LabeledGraph graph){
		// numbers of nodes, edges and labels must be the same
		boolean notSameSize = nodes.size() != graph.nodes.size() ||
				edges.size() != graph.edges.size() ||
				labels.size() != graph.labels.size();
		if(notSameSize) return true;
			
//		if(maxDegree != graph.maxDegree) return true;
		
		// label histograms must be the same
		Iterator<Label> otherLabels = graph.labels.values().iterator();
		// in TreeMap labels are sorted with respect to their key (label id)! 
		for(Label labelA : labels.values()){
			Label labelB = otherLabels.next();
			if(labelA.id != labelB.id || labelA.inUse != labelB.inUse) return true;
		}
		
		// histograms of outgoing edges must be the same 
//		for(int i=0; i<degreeHistogram.length; i++) 
//			for(int i=0; i<=maxDegree; i++) 
//			if(degreeHistogram[i] != graph.degreeHistogram[i]) return true;
		
		return false;
	}
	
	public synchronized boolean isomorph(LabeledGraph graph){
		if(different(graph)) return false;
		
		LabeledGraphMapping mapping = new LabeledGraphMapping();
		temp1.clear();
		temp1.addAll(nodes.values());
		temp2.clear();
		temp2.addAll(graph.nodes.values());
		if(mapNodes(temp1, 0, temp1.size(), temp2, graph, mapping, edges, false, null)) return true;
		
		return false;
	}
	
	public synchronized LabeledGraphMapping isomorphMapping(LabeledGraph graph, boolean testDifferent){
		if(testDifferent && different(graph)) return null;
		
		LabeledGraphMapping mapping = new LabeledGraphMapping();
		temp1.clear();
		temp1.addAll(nodes.values());
		temp2.clear();
		temp2.addAll(graph.nodes.values());
		if(mapNodes(temp1, 0, temp1.size(), temp2, graph, mapping, edges, false, null)) return mapping;
		
		return null;
	}
	
	public synchronized LabeledGraphMapping subgraphIsomorphMapping(LabeledGraph graph, Set<Integer> nodesOfSubgraph, boolean considerBoundaryNodes, boolean considerBoundaryEdges, Set<LabeledGraphMapping> tabus){
		// used refers to this graph
		LabeledGraphMapping mapping = new LabeledGraphMapping();
		
		temp1.clear();
		for(Integer nodeID : nodesOfSubgraph){
			if(nodes.get(nodeID) == null){
				System.err.println(nodesOfSubgraph);
				System.err.println(this.nodes.keySet());
			}
			temp1.add(nodes.get(nodeID));
		}
		
		temp3.clear();
		if(considerBoundaryEdges){
			for(Node node : temp1){
				temp3.addAll(node.incomingEdges);
				temp3.addAll(node.outgoingEdges);
			}
		} else {
			for(Node node : temp1){
				for(Edge edge : node.incomingEdges)
					if(temp1.contains(edge.sourceNode))
						temp3.add(edge);
				for(Edge edge : node.outgoingEdges)
					if(temp1.contains(edge.targetNode))
						temp3.add(edge);
			}
		}

		if(considerBoundaryNodes){
			for(Edge edge : temp3){
				if(!temp1.contains(edge.sourceNode)){
					temp1.add(edge.sourceNode);
				}
				if(!temp1.contains(edge.targetNode)){
					temp1.add(edge.targetNode);
				}
			}
		}
		
		temp2.clear();
		temp2.addAll(graph.nodes.values());
		if(mapNodes(temp1, 0, temp1.size(), temp2, graph, mapping, temp3, true, tabus)) return mapping;
		
		return null;
	}
	
	public synchronized boolean validateEdgesOfIsomorphMapping(LabeledGraphMapping mapping, LabeledGraph graph, boolean subgraph){
		temp1.clear();
		temp1.addAll(nodes.values());
		temp2.clear();
		temp2.addAll(graph.nodes.values());
		
		if(subgraph){
			for(Node node : temp1){
				Node otherNode = graph.nodes.get(mapping.nodeIDMapping.get(node.id));
				if(!node.isomorph(otherNode)) return false;
			}
		} else {
			for(Node node : temp1){
				Node otherNode = graph.nodes.get(mapping.nodeIDMapping.get(node.id));
				if(node.outgoingEdges.size() != otherNode.outgoingEdges.size() ||
					node.incomingEdges.size() != otherNode.incomingEdges.size() ||
					node.isomorph(otherNode)) return false;
			}
		}
		
		return mapNodes(temp1, nodes.size(), nodes.size(), temp2, graph, mapping, edges, subgraph, null);
	}
	
	private boolean mapNodes(List<Node> nodes, int mapped, int total, List<Node> otherNodes, LabeledGraph graph, LabeledGraphMapping mapping, Set<Edge> edges, boolean subgraph, Set<LabeledGraphMapping> tabus){
		if(mapped == total){
			// step2: valid node mapping found, start testing edges
			
			if(tabus != null){
				for(LabeledGraphMapping tabu : tabus){
					if(mapping.nodeIDMapping.equals(tabu.nodeIDMapping)){
						return false;
					}
				}
			}
			
			if(edges.size() == 0) return true;
			
			// add edges with respect to mapping
			mapping.edgeMapping.clear();
			if(subgraph){
				for(Edge edge : edges){
					Node sourceNode = graph.nodes.get(mapping.nodeIDMapping.get(edge.sourceNode.id));
					Node targetNode = graph.nodes.get(mapping.nodeIDMapping.get(edge.targetNode.id));
					
					boolean matched = false;
					loop: for(Edge otherEdge: sourceNode.outgoingEdges){
						if(otherEdge.labels.size() != edge.labels.size()) continue;
						
						for(int j=0; j<otherEdge.labels.size(); j++)
							if(!otherEdge.labels.get(j).equals(edge.labels.get(j))) continue loop;
						
						if(otherEdge.targetNode.equals(targetNode)){
							matched = true;
							mapping.edgeMapping.put(edge, otherEdge);
							break;
						}
					}
					
					if(!matched) return false;
				}
			} else {
				for(Edge edge : edges){
					Node sourceNode = graph.nodes.get(mapping.nodeIDMapping.get(edge.sourceNode.id));
					if(sourceNode.outgoingEdges.size() != edge.sourceNode.outgoingEdges.size()) return false;
					Node targetNode = graph.nodes.get(mapping.nodeIDMapping.get(edge.targetNode.id));
					if(targetNode.incomingEdges.size() != edge.targetNode.incomingEdges.size()) return false;
					
					boolean matched = false;
					loop: for(Edge otherEdge: sourceNode.outgoingEdges){
						if(otherEdge.labels.size() != edge.labels.size()) continue;
						
						for(int j=0; j<otherEdge.labels.size(); j++)
							if(!otherEdge.labels.get(j).equals(edge.labels.get(j))) continue loop;
						
						if(otherEdge.targetNode.equals(targetNode)){
							matched = true;
							mapping.edgeMapping.put(edge, otherEdge);
							break;
						}
					}
					
					if(!matched) return false;
				}
			}
			
			return true;
		} else {
			if(subgraph){
				Node node = nodes.get(mapped);
				for(int j = 0; j < otherNodes.size(); j++){
					Node otherNode = otherNodes.get(j);
					if(node.isomorph(otherNode)){
						mapping.nodeIDMapping.put(node.id, otherNode.id);
						
						otherNodes.remove(j);
						if(mapNodes(nodes, mapped + 1, total, otherNodes, graph, mapping, edges, subgraph, tabus)) return true;
						otherNodes.add(j, otherNode);
					}
				}
			} else {
				Node node = nodes.get(mapped);
				loop: for(int j = 0; j < otherNodes.size(); j++){
					Node otherNode = otherNodes.get(j);
					if(node.outgoingEdges.size() == otherNode.outgoingEdges.size() &&
						node.incomingEdges.size() == otherNode.incomingEdges.size() &&
						node.isomorph(otherNode)){
						// try histograms for nodes
						for(int k = 0; k < node.labelHistogram.length; k++)
							if(node.labelHistogram[k] != otherNode.labelHistogram[k]) continue loop;
						
						mapping.nodeIDMapping.put(node.id, otherNode.id);
						
						otherNodes.remove(j);
						if(mapNodes(nodes, mapped + 1, total, otherNodes, graph, mapping, edges, subgraph, tabus)) return true;
						otherNodes.add(j, otherNode);
					}
				}
			}
			
			return false;
		}
	}
	
	public void join(LabeledGraph graph){
		for(Label label : graph.labels.values())
			if(!labels.containsKey(label.id))
				addLabel(label.content);
		for(Node node : graph.nodes.values()){
			if((nodes.get(node.id)) == null){
				Node newNode = new Node(this, node.id);
				for(Label label : node.labels)
					newNode.addLabel(labels.get(label.id));
			} 
		}
		for(Edge edge : graph.edges){
			boolean add = true;
			for(Edge existingEdge : edges)
				if(edge.equals(existingEdge)){
					add = false;
					break;
				}
			if(add){
				Node sourceNode = nodes.get(edge.sourceNode.id);
				Node targetNode = nodes.get(edge.targetNode.id);
				Edge newEdge = addEdge(sourceNode, targetNode);
				for(Label label : edge.labels)
					newEdge.addLabel(labels.get(label.id));
			}
		}
	}
	
	public LabeledGraph copy(boolean copyNodeIDGenerator){
		LabeledGraph copy;
		if(copyNodeIDGenerator)
			copy = new LabeledGraph(nodeIDGenerator.copy());
		else
			copy = new LabeledGraph(nodeIDGenerator);
		for(Label label : labels.values())
			copy.addLabel(label.content);
		for(Node node : nodes.values()){
			Node newNode = new Node(copy, node.id);
			for(Label label : node.labels)
				newNode.addLabel(copy.labels.get(label.id));
		}
		for(Edge edge : edges){
			Edge newEdge = copy.addEdge(copy.nodes.get(edge.sourceNode.id), copy.nodes.get(edge.targetNode.id));
			for(Label label : edge.labels)
				newEdge.addLabel(copy.labels.get(label.id));
		}
		return copy;
	}
	
	public LabeledGraph reinstantiate(NodeIDGenerator nodeIDGenerator, LabeledGraphMapping mapping){
		LabeledGraph copy = new LabeledGraph(nodeIDGenerator);
		for(Label label : labels.values())
			copy.addLabel(label.content);
		for(Node node : nodes.values()){
			Integer nodeID = mapping.nodeIDMapping.get(node.id);
			
			Node newNode;
			if(nodeID != null){
				newNode = new Node(copy, nodeID);
			} else {
				newNode = copy.addNode();
				mapping.nodeIDMapping.put(node.id, newNode.id);
			}
			
			for(Label label : node.labels)
				newNode.addLabel(copy.labels.get(label.id));
		}
		for(Edge edge : edges){
			Integer sourceNodeID = mapping.nodeIDMapping.get(edge.sourceNode.id);
			if(sourceNodeID == null)
				sourceNodeID = edge.sourceNode.id;
			
			Integer targetNodeID = mapping.nodeIDMapping.get(edge.targetNode.id);
			if(targetNodeID == null)
				targetNodeID = edge.targetNode.id;
			
			Edge newEdge = copy.addEdge(copy.nodes.get(sourceNodeID), copy.nodes.get(targetNodeID));
			for(Label label : edge.labels)
				newEdge.addLabel(copy.labels.get(label.id));
		}
		return copy;
	}
	
	private Map<Pair<Node,Node>, Double> similarities = new TreeMap<Pair<Node,Node>, Double>();
	private List<Node> temp = new ArrayList<Node>();
	public synchronized Pair<Double,LabeledGraphMapping> getGraphSimilarity(LabeledGraph graph) {
        double finalGraphSimilarity = 0.0;
        LabeledGraphMapping mapping = new LabeledGraphMapping();
        
        if(nodes.size() == 0 && graph.nodes.size() == 0){
        	finalGraphSimilarity = 1.0;
        } else {
        	double epsilon = 0.1;
        	
        	similarities.clear();
        	for(Node node1 : nodes.values())
        		for(Node node2 : graph.nodes.values())
        			node1.initSimilarity(node2, similarities);
        	
        	double maxDifference = 0.0;
        	boolean terminate = false;
        	int i = 0;
        	while (!terminate) {
        		maxDifference = 0.0;
        		i++;
        		for(Node node1 : nodes.values()){
        			for(Node node2 : graph.nodes.values()){
        				Pair<Node, Node> key = new Pair<Node,Node>(node1,node2);
        				double oldSimilarity = similarities.get(key);
        				
        				double edgeSimilarity = (node1.getIncomingSimilarity(node2, similarities) 
        						+ node1.getOutgoingSimilarity(node2, similarities)) / 2;
        				
//        				double nodeSimilarity = node1.getDirectNodeSimilarity(node2);
//        				
//        				double newSimilarity = (edgeSimilarity + nodeSimilarity)/2;
        				double newSimilarity = edgeSimilarity;
        				
        				double difference = Math.abs(oldSimilarity - newSimilarity);
        				if(difference > maxDifference) {
        					maxDifference = difference;
        				}
        				similarities.put(key,newSimilarity);
        			}
        		}
        		
        		if (maxDifference < epsilon || i > 50) {
        			terminate = true;
        		}
        	}
        	
        	boolean thisSmaller = nodes.size() < graph.nodes.size();
        	Collection<Node> a;
        	List<Node>b;
        	boolean keyOrderAB;
        	if(thisSmaller){ // find matches for all elements of smaller set
        		a = nodes.values();
        		temp.clear();
        		temp.addAll(graph.nodes.values());
        		b = temp;
        		keyOrderAB = true;
        	} else {
        		a = graph.nodes.values();
        		temp.clear();
        		temp.addAll(nodes.values());
        		b = temp;
        		keyOrderAB = false;
        	}
        	
        	int size = a.size();
        	
        	// match neighbors
        	for(Node node1 : a){
        		double maxSimilarity = -1.0;
        		Node maxSimilarNode = null;
        		int maxSimilarNodeIndex = -1;
        		for(int j=0; j<b.size(); j++){
        			Node node2 = b.get(j);
        			Pair<Node,Node> key = keyOrderAB ? new Pair<Node,Node>(node1,node2) : new Pair<Node,Node>(node2,node1);
        			double similarity = similarities.get(key);
        			if(similarity > maxSimilarity) {
        				maxSimilarity = similarity;
        				maxSimilarNode = node2;
        				maxSimilarNodeIndex = j;
        			}
        		}
        		if(nodeBias) finalGraphSimilarity += (10*node1.getDirectNodeSimilarity(maxSimilarNode) + maxSimilarity);
        		else finalGraphSimilarity += maxSimilarity;
        		if(thisSmaller)
        			mapping.nodeIDMapping.put(node1.id, maxSimilarNode.id);
        		else
        			mapping.nodeIDMapping.put(maxSimilarNode.id, node1.id);
        		b.remove(maxSimilarNodeIndex);
        	}
        	if(nodeBias) finalGraphSimilarity /= 11*size;
        	else finalGraphSimilarity /= size;
        }
        
        return new Pair<Double,LabeledGraphMapping>(finalGraphSimilarity, mapping);
    }
	
	private static boolean nodeBias = false; // test 1,2,3 false
	
	/*
	public Map<Pair<Node,Node>, Double> initializeSimilarityMatrices(LabeledGraph graph) {
		
		double epsilon = 0.01;
		
		// initialize
		
		Map<Pair<Node,Node>, Double> nodeSimilarity = new TreeMap<Pair<Node,Node>, Double>();
		double[] incomingSimilarity = new double[nodes.size() * graph.nodes.size()];
		double[] outgoingSimilarity = new double[nodes.size() * graph.nodes.size()];
		
		int i = 0;
		for(Node node1 : nodes.values()){
			for(Node node2 : graph.nodes.values()){
				double maxDegree = Math.max(node1.incomingEdges.size(), node2.incomingEdges.size());
				if(maxDegree != 0.0){
					double minDegree = Math.min(node1.incomingEdges.size(), node2.incomingEdges.size());
					incomingSimilarity[i] = minDegree / maxDegree;
				} else {
					incomingSimilarity[i] = 0.0;
				}
				
				maxDegree = Math.max(node1.outgoingEdges.size(), node2.outgoingEdges.size());
				if(maxDegree != 0.0){
					double minDegree = Math.min(node1.outgoingEdges.size(), node2.outgoingEdges.size());
					outgoingSimilarity[i] = minDegree / maxDegree;
				} else {
					outgoingSimilarity[i] = 0.0;
				}
				
				nodeSimilarity.put(new Pair<Node,Node>(node1, node2), (incomingSimilarity[i] + outgoingSimilarity[i]) / 2);

				i++;
			}
		}
		
		// do similarity estimation
		
        double maxDifference = 0.0;
        boolean terminate = false;

        while (!terminate) {
            maxDifference = 0.0;
            
            i = 0;
    		for(Node node1 : nodes.values()){
    			for(Node node2 : graph.nodes.values()){
    				double similaritySum = 0.0;
    				double maxDegree = Math.max(node1.incomingEdges.size(), node2.incomingEdges.size());
    				double minDegree = Math.min(node1.incomingEdges.size(), node2.incomingEdges.size());
    				if (minDegree == node1.incomingEdges.size()) {
                        similaritySum = enumerationFunction(node1.incomingEdges, node2.incomingEdges, 0, true, graph, nodeSimilarity);
                    } else {
                        similaritySum = enumerationFunction(node2.incomingEdges, node1.incomingEdges, 1, true, graph, nodeSimilarity);
                    }
                    if (maxDegree == 0.0 && similaritySum == 0.0) {
                    	incomingSimilarity[i] = 1.0;
                    } else if (maxDegree == 0.0) {
                    	incomingSimilarity[i] = 0.0;
                    } else {
                    	incomingSimilarity[i] = similaritySum / maxDegree;
                    }
                    
                    similaritySum = 0.0;
                    maxDegree = Math.max(node1.outgoingEdges.size(), node2.outgoingEdges.size());
    				minDegree = Math.min(node1.outgoingEdges.size(), node2.outgoingEdges.size());
                    if (minDegree == node1.incomingEdges.size()) {
                        similaritySum = enumerationFunction(node1.outgoingEdges, node2.outgoingEdges, 0, false, graph, nodeSimilarity);
                    } else {
                        similaritySum = enumerationFunction(node2.outgoingEdges, node1.outgoingEdges, 1, false, graph, nodeSimilarity);
                    }
                    if (maxDegree == 0.0 && similaritySum == 0.0) {
                        outgoingSimilarity[i] = 1.0;
                    } else if (maxDegree == 0.0) {
                    	outgoingSimilarity[i] = 0.0;
                    } else {
                    	outgoingSimilarity[i] = similaritySum / maxDegree;
                    }
    				
                    System.out.println(incomingSimilarity[i] + "  " + outgoingSimilarity[i]);
                    
    				i++;
    			}
    		}
            
    		i = 0;
    		for(Node node1 : nodes.values()){
    			for(Node node2 : graph.nodes.values()){
                    double temp = (incomingSimilarity[i] + outgoingSimilarity[i]) / 2;
                    double similarity = nodeSimilarity.get(new Pair<Node,Node>(node1,node2));
                    if (Math.abs(similarity - temp) > maxDifference) {
                        maxDifference = Math.abs(similarity - temp);
                    }
                    nodeSimilarity.put(new Pair<Node,Node>(node1,node2), temp);
                    i++;
                }
            }
    		
            if (maxDifference < epsilon) {
                terminate = true;
            }
        }
        
        return nodeSimilarity;
    }
	
	public double enumerationFunction(Collection<Node> neighborListMin, Collection<Node> neighborListMax, int x, LabeledGraph graph, Map<Pair<Node,Node>,Double> nodeSimilarity) {
        double similaritySum = 0.0;
        Map<Integer, Double> valueMap = new TreeMap<Integer, Double>();
        if (x == 0) {
        	for(Node neighbor : neighborListMin){
        		double max = 0.0;
                int maxIndex = -1;
                for(Node key : neighborListMax){
                    if (!valueMap.containsKey(key.id)) {
                    	Double sim = nodeSimilarity.get(new Pair<Node,Node>(neighbor,key));
                        if (max < sim) {
                            max = sim;
                            maxIndex = key.id;
                        }
                    }
                }
                valueMap.put(maxIndex, max);
        	}
        } else {
        	for(Node neighbor : neighborListMin){
        		double max = 0.0;
                int maxIndex = -1;
                for(Node key : neighborListMax){
                    if (!valueMap.containsKey(key.id)) {
                    	Double sim = nodeSimilarity.get(new Pair<Node,Node>(key,neighbor));
                        if (max < sim) {
                            max = sim;
                            maxIndex = key.id;
                        }
                    }
                }
                valueMap.put(maxIndex, max);
        	}
        }

        for (double value : valueMap.values()) {
            similaritySum += value;
        }
        return similaritySum;
    }
	
	public double enumerationFunction(Set<Edge> neighborListMin, Set<Edge> neighborListMax, int x, boolean incoming, LabeledGraph graph, Map<Pair<Node,Node>,Double> nodeSimilarity) {
        double similaritySum = 0.0;
        Map<Integer, Double> valueMap = new TreeMap<Integer, Double>();
        if (x == 0) {
        	for(Edge edge1 : neighborListMin){
        		Node neighbor = incoming ? edge1.sourceNode : edge1.targetNode;
        		double max = 0.0;
                int maxIndex = -1;
                for(Edge edge2 : neighborListMax){
            		Node key = incoming ? edge2.sourceNode : edge2.targetNode;
                    if (!valueMap.containsKey(key.id)) {
                    	Double sim = nodeSimilarity.get(new Pair<Node,Node>(neighbor,key));
                        if (max < sim) {
                            max = sim;
                            maxIndex = key.id;
                        }
                    }
                }
                valueMap.put(maxIndex, max);
        	}
        } else {
        	for(Edge edge1 : neighborListMin){
        		Node neighbor = incoming ? edge1.sourceNode : edge1.targetNode;
        		double max = 0.0;
                int maxIndex = -1;
                for(Edge edge2 : neighborListMax){
            		Node key = incoming ? edge2.sourceNode : edge2.targetNode;
                    if (!valueMap.containsKey(key.id)) {
                    	Double sim = nodeSimilarity.get(new Pair<Node,Node>(key,neighbor));
                        if (max < sim) {
                            max = sim;
                            maxIndex = key.id;
                        }
                    }
                }
                valueMap.put(maxIndex, max);
        	}
        }

        for (double value : valueMap.values()) {
            similaritySum += value;
        }
        return similaritySum;
    }
	*/
}
