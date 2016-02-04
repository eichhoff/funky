/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.functionstructure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RSIterator;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.eich.rewriter.FunctionalBasis;
import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.utils.IOUtils;

public class FunctionStructure {

	public LabeledGraph graph;
	
	private Label reactionForceLabel, primaryFunctionLabel;
	
	public static boolean record = true;
	public Map<Integer, Integer> nodeAddedByRule = record ? new TreeMap<Integer, Integer>() : null;
	public Map<Edge, Integer> edgeAddedByRule = record ? new TreeMap<Edge, Integer>() : null;
	public Map<Integer, Integer> nodeUsedByRule = record ? new TreeMap<Integer, Integer>() : null;
	public Map<Edge, Integer> edgeUsedByRule = record ? new TreeMap<Edge, Integer>() : null;
	
	public static enum LabelGroupType{
		ATTRIBUTE_GROUP,
		FUNCTION_GROUP,
		FLOW_GROUP
	}
	
	public Map<String, Triplet<Set<Pair<Node,Integer>>,Set<Pair<Edge,Integer>>,LabelGroupType>> labelGroups;
	
	public FunctionStructure(){
		NodeIDGenerator nodeIDGenerator = new NodeIDGenerator();
		graph = new LabeledGraph(nodeIDGenerator);
		reactionForceLabel = new Label(graph, FunctionalBasis.AttributeLabel.REACTION);
		primaryFunctionLabel =  new Label(graph, FunctionalBasis.AttributeLabel.PRIMARY);
	}
	
	private FunctionStructure(LabeledGraph graph){
		this.graph = graph;
		reactionForceLabel = graph.labels.get(FunctionalBasis.AttributeLabel.REACTION.id);
		primaryFunctionLabel = graph.labels.get(FunctionalBasis.AttributeLabel.PRIMARY.id);
	}
	
	public Node addNonterminalFunction(){
		Node node = graph.addNode();
		return node;
	}
	
	public Node addFunction(FunctionLabel function, FlowLabel flow1, FlowLabel flow2){
		Label label1 = graph.addLabel(function);
		Label label2 = graph.addLabel(flow1);
		Label label3 = graph.addLabel(flow2);
		Node node = graph.addNode();
		node.addLabel(label1);
		node.addLabel(label2);
		node.addLabel(label3);
		return node;
	}
	
	public Edge addFlow(FlowLabel flow, Node node1, Node node2){
		Label label = graph.addLabel(flow);
		Edge edge = graph.addEdge(node1, node2);
		edge.addLabel(label);
		if(node1.graph != node2.graph) System.err.println(edge + " not same graph");
		return edge;
	}
	
	public void removeFunction(int nodeID){
		Node node = graph.nodes.get(nodeID);
		node.delete();
		for(Label label : node.labels) label.deleteIfUnused();
	}
	
	public void removeFunction(Node node){
		node.delete();
		for(Label label : node.labels) label.deleteIfUnused();
	}
	
	public void removeFlow(Edge edge){
		edge.delete();
		for(Label label : edge.labels) label.deleteIfUnused();
	}
	
	public void removeOutgoingFlows(Node node){
		for(Edge edge : node.outgoingEdges) removeFlow(edge);
	}
	
	public void removeIncomingFlows(Node node){
		for(Edge edge : node.incomingEdges) removeFlow(edge);
	}
	
	public void setReactionForce(Node node){
		node.addLabel(reactionForceLabel);
	}
	
	public void setPrimaryFunction(Node node){
		node.addLabel(primaryFunctionLabel);
	}
	
	public Set<Node> getFunctions(){
		return new TreeSet<Node>(graph.nodes.values());
	}
	
	public Node getFunction(int nodeID){
		return graph.nodes.get(nodeID);
	}
	
	public Set<Node> getNonterminalFunctions(){
		Set<Node> nodes = new TreeSet<Node>();
		for(Node node : graph.nodes.values()) 
			if(isNonterminalFunction(node)) nodes.add(node);
		return nodes;
	}
	
