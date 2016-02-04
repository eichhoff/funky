/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.javatuples.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.eich.rewriter.AbstractRewriteSystem;
import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.derivation.operations.AbstractParametrizedOperation;
import de.eich.rewriter.derivation.operations.AbstractParametrizedOperation.OperationType;
import de.eich.rewriter.derivation.operations.AbstractParametrizedOperation.ParameterType;
import de.eich.rewriter.functionstructure.Edge;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Node;

public class Rule implements Comparable<Rule> {
	
	public static Integer closeGapsRuleID = null;
	
	public final int id;
	
	public final List<AbstractParametrizedOperation> left = new ArrayList<AbstractParametrizedOperation>(); 
	public final List<AbstractParametrizedOperation> right = new ArrayList<AbstractParametrizedOperation>(); 
	
	public ExternalHandlerInterface externalHandler;
	
	public Rule(int id){
		this.id = id;
	}
	
	public void reset(){
		for(AbstractParametrizedOperation operation : left)
			operation.reset();
		for(AbstractParametrizedOperation operation : right)
			operation.reset();
	}
	
	public Set<DirectDerivation> derive(FunctionStructure source, DirectDerivation precedingDirectDerivationOfSameRule){
		Set<DirectDerivation> directDerivations = new HashSet<DirectDerivation>();
		applyLeft(source, directDerivations, precedingDirectDerivationOfSameRule);
		if(!directDerivations.isEmpty()){
			applyRight(source, directDerivations);
		}
		return directDerivations;
	}
	/*
	public Set<DirectDerivation> deriveAdditionals2(FunctionStructure source, Set<Occurrence> excluded){
		Set<DirectDerivation> directDerivations = new HashSet<DirectDerivation>();
		applyLeft(source, directDerivations);
		for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
			DirectDerivation directDerivation1 = iterator.next();
			for(Occurrence occurrence : excluded){
				if(directDerivation1.left.equals(occurrence)){
					iterator.remove();
					break;
				}
			}
		}
		if(!directDerivations.isEmpty()){
			applyRight(source, directDerivations);
		}
		return directDerivations;
	}*/
	
	public Pair<Set<Occurrence>, Set<DirectDerivation>> deriveAdditionals(FunctionStructure source, Set<Occurrence> existing, Set<Occurrence> uncriticalOccurrences){
		Set<DirectDerivation> directDerivations = new HashSet<DirectDerivation>();
		Set<Occurrence> matched = new TreeSet<Occurrence>(uncriticalOccurrences);
		applyLeft(source, directDerivations, uncriticalOccurrences, null);
		for (Iterator<DirectDerivation> iterator = directDerivations.iterator(); iterator.hasNext();) {
			DirectDerivation directDerivation1 = iterator.next();
			for(Occurrence occurrence : existing){
				if(directDerivation1.left.equals(occurrence)){
					matched.add(occurrence);
					iterator.remove();
					break;
				}
			}
		}
		
		if(!directDerivations.isEmpty()){
			applyRight(source, directDerivations);
		}
		return new Pair<Set<Occurrence>, Set<DirectDerivation>>(matched, directDerivations);
	}
	
	public DirectDerivation apply(FunctionStructure source, DirectDerivation directDerivation){
		DirectDerivation copy = directDerivation.copy();
		copy.source = source;
		copy.target = source.copy(false);
		
		// adapt left hand side nodes
		for(int nodeID : directDerivation.left){
			Node node2 = directDerivation.target.getFunction(nodeID);
			if(node2 != null){
				copy.target.setSameLabels(nodeID, node2);
			}
		}
		
		// remove all functions except those of left and right hand side
		FunctionStructure cropped = directDerivation.target.copy(false);
		for(Node function : cropped.getFunctions()){
			if(!directDerivation.right.contains(function.id)){
				cropped.removeFunction(function);
			}
		}
		
		// delete all flows not added by direct derivation
		for(Edge flow : cropped.getFlows()){
			if(!directDerivation.addedFlows.contains(flow)){
				cropped.removeFlow(flow);
			}
		}
		
		// join
		copy.target.join(cropped, new TreeSet<Integer>(directDerivation.getRemoved()));
		
		return copy;
	}
	
	public Set<DirectDerivation> apply(FunctionStructure source, Set<DirectDerivation> directDerivations){
		Set<DirectDerivation> directDerivationsWorkingCopy = new HashSet<DirectDerivation>();
		for(DirectDerivation directDerivation : directDerivations){
			directDerivationsWorkingCopy.add(apply(source, directDerivation));
		}
		return directDerivationsWorkingCopy;
	}
	
