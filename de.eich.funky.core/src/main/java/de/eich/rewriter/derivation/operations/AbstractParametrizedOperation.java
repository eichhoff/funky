/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.derivation.DirectDerivation;
import de.eich.rewriter.derivation.Occurrence;
import de.eich.rewriter.derivation.Rule;
import de.eich.rewriter.functionstructure.FunctionStructure;

public abstract class AbstractParametrizedOperation {

	public static enum ParameterType{
		NODE_VARIABLE,
		FUNCTION_VARIABLE,
		FUNCTION_CONSTANT,
		FUNCTION_ANONYMOUS_VARIABLE,
		FLOW_VARIABLE,
		FLOW_CONSTANT,
		FLOW_ANONYMOUS_VARIABLE,
		OTHER
	}
	
	public static enum OperationType {
		GET_FUNCTIONS,
		GET_GAPS,
		WITHOUT_OUTGOING_FLOW,
		WITHOUT_INCOMING_FLOW,
		USE_OUTGOING_NONTERMINALS,
		USE_INCOMING_NONTERMINALS,
		NOT_EQUAL,
		HAS_LABEL,
		RELATED_LABELS,
		EXTERNAL_HANDLER,
		CHOICE,
		OPTION,
		ADD_FUNCTION, 
		ADD_OUTGOING_NONTERMINAL,
		ADD_INCOMING_NONTERMINAL,
		ADD_FLOW,
		ADD_LABEL
	}
	
	public final Rule rule;
	public final OperationType operation;
	public final List<Pair<ParameterType,Integer>> parameters = new ArrayList<Pair<ParameterType,Integer>>(); 
	
	protected AbstractParametrizedOperation(Rule rule, OperationType operation){
		this.rule = rule;
		this.operation = operation;
	}
	
	public static AbstractParametrizedOperation getInstance(Rule rule, OperationType operation){
		switch(operation){
		case GET_FUNCTIONS:
			return new GetFunctions(rule, operation);
		case GET_GAPS:
			return new GetGaps(rule, operation);
		case WITHOUT_OUTGOING_FLOW:
			return new WithoutFlow(rule, operation);		
		case WITHOUT_INCOMING_FLOW:
			return new WithoutFlow(rule, operation);	
		case USE_OUTGOING_NONTERMINALS:
			return new UseNonterminals(rule, operation);	
		case USE_INCOMING_NONTERMINALS:
			return new UseNonterminals(rule, operation);	
		case NOT_EQUAL:
			return new NotEqual(rule, operation);	
		case HAS_LABEL:
			return new HasLabel(rule, operation);	
		case RELATED_LABELS:
			return new RelatedLabels(rule, operation);	
		case EXTERNAL_HANDLER:
			return new ExternalHandler(rule, operation);	
		case CHOICE:
			return new Choice(rule, operation);	
		case OPTION:
			return new Choice(rule, operation);	
		case ADD_FUNCTION: 
			return new AddFunction(rule, operation);	
		case ADD_OUTGOING_NONTERMINAL:
			return new AddNonterminal(rule, operation);	
		case ADD_INCOMING_NONTERMINAL:
			return new AddNonterminal(rule, operation);	
		case ADD_FLOW:
			return new AddFlow(rule, operation);	
		case ADD_LABEL:
			return new AddLabel(rule, operation);	
		}
		return null;
	}
	
	public abstract void invoke(Rule rule, FunctionStructure source, Set<DirectDerivation> directDerivations, Set<Occurrence> avoidOccurrences, DirectDerivation precedingDirectDerivationOfSameRule);
	public abstract void invoke(Rule rule, FunctionStructure source, Occurrence occurrence, Set<DirectDerivation> directDerivations, DirectDerivation precedingDirectDerivationOfSameRule);
	public abstract void reset();

	/*************************** XML SAVING ****************************/
	