	public Set<Node> getFunctions(FunctionLabel function, FlowLabel flow1, FlowLabel flow2){
		Set<Node> nodes = new TreeSet<Node>();
		Label label1 = null, label2 = null, label3 = null;
		Label anyFunction = graph.labels.get(FunctionLabel.ANY_FUNCTION.id); 
		Label anyFlow = graph.labels.get(FlowLabel.ANY_FLOW.id);
		
		// cancel search if labels do not exist in graph
		if(function != null && (label1 = graph.labels.get(function.id)) == null) return nodes;
		if(flow1 != null && (label2 = graph.labels.get(flow1.id)) == null) return nodes;
		if(flow2 != null && (label3 = graph.labels.get(flow2.id)) == null) return nodes;
		
		if(function != null){
			if(flow1 != null){
				if(flow2 != null){ // all labels specified
					for(Node node : label1.nodes) // iterate through all nodes with the given function label
						if(node.labels.size() >= 3 
							&& (node.labels.get(1) == label2 || node.labels.get(1).content == FlowLabel.ANY_FLOW)
							&& (node.labels.get(2) == label3 || node.labels.get(2).content == FlowLabel.ANY_FLOW)) nodes.add(node);
					if(anyFunction != null)
						for(Node node : anyFunction.nodes) // iterate through all nodes that have an any-function label
							if(node.labels.size() >= 3 
								&& (node.labels.get(1) == label2 || node.labels.get(1).content == FlowLabel.ANY_FLOW)
								&& (node.labels.get(2) == label3 || node.labels.get(2).content == FlowLabel.ANY_FLOW)) nodes.add(node);
				} else { // function and flow1 specified
					for(Node node : label1.nodes) // iterate through all nodes with the given function label
						if(node.labels.size() >= 3 && (node.labels.get(1) == label2 || node.labels.get(1).content == FlowLabel.ANY_FLOW)) nodes.add(node);
					if(anyFunction != null)
						for(Node node : anyFunction.nodes) // iterate through all nodes that have an any-function label
							if(node.labels.size() >= 3 && (node.labels.get(1) == label2 || node.labels.get(1).content == FlowLabel.ANY_FLOW)) nodes.add(node);
				}
			} else {
				if(flow2 != null){ // function and flow2 specified
					for(Node node : label1.nodes) // iterate through all nodes with the given function label
						if(node.labels.size() >= 3 && (node.labels.get(2) == label3 || node.labels.get(2).content == FlowLabel.ANY_FLOW)) nodes.add(node);
					if(anyFunction != null)
						for(Node node : anyFunction.nodes) // iterate through all nodes that have an any-function label
							if(node.labels.size() >= 3 && (node.labels.get(2) == label3 || node.labels.get(2).content == FlowLabel.ANY_FLOW)) nodes.add(node);
				} else { // only function specified
					for(Node node : label1.nodes) // iterate through all nodes with the given function label
						if(node.labels.size() >= 3) nodes.add(node);
					if(anyFunction != null)
						for(Node node : anyFunction.nodes) // iterate through all nodes that have an any-function label
							if(node.labels.size() >= 3) nodes.add(node);
				}
			}
		} else if(flow1 != null){
			if(flow2 != null){ // flow1 and flow2 specified
				for(Node node : label2.nodes) 
					if(node.labels.size() >= 3 
						&& (node.labels.get(1) == label2 || node.labels.get(1).content == FlowLabel.ANY_FLOW)
						&& (node.labels.get(2) == label3 || node.labels.get(2).content == FlowLabel.ANY_FLOW)) nodes.add(node);
				if(anyFlow != null)
					for(Node node : anyFlow.nodes)
						if(node.labels.size() >= 3 
							&& (node.labels.get(1) == label2 || node.labels.get(1).content == FlowLabel.ANY_FLOW)
							&& (node.labels.get(2) == label3 || node.labels.get(2).content == FlowLabel.ANY_FLOW)) nodes.add(node);
			} else { // only flow1 specified
				for(Node node : label2.nodes) 
					if(node.labels.size() >= 3 && node.labels.get(1) == label2) nodes.add(node);
				if(anyFlow != null)
					for(Node node : anyFlow.nodes)
						if(node.labels.size() >= 3 && node.labels.get(1).content == FlowLabel.ANY_FLOW) nodes.add(node);
			}
		} else if(flow2 != null){ // only flow2 specified
			for(Node node : label3.nodes) 
				if(node.labels.size() >= 3 && node.labels.get(2) == label3) nodes.add(node);
			if(anyFlow != null)
				for(Node node : anyFlow.nodes)
					if(node.labels.size() >= 3 && node.labels.get(2).content == FlowLabel.ANY_FLOW) nodes.add(node);
		} else { // nothing specified
			for(Node node : graph.nodes.values()) 
				if(!isNonterminalFunction(node)) nodes.add(node);
		}
		
		return nodes;
	}
	