	private void applyLeft(FunctionStructure source, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule){
		applyLeft(source, directDerivations, null, precedingDirectDerivationOfSameRule);
	}
	
	private void applyLeft(FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule){
		Set<DirectDerivation> directDerivationsWorkingCopy = new HashSet<DirectDerivation>();
		Set<DirectDerivation> directDerivationsOption = null;
		Set<DirectDerivation> directDerivationsChoice = null;
		boolean openChoiceBlock = true;
		boolean openOptionBlock = true;
		boolean isFirstOperation = true;
		boolean isFirstChoiceBlock = true;
		int optionCount = 0;
		for(AbstractParametrizedOperation parametrizedOperation : left){
			if(parametrizedOperation.operation == OperationType.CHOICE){
				if(openChoiceBlock){
					optionCount = 0;
					// reference original direct derivations up to this point
					directDerivationsOption = directDerivationsWorkingCopy;
					// prepare container for derivations from all options
					directDerivationsChoice = new HashSet<DirectDerivation>();
				} else {
					directDerivationsWorkingCopy = directDerivationsChoice;
					isFirstChoiceBlock = false;
				}
				openChoiceBlock = !openChoiceBlock;
			} else if(parametrizedOperation.operation == OperationType.OPTION){
				if(openOptionBlock){
					if(isFirstChoiceBlock) isFirstOperation = true;
					// make copies of original direct derivations
					// they will be subject to the operations of this block
					directDerivationsWorkingCopy = new HashSet<DirectDerivation>();
					for(DirectDerivation directDerivation : directDerivationsOption){
						directDerivationsWorkingCopy.add(directDerivation.copy());
					}
				} else {
					// add found derivations for this block when all operations are done
					for(DirectDerivation directDerivation : directDerivationsWorkingCopy){
						directDerivation.option.add(optionCount);
					}
					directDerivationsChoice.addAll(directDerivationsWorkingCopy);
					optionCount++;
				}
				openOptionBlock = !openOptionBlock;
			} else {
				if(isFirstOperation){
					parametrizedOperation.invoke(this, source, directDerivationsWorkingCopy, avoidOccurrences, precedingDirectDerivationOfSameRule);
					if(id != closeGapsRuleID) 
						AbstractRewriteSystem.directDerivationCounter++;
				} else {
					if(!directDerivationsWorkingCopy.isEmpty()){
						parametrizedOperation.invoke(this, null, directDerivationsWorkingCopy, avoidOccurrences, precedingDirectDerivationOfSameRule);
						if(id != closeGapsRuleID) 
							AbstractRewriteSystem.directDerivationCounter++;
					}
				}
				isFirstOperation = false;
			}
		}
		directDerivations.addAll(directDerivationsWorkingCopy);
		
//		if(this.id == 16){
//			for(DirectDerivation dd : directDerivations)
//				System.out.println(dd.left);
//		}
	}
	
	private void applyRight(FunctionStructure source, Set<DirectDerivation> directDerivations){
		for(AbstractParametrizedOperation parametrizedOperation : right){
			if(!directDerivations.isEmpty()){
				parametrizedOperation.invoke(this, source, directDerivations, null, null);
				if(id != closeGapsRuleID) 
					AbstractRewriteSystem.directDerivationCounter++;
			}
		}
	}
	
	/*************************** DATA STORE ***************************/
	
	public int nextFunctionAnonymousVariableIndex = 0;
	public int nextFlowAnonymousVariableIndex = 0;
	
	private List<FunctionLabel> functionConstants = new ArrayList<FunctionLabel>();
	private List<FlowLabel> flowConstants = new ArrayList<FlowLabel>();
	private List<String> otherConstants = new ArrayList<String>();
	
	public Node getNode(Pair<ParameterType,Integer> parameter, DirectDerivation directDerivation, FunctionStructure functionSructure){
		return functionSructure.getFunction(directDerivation.nodeVariables.get(parameter.getValue1()));
	}
	public void setNode(Pair<ParameterType,Integer> parameter, DirectDerivation directDerivation, int nodeID){
		directDerivation.nodeVariables.put(parameter.getValue1(), nodeID);
	}
	