	public void saveToXML(Document document, Element parametersXML){
		for(Pair<ParameterType, Integer> parameter : parameters){
			Element parameterXML, parameterXML3;
			switch(parameter.getValue0()){
				case FUNCTION_ANONYMOUS_VARIABLE:
					parameterXML = document.createElement("function");
					parametersXML.appendChild(parameterXML);
					break;
				case FLOW_ANONYMOUS_VARIABLE:
					parameterXML = document.createElement("flow");
					parametersXML.appendChild(parameterXML);
					break;
				case NODE_VARIABLE:
					parameterXML = document.createElement("node-var");
					parameterXML.setAttribute("id", "node-" + parameter.getValue1());
					parametersXML.appendChild(parameterXML);
					break;
				case FUNCTION_VARIABLE:
					parameterXML = document.createElement("function");
					parametersXML.appendChild(parameterXML);
					parameterXML3 = document.createElement("function-var");
					parameterXML3.setAttribute("id", "function-" + parameter.getValue1());
					parameterXML.appendChild(parameterXML3);
					break;
				case FLOW_VARIABLE:
					parameterXML = document.createElement("flow");
					parametersXML.appendChild(parameterXML);
					parameterXML3 = document.createElement("flow-var");
					parameterXML3.setAttribute("id", "flow-" + parameter.getValue1());
					parameterXML.appendChild(parameterXML3);
					break;
				case FUNCTION_CONSTANT:
					parameterXML = document.createElement("function");
					parametersXML.appendChild(parameterXML);
					parameterXML3 = document.createElement("function-const");
					parameterXML3.setTextContent(rule.getFunctionConstant(parameter).name());
					parameterXML.appendChild(parameterXML3);
					break;
				case FLOW_CONSTANT:
					parameterXML = document.createElement("flow");
					parametersXML.appendChild(parameterXML);
					parameterXML3 = document.createElement("flow-const");
					parameterXML3.setTextContent(rule.getFlowConstant(parameter).name());
					parameterXML.appendChild(parameterXML3);
					break;
				case OTHER:
					parameterXML = document.createElement("other");
					parameterXML.setTextContent(rule.getOther(parameter));
					parametersXML.appendChild(parameterXML);
					break;
			}
		}
	}
	
	/*************************** XML LOADING ***************************/
	
	public void loadFromXML(NodeList parametersXML){
		for(int i=0; i<parametersXML.getLength(); i++){
			if(!parametersXML.item(i).hasChildNodes()){
				if(!parametersXML.item(i).hasAttributes()){
					String type = parametersXML.item(i).getNodeName();
					if(type.equals("function")){
						parameters.add(new Pair<ParameterType,Integer>(ParameterType.FUNCTION_ANONYMOUS_VARIABLE, rule.nextFunctionAnonymousVariableIndex++));
					} else if(type.equals("flow")){
						parameters.add(new Pair<ParameterType,Integer>(ParameterType.FLOW_ANONYMOUS_VARIABLE, rule.nextFlowAnonymousVariableIndex++));
					}
				} else {
					String nodeVariableXML = parametersXML.item(i).getAttributes().getNamedItem("id").getNodeValue();
					int nodeVariableID = Integer.parseInt(nodeVariableXML.substring(nodeVariableXML.indexOf('-') +1));
					parameters.add(new Pair<ParameterType,Integer>(ParameterType.NODE_VARIABLE, nodeVariableID));
				}
			} else {
				Node parameterXML = parametersXML.item(i).getFirstChild().getNextSibling();
				if(parameterXML == null) parameterXML = parametersXML.item(i).getFirstChild();
				String type = parameterXML.getNodeName();
				if(type.equals("function-var")){
					String functionVariableXML = parameterXML.getAttributes().getNamedItem("id").getNodeValue();
					int functionVariableID = Integer.parseInt(functionVariableXML.substring(functionVariableXML.indexOf('-') +1));
					parameters.add(new Pair<ParameterType,Integer>(ParameterType.FUNCTION_VARIABLE, functionVariableID));
				} else if(type.equals("function-const")){
					FunctionLabel functionConstant = FunctionLabel.valueOf(parameterXML.getFirstChild().getNodeValue());
					parameters.add(new Pair<ParameterType,Integer>(ParameterType.FUNCTION_CONSTANT, rule.initFunction(functionConstant)));
				} else if(type.equals("flow-var")){
					String flowVariableXML = parameterXML.getAttributes().getNamedItem("id").getNodeValue();
					int flowVariableID = Integer.parseInt(flowVariableXML.substring(flowVariableXML.indexOf('-') +1));
					parameters.add(new Pair<ParameterType,Integer>(ParameterType.FLOW_VARIABLE, flowVariableID));
				} else if(type.equals("flow-const")){
					FlowLabel flowConstant = FlowLabel.valueOf(parameterXML.getFirstChild().getNodeValue());
					parameters.add(new Pair<ParameterType,Integer>(ParameterType.FLOW_CONSTANT, rule.initFlow(flowConstant)));
				} else if(type.equals("#text") && !parameterXML.getNodeValue().trim().isEmpty()){
					parameters.add(new Pair<ParameterType,Integer>(ParameterType.OTHER, rule.initOther(parameterXML.getNodeValue())));
				}
			}
		}
	}
	
	public String toString(){
		String parameterString = "";
		for(Pair<ParameterType, Integer> parameter : parameters){
			parameterString += parameter + " ";
			if(parameter.getValue0() == ParameterType.FLOW_CONSTANT)
				parameterString += "(" + rule.getFlow(parameter, null) + ") ";
			else if(parameter.getValue0() == ParameterType.FUNCTION_CONSTANT)
				parameterString += "(" + rule.getFunction(parameter, null) + ") ";
		}
		return "Operation: " + operation + "\nParameters: " + parameterString;
	}
	
}