	public Set<Edge> getFlows(){
		return new TreeSet<Edge>(graph.edges);
	}
	
	public Edge getFlow(Node sourceNode, Node targetNode, FlowLabel flow){
		Label label = graph.labels.get(flow.id);
		for(Edge edge: sourceNode.outgoingEdges){
			if(edge.targetNode == targetNode && edge.labels.contains(label)){
				return edge;
			}
		}
		return null;
	}
	
	public Edge getFlow(Node sourceNode, Node targetNode){
		for(Edge edge: sourceNode.outgoingEdges){
			if(edge.targetNode == targetNode){
				return edge;
			}
		}
		return null;
	}
	
	public static boolean isNonterminalFunction(Node node){
		if(node.labels.isEmpty() || !(node.labels.get(0).content instanceof FunctionLabel)) return true;
		return false;
	}
	
	public boolean withIncomingFlow(FlowLabel flow, Node node){
		return !withoutIncomingFlow(flow, node);
	}
	
	public boolean withOutgoingFlow(FlowLabel flow, Node node){
		return !withoutOutgoingFlow(flow, node);
	}
	
	public boolean withoutIncomingFlow(FlowLabel flow, Node node){
		Label label = graph.labels.get(flow.id);
		for(Edge edge : node.incomingEdges){
			if(edge.labels.contains(label)) return false;
		}
		return true;
	}
	
	public boolean withoutOutgoingFlow(FlowLabel flow, Node node){
		Label label = graph.labels.get(flow.id);
		for(Edge edge : node.outgoingEdges)
			if(edge.labels.contains(label)) return false;
		return true;
	}
	
	public Set<Edge> getIncomingNonterminalFlows(FlowLabel flow) {
		Set<Edge> incomingNonterminalFlows = new TreeSet<Edge>();
		Set<Node> nonterminalFunctions = getNonterminalFunctions();
		if(flow == null){
			for (Iterator<Node> iterator = nonterminalFunctions.iterator(); iterator.hasNext();) {
				Node node = iterator.next();
				incomingNonterminalFlows.addAll(node.outgoingEdges);
			}
		} else {
			Label label = graph.labels.get(flow.id);
			for (Iterator<Node> iterator = nonterminalFunctions.iterator(); iterator.hasNext();) {
				Node node = iterator.next();
				for(Edge edge : node.outgoingEdges)
					if(edge.labels.contains(label)) incomingNonterminalFlows.add(edge);
			}
		}
		return incomingNonterminalFlows;
	}
	
	public Set<Edge> getOutgoingNonterminalFlows(FlowLabel flow) {
		Set<Edge> outgoingNonterminalFlows = new TreeSet<Edge>();
		Set<Node> nonterminalFunctions = getNonterminalFunctions();
		if(flow == null){
			for (Iterator<Node> iterator = nonterminalFunctions.iterator(); iterator.hasNext();) {
				Node node = iterator.next();
				outgoingNonterminalFlows.addAll(node.incomingEdges);
			}
		} else {
			Label label = graph.labels.get(flow.id);
			for (Iterator<Node> iterator = nonterminalFunctions.iterator(); iterator.hasNext();) {
				Node node = iterator.next();
				for(Edge edge : node.incomingEdges)
					if(edge.labels.contains(label)) outgoingNonterminalFlows.add(edge);
			}
		}
		return outgoingNonterminalFlows;
	}
	
	public boolean hasLabel(Node node, IdentifiableLabelContent content){
		Label label = graph.labels.get(content.getID());
		return label != null && node.labels.contains(label);
	}
	
	public void addLabel(Node node, IdentifiableLabelContent content){
		Label label = graph.labels.get(content.getID());
		node.addLabel(label);
	}
	
	public void addLabel(Node node, Label label){
		node.addLabel(label);
	}
	