	public int initFunction(FunctionLabel function){
		functionConstants.add(function);
		return functionConstants.size() -1;
	}
	public FunctionLabel getFunction(Pair<ParameterType,Integer> parameter, DirectDerivation directDerivation){
		if(directDerivation != null && parameter.getValue0() == ParameterType.FUNCTION_VARIABLE){
			return directDerivation.functionVariables.get(parameter.getValue1());
		} else if(directDerivation != null && parameter.getValue0() == ParameterType.FUNCTION_ANONYMOUS_VARIABLE){
			return directDerivation.functionAnonymousVariables.get(parameter.getValue1());
		} else if(parameter.getValue0() == ParameterType.FUNCTION_CONSTANT){
			return functionConstants.get(parameter.getValue1());
		}
		return null;
	}
	public void setFunction(Pair<ParameterType,Integer> parameter, DirectDerivation directDerivation, FunctionLabel function){
		if(parameter.getValue0() == ParameterType.FUNCTION_VARIABLE){
			directDerivation.functionVariables.put(parameter.getValue1(), function);
		} else if(parameter.getValue0() == ParameterType.FUNCTION_ANONYMOUS_VARIABLE){
			directDerivation.functionAnonymousVariables.put(parameter.getValue1(), function);
		}
	}
	public FunctionLabel getFunctionConstant(Pair<ParameterType,Integer> parameter){
		return functionConstants.get(parameter.getValue1());
	}
	
	public int initFlow(FlowLabel flow){
		flowConstants.add(flow);
		return flowConstants.size() -1;
	}
	public FlowLabel getFlow(Pair<ParameterType,Integer> parameter, DirectDerivation directDerivation){
		if(directDerivation != null && parameter.getValue0() == ParameterType.FLOW_VARIABLE){
			return directDerivation.flowVariables.get(parameter.getValue1());
		} else if(directDerivation != null && parameter.getValue0() == ParameterType.FLOW_ANONYMOUS_VARIABLE){
			return directDerivation.flowAnonymousVariables.get(parameter.getValue1());
		} else if(parameter.getValue0() == ParameterType.FLOW_CONSTANT){
			return flowConstants.get(parameter.getValue1());
		}
		return null;
	}
	public void setFlow(Pair<ParameterType,Integer> parameter, DirectDerivation directDerivation, FlowLabel flow){
		if(parameter.getValue0() == ParameterType.FLOW_VARIABLE){
			directDerivation.flowVariables.put(parameter.getValue1(), flow);
		} else if(parameter.getValue0() == ParameterType.FLOW_ANONYMOUS_VARIABLE){
			directDerivation.flowAnonymousVariables.put(parameter.getValue1(), flow);
		}
	}
	public FlowLabel getFlowConstant(Pair<ParameterType,Integer> parameter){
		return flowConstants.get(parameter.getValue1());
	}
	
	public int initOther(String other){
		otherConstants.add(other);
		return otherConstants.size() -1;
	}
	public String getOther(Pair<ParameterType,Integer> parameter){
		return otherConstants.get(parameter.getValue1());
	}
	
	/*************************** XML ***********************************/
	