	public void setSameLabels(int nodeID, Node node2){
		// node 1 is going to have the same labels as node 2
		Node node1 = graph.nodes.get(nodeID); 

		// remove labels not present in node 2
		for(int i=0; i<node1.labels.size(); i++){
			Label label = node1.labels.get(i);
			if(!node2.labels.contains(label)){
				node1.removeLabel(label);
				i--;
			}
			label.deleteIfUnused();
		}
		
		// add labels not present in node 1
		for(int i=0; i<node2.labels.size(); i++){
			Label label = node2.labels.get(i);
			if(!node1.labels.contains(label)){
				Label existingLabel;
				if((existingLabel = node1.graph.labels.get(label.id)) == null){
					existingLabel = node1.graph.addLabel(label.content);
				}
				node1.addLabel(existingLabel);
			}
		}
	}
	
	public boolean isValid(){
		for(Node node : graph.nodes.values()){
			if(isNonterminalFunction(node)) return false;
			FunctionLabel function = (FunctionLabel) node.labels.get(0).content;
			switch(function){
			case IMPORT:
				if(node.outgoingEdges.isEmpty()) return false;
				break;
			case EXPORT:
				if(node.incomingEdges.isEmpty()) return false;
				break;
			default:
				if(node.incomingEdges.isEmpty() || node.outgoingEdges.isEmpty()) return false; // TODO: may be not required for some functions
			}
		}
		return true;
	}
	
	public double getDistanceToValidity(){
		double invalid = 0.0;
		double maxInvalid = graph.nodes.size();
		for(Node node : graph.nodes.values()){
			if(isNonterminalFunction(node)){
				invalid++;
				continue;
			}
			FunctionLabel function = (FunctionLabel) node.labels.get(0).content;
			switch(function){
			case IMPORT:
				if(node.outgoingEdges.isEmpty()) invalid++;
				break;
			case EXPORT:
				if(node.incomingEdges.isEmpty()) invalid++;
				break;
			default:
				if(node.incomingEdges.isEmpty() || node.outgoingEdges.isEmpty()) invalid++; // TODO: may be not required for some functions
			}
		}
		return invalid / maxInvalid; // ranges form 0 (completely valid) to 1 (completely invalid)
	}
	
	public boolean isomorph(FunctionStructure functionStructure){
		return graph.isomorph(functionStructure.graph);
	}
	
	public LabeledGraphMapping isomorphMapping(FunctionStructure functionStructure, boolean testDifferent){
		return graph.isomorphMapping(functionStructure.graph, testDifferent);
	}
	
	public LabeledGraphMapping subgraphIsomorphMapping(FunctionStructure functionStructure, Set<Integer> nodesOfSubgraph, boolean considerBoundaryNodes, boolean considerBoundaryEdges, Set<LabeledGraphMapping> tabus){
		// nodesOfSubgraph refers to this function structure
		return graph.subgraphIsomorphMapping(functionStructure.graph, nodesOfSubgraph, considerBoundaryNodes, considerBoundaryEdges, tabus);
	}
	
	public FunctionStructure join(FunctionStructure functionStructure, Set<Integer> deleteNodeIDs){
		graph.join(functionStructure.graph);
		for(int nodeID : deleteNodeIDs){
			Node node = graph.nodes.get(nodeID);
			if(node != null) node.delete();
		}
		return this;
	}
	
	public FunctionStructure join(FunctionStructure functionStructure){
		graph.join(functionStructure.graph);
		return this;
	}
	
	public boolean different(FunctionStructure functionStructure){
		return graph.different(functionStructure.graph);
	}
	
	public String toString(){
		return graph.toString();
	}
	
	public FunctionStructure copy(boolean copyNodeIDGenerator){
		FunctionStructure copy = new FunctionStructure(graph.copy(copyNodeIDGenerator));
		if(record){
			copy.nodeAddedByRule.putAll(nodeAddedByRule);
			copy.edgeAddedByRule.putAll(edgeAddedByRule);
			copy.nodeUsedByRule.putAll(nodeUsedByRule);
			copy.edgeUsedByRule.putAll(edgeUsedByRule);
		}
		return copy;
	}
	
	public FunctionStructure reinstantiate(NodeIDGenerator nodeIDGenerator, LabeledGraphMapping mapping){
		FunctionStructure copy = new FunctionStructure(graph.reinstantiate(nodeIDGenerator, mapping));
		return copy;
	}
	
	/*************************** RDFS LOADING ***************************/
	
	public void readFromRDFS(String pathToRDFS){
		Model model = IOUtils.readRDFS(pathToRDFS);
		
		graph = new LabeledGraph(graph.nodeIDGenerator);
		reactionForceLabel = new Label(graph, FunctionalBasis.AttributeLabel.REACTION);
		primaryFunctionLabel = new Label(graph, FunctionalBasis.AttributeLabel.PRIMARY);
		
		String ns = "http://www.iris.uni-stuttgart.de/eichhojn/FUNKY#";
		
		Resource functionLabel = model.getResource(ns + "FunctionLabel");
		Resource flowLabel = model.getResource(ns + "FlowLabel");
		
		Resource function = model.getResource(ns + "Function");
		Property flow = model.getProperty(ns + "Flow");
		
		for(StmtIterator i = model.listStatements(null, RDF.type, functionLabel); i.hasNext();) {
            Statement stmt = i.nextStatement();
            Resource subj = stmt.getSubject();
            graph.addLabel(FunctionLabel.valueOf(subj.getLocalName()));
        }
		
		for(StmtIterator i = model.listStatements(null, RDF.type, flowLabel); i.hasNext();) {
			Statement stmt = i.nextStatement();
			Resource subj = stmt.getSubject();
			graph.addLabel(FlowLabel.valueOf(subj.getLocalName()));
		}
		
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		for(StmtIterator i = model.listStatements(null, RDF.type, function); i.hasNext();) {
			Statement stmt = i.nextStatement();
			Resource subj = stmt.getSubject();
			
			Node node = graph.addNode();
			nodeMap.put(subj.getLocalName(), node);
			
			Resource obj = null;
			for(StmtIterator j = model.listStatements(subj, RDFS.label, obj); j.hasNext();) {
				Statement stmt2 = j.nextStatement();
				Seq seq = stmt2.getSeq();
				for(NodeIterator k = seq.iterator(); k.hasNext();) {
					Resource seqResource = (Resource) k.next();
					if(model.listStatements(seqResource, RDF.type, functionLabel).hasNext()){
						node.addLabel(graph.labels.get(FunctionalBasis.FunctionLabel.valueOf(seqResource.getLocalName()).id));
					} else if(model.listStatements(seqResource, RDF.type, flowLabel).hasNext()){
						node.addLabel(graph.labels.get(FunctionalBasis.FlowLabel.valueOf(seqResource.getLocalName()).id));
					} else {
						node.addLabel(graph.labels.get(FunctionalBasis.AttributeLabel.valueOf(seqResource.getLocalName()).id));
					}
				}
			}	
		}
		
		for(RSIterator i = model.listReifiedStatements(); i.hasNext();) {
			ReifiedStatement rstmt = i.nextRS();
			Statement stmt = rstmt.getStatement();
			
			if(stmt.getPredicate().equals(flow)){
				Node sourceNode = nodeMap.get(stmt.getSubject().getLocalName());
				Node targetNode = nodeMap.get(((Resource) stmt.getObject()).getLocalName());
				Edge edge = graph.addEdge(sourceNode, targetNode);
				
				Resource obj = null;
				for(StmtIterator j = model.listStatements(rstmt, RDFS.label, obj); j.hasNext();) {
					Statement stmt2 = j.nextStatement();
					Seq seq = stmt2.getSeq();
					for(NodeIterator k = seq.iterator(); k.hasNext();) {
						Resource seqResource = (Resource) k.next();
						if(model.listStatements(seqResource, RDF.type, flowLabel).hasNext()){
							edge.addLabel(graph.labels.get(FunctionalBasis.FlowLabel.valueOf(seqResource.getLocalName()).id));
						} else {
							edge.addLabel(graph.labels.get(FunctionalBasis.AttributeLabel.valueOf(seqResource.getLocalName()).id));
						}
					}
				}
			}
			
		}
	}
	