	private Document loadDocument(String pathToXMLSchema, String pathToXML){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			factory.setSchema(schemaFactory.newSchema(new Source[] {new StreamSource(pathToXMLSchema)}));
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(pathToXML));
			document.getDocumentElement().normalize();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Document newDocument(String pathToXMLSchema){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			factory.setSchema(schemaFactory.newSchema(new Source[] {new StreamSource(pathToXMLSchema)}));
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*************************** XML SAVING ****************************/
	
	public void writeToXML(String pathToXMLSchema, String pathToXML){
		try {
			// try to load rule document
			Document document = loadDocument(pathToXMLSchema, pathToXML);
			Element rulesetXML;
			Element ruleXML = null;
			File file = new File(pathToXML);
			if(document == null){
				// create new if load failed
				file.createNewFile();
				document = newDocument(pathToXMLSchema);
//				rulesetXML = document.createElement("ruleset");
//				rulesetXML.setAttribute("xmlns", "http://www.iris.uni-stuttgart.de/eichhojn/FUNKY");
				rulesetXML = document.createElementNS("http://www.iris.uni-stuttgart.de/eichhojn/FUNKY", "ruleset");
				document.appendChild(rulesetXML);				
			} else {
				rulesetXML = (Element) document.getElementsByTagName("ruleset").item(0);
				ruleXML = document.getElementById("rule-" + id);
			}
			
			if(ruleXML == null){
				ruleXML = document.createElement("rule");
				ruleXML.setAttribute("id", "rule-" + id);
				rulesetXML.appendChild(ruleXML);
			} else {
				Element ruleXMLNew = document.createElement("rule");
				ruleXMLNew.setAttribute("id", "rule-" + id);
				rulesetXML.replaceChild(ruleXMLNew, ruleXML);
				ruleXML = ruleXMLNew;
			}
			
			Element leftXML = document.createElement("left");
			ruleXML.appendChild(leftXML);
			
			Element rightXML = document.createElement("right");
			ruleXML.appendChild(rightXML);
			
			for(AbstractParametrizedOperation operation : left){
				Element operationXML = document.createElement(operation.operation.toString());
				operation.saveToXML(document, operationXML);
				leftXML.appendChild(operationXML);
			}
			
			for(AbstractParametrizedOperation operation : right){
				Element operationXML = document.createElement(operation.operation.toString());
				operation.saveToXML(document, operationXML);
				rightXML.appendChild(operationXML);
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(file);
	 
			transformer.transform(source, result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*************************** XML LOADING ***************************/
	
	public void readFromXML(String pathToXMLSchema, String pathToXML){
		try {
			// load rule document
			Document document = loadDocument(pathToXMLSchema, pathToXML);
			
			// pick designated rule
			Element ruleXML = document.getElementById("rule-" + id);
			if(ruleXML == null){
//				System.out.println("Rule " + id + " does not exist in rule set " + pathToXML);
				return;
			}
			
			// load its lhs and rhs
			NodeList leftOperationsXML = ruleXML.getElementsByTagName("left").item(0).getChildNodes();
			parseLeftOperations(leftOperationsXML);

			NodeList rightOperationsXML = ruleXML.getElementsByTagName("right").item(0).getChildNodes();
			parseRightOperations(rightOperationsXML);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseLeftOperations(NodeList leftOperationsXML){
		for(int i=0; i<leftOperationsXML.getLength(); i++){
			String nodeName = leftOperationsXML.item(i).getNodeName();
			
			if(nodeName.equals("#text")) continue;

			AbstractParametrizedOperation parametrizedOperation = AbstractParametrizedOperation.getInstance(this, OperationType.valueOf(nodeName));
			if(parametrizedOperation.operation == OperationType.CHOICE){
				left.add(parametrizedOperation);
				parseLeftOperations(leftOperationsXML.item(i).getChildNodes());
				left.add(parametrizedOperation);
			} else if(parametrizedOperation.operation == OperationType.OPTION){
				left.add(parametrizedOperation);
				parseLeftOperations(leftOperationsXML.item(i).getChildNodes());
				left.add(parametrizedOperation);
			} else {
				NodeList parametersXML = leftOperationsXML.item(i).getChildNodes();
				parametrizedOperation.loadFromXML(parametersXML);
				left.add(parametrizedOperation);
			}
		}
	}
	
	private void parseRightOperations(NodeList rightOperationsXML){
		for(int i=0; i<rightOperationsXML.getLength(); i++){
			String nodeName = rightOperationsXML.item(i).getNodeName();
			
			if(nodeName.equals("#text")) continue;

			AbstractParametrizedOperation parametrizedOperation = AbstractParametrizedOperation.getInstance(this, OperationType.valueOf(nodeName));
			NodeList parametersXML = rightOperationsXML.item(i).getChildNodes();
			parametrizedOperation.loadFromXML(parametersXML);
			right.add(parametrizedOperation);
		}
	}
	
	public String toString(){
		String string = "";
		for(AbstractParametrizedOperation parametrizedOperation : left)
			string += parametrizedOperation.toString() + "\n";
		for(AbstractParametrizedOperation parametrizedOperation : right)
			string += parametrizedOperation.toString() + "\n";
		return string;
	}

	public int compareTo(Rule rule) {
		return rule.id - id;
	}
	
	public Rule copy(){
		Rule rule = new Rule(id);
		rule.left.addAll(left);
		rule.right.addAll(right);
		rule.externalHandler = externalHandler;
		rule.nextFunctionAnonymousVariableIndex = 0;
		rule.nextFlowAnonymousVariableIndex = 0;
		rule.functionConstants.addAll(functionConstants);
		rule.flowConstants.addAll(flowConstants);
		rule.otherConstants.addAll(otherConstants);
		return rule;
	}
	
}