	public void writeToRDFS(String pathToRDFS){
		Model model = ModelFactory.createDefaultModel();
		
		String ns = "http://www.iris.uni-stuttgart.de/eichhojn/FUNKY#";
		model.setNsPrefix("FUNKY", ns);
		
		Resource attributeLabel = model.createResource(ns + "AttributeLabel");
		Resource functionLabel = model.createResource(ns + "FunctionLabel");
		Resource flowLabel = model.createResource(ns + "FlowLabel");
		
		Resource function = model.createResource(ns + "Function");
		Property flow = model.createProperty(ns + "Flow");
		
		for(Label label : graph.labels.values()){
			Resource subj = model.createResource(ns + label.toString());
			
			if(label.content instanceof FunctionalBasis.AttributeLabel)
				model.add(subj, RDF.type, attributeLabel);
			else if(label.content instanceof FunctionalBasis.FunctionLabel)
				model.add(subj, RDF.type, functionLabel);
			else if(label.content instanceof FunctionalBasis.FlowLabel)
				model.add(subj, RDF.type, flowLabel);
		}
		
		for(Node node : graph.nodes.values()){
			Resource subj = model.createResource(ns + "Function" + node.id);
			
			model.add(subj, RDF.type, function);
			
			Seq seq = model.createSeq();
			for(Label label : node.labels)
				seq.add(model.getResource(ns + label.toString()));
			model.add(subj, RDFS.label, seq);
		}
		
		for(Edge edge : graph.edges){
			Resource subj = model.getResource(ns + "Function" + edge.sourceNode.id);
			Resource obj = model.getResource(ns + "Function" + edge.targetNode.id);
			ReifiedStatement rstmt = model.createReifiedStatement(model.createStatement(subj, flow, obj));
			
			Seq seq = model.createSeq();
			for(Label label : edge.labels)
				seq.add(model.getResource(ns + label.toString()));
			model.add(rstmt, RDFS.label, seq);
		}
		
		IOUtils.writeRDFS(pathToRDFS, model);
	}
	
	public void addToLabelGroup(String groupID, Node node, int labelPosition, LabelGroupType type){
		Triplet<Set<Pair<Node,Integer>>,Set<Pair<Edge,Integer>>,LabelGroupType> labelGroup = labelGroups.get(groupID);
		if(labelGroup == null) labelGroups.put(groupID, 
				labelGroup = new Triplet<Set<Pair<Node,Integer>>,Set<Pair<Edge,Integer>>, LabelGroupType>(
						new TreeSet<Pair<Node,Integer>>(),
						new TreeSet<Pair<Edge,Integer>>(), type)
						);
		labelGroup.getValue0().add(new Pair<Node,Integer>(node, labelPosition));
	}
	
	public void addToLabelGroup(String groupID, Edge edge, int labelPosition, LabelGroupType type){
		Triplet<Set<Pair<Node,Integer>>,Set<Pair<Edge,Integer>>,LabelGroupType> labelGroup = labelGroups.get(groupID);
		if(labelGroup == null) labelGroups.put(groupID, 
				labelGroup = new Triplet<Set<Pair<Node,Integer>>,Set<Pair<Edge,Integer>>, LabelGroupType>(
						new TreeSet<Pair<Node,Integer>>(),
						new TreeSet<Pair<Edge,Integer>>(), type)
				);
		labelGroup.getValue1().add(new Pair<Edge,Integer>(edge, labelPosition));
	}
	
	public void removeSingularLabelGroups(){
		for(Iterator<Entry<String, Triplet<Set<Pair<Node,Integer>>,Set<Pair<Edge,Integer>>,LabelGroupType>>> iterator = labelGroups.entrySet().iterator(); iterator.hasNext();){
			Entry<String, Triplet<Set<Pair<Node,Integer>>,Set<Pair<Edge,Integer>>,LabelGroupType>> entry = iterator.next();
			if(entry.getValue().getValue0().size() + entry.getValue().getValue1().size() == 1) iterator.remove();
		}
	}
	
//	public void removeFromLabelGroup(String groupID, Node node, int labelPosition){
//		Set<Pair<Node, Integer>> labelGroup = labelGroups.get(groupID);
//		if(labelGroup == null) return;
//		labelGroup.remove(new Pair<Node,Integer>(node, labelPosition));
//		if(labelGroup.isEmpty()) labelGroups.remove(groupID);
//	}
	
}
