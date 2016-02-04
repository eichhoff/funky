package de.eich.utils;

import java.io.File;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.functionstructure.Edge;
import de.eich.rewriter.functionstructure.FunctionStructure;
import de.eich.rewriter.functionstructure.Label;
import de.eich.rewriter.functionstructure.Node;

public class SysML {

	private Document doc;
	private FunctionStructure functionStructure;
	
	public SysML(FunctionStructure functionStructure){
		this.functionStructure = functionStructure;
	}
	
	public static void main(String[] args){
		SysML sysML = new SysML(null);
		sysML.write(null);
	}
	
	private Element createPackagedElement(String name, String type, String id){
		Element element = doc.createElement("packagedElement");
		element.setAttribute("name", name);
		element.setAttribute("xmi:type", type);
		element.setAttribute("xmi:id", id);
		return element;
	}
	
	private Element createPackagedElement(String type, String id){
		Element element = doc.createElement("packagedElement");
		element.setAttribute("xmi:type", type);
		element.setAttribute("xmi:id", id);
		return element;
	}
	
	private Element createOwnedAttribute(String name, String type1, String type2, String id){
		Element attribute = doc.createElement("ownedAttribute");
		attribute.setAttribute("name", name);
		attribute.setAttribute("xmi:type", type1);
		attribute.setAttribute("type", type2);
		attribute.setAttribute("xmi:id", id);
		attribute.setAttribute("aggregation", "composite");
		
		Element upperValue = doc.createElement("upperValue");
		upperValue.setAttribute("xmi:type", "uml:LiteralInteger");
		upperValue.setAttribute("xmi:id", id + "_upper");
		upperValue.setAttribute("value", "1");
		attribute.appendChild(upperValue);
		
		Element lowerValue = doc.createElement("lowerValue");
		lowerValue.setAttribute("xmi:type", "uml:LiteralInteger");
		lowerValue.setAttribute("xmi:id", id + "_lower");
		lowerValue.setAttribute("value", "1");
		attribute.appendChild(lowerValue);
		
		return attribute;
	}
	
	private Element createOwnedEnd(String type1, String type2, String id){
		Element attribute = doc.createElement("ownedEnd");
		attribute.setAttribute("xmi:type", type1);
		attribute.setAttribute("type", type2);
		attribute.setAttribute("xmi:id", id);
		
		Element upperValue = doc.createElement("upperValue");
		upperValue.setAttribute("xmi:type", "uml:LiteralInteger");
		upperValue.setAttribute("xmi:id", id + "_upper");
		upperValue.setAttribute("value", "1");
		attribute.appendChild(upperValue);
		
		Element lowerValue = doc.createElement("lowerValue");
		lowerValue.setAttribute("xmi:type", "uml:LiteralInteger");
		lowerValue.setAttribute("xmi:id", id + "_lower");
		lowerValue.setAttribute("value", "1");
		attribute.appendChild(lowerValue);
		
		return attribute;
	}
	
	private Element createOwnedConnector(String name, String type, String id){
		Element attribute = doc.createElement("ownedConnector");
		attribute.setAttribute("name", name);
		attribute.setAttribute("xmi:type", type);
		attribute.setAttribute("xmi:id", id);
		
		return attribute;
	}
	
	private Element createEnd(String type, String partWithPort, String role, String id){
		Element attribute = doc.createElement("end");
		attribute.setAttribute("xmi:type", type);
		attribute.setAttribute("xmi:id", id);
		attribute.setAttribute("partWithPort", partWithPort);
		attribute.setAttribute("role", role);
		
		Element upperValue = doc.createElement("upperValue");
		upperValue.setAttribute("xmi:type", "uml:LiteralInteger");
		upperValue.setAttribute("xmi:id", id + "_upper");
		upperValue.setAttribute("value", "1");
		attribute.appendChild(upperValue);
		
		Element lowerValue = doc.createElement("lowerValue");
		lowerValue.setAttribute("xmi:type", "uml:LiteralInteger");
		lowerValue.setAttribute("xmi:id", id + "_lower");
		lowerValue.setAttribute("value", "1");
		attribute.appendChild(lowerValue);
		
		return attribute;
	}
	
//    <ownedConnector xmi:type = "uml:Connector" name = "Connects" xmi:id = "_ac15c7cb-4c4b-4cce-90bb-414e6a7afcdb">
//    <end xmi:type = "uml:ConnectorEnd" xmi:id = "_8ec81a66-b782-42d9-89be-7405affd1856" partWithPort = "_3a0f1fb7-2b45-4285-bb4f-0dcef2dd459b" role = "_ad4945c2-9061-41a0-92d9-4f70544e219a">
//      <upperValue xmi:type = "uml:LiteralInteger" xmi:id = "_8ec81a66-b782-42d9-89be-7405affd1856upper" value = "1"/>
//      <lowerValue xmi:type = "uml:LiteralInteger" xmi:id = "_8ec81a66-b782-42d9-89be-7405affd1856lower" value = "1"/>
//    </end>
//    <end xmi:type = "uml:ConnectorEnd" xmi:id = "_5cb97e53-2c3e-41cd-8750-331bca1f98ce" partWithPort = "_d143e812-5d3c-448f-aeee-88517a6a885a" role = "_a6515d86-7b4d-48e5-a86e-aeba15892ef1">
//      <upperValue xmi:type = "uml:LiteralInteger" xmi:id = "_5cb97e53-2c3e-41cd-8750-331bca1f98ceupper" value = "1"/>
//      <lowerValue xmi:type = "uml:LiteralInteger" xmi:id = "_5cb97e53-2c3e-41cd-8750-331bca1f98celower" value = "1"/>
//    </end>
//  </ownedConnector>
	
	private Element createSysMLBlock(String baseClass, String id){
		Element element = doc.createElement("sysml:Block");
		element.setAttribute("base_Class", baseClass);
		element.setAttribute("xmi:id", id);
		return element;
	}
	
	private Element createSysMLFlowSpecification(String baseInterface, String id){
		Element element = doc.createElement("sysml:FlowSpecification");
		element.setAttribute("base_Interface", baseInterface);
		element.setAttribute("xmi:id", id);
		return element;
	}
	
	private Element createSysMLFlowProperty(String baseProperty, String direction, String id){
		Element element = doc.createElement("sysml:FlowProperty");
		element.setAttribute("base_Feature", baseProperty);
		element.setAttribute("xmi:id", id);
		element.setAttribute("direction", direction);
		return element;
	}
	
	private Element createSysMLFlowPort(String basePort, String direction, String id){
		Element element = doc.createElement("sysml:FlowPort");
		element.setAttribute("base_Port", basePort);
		element.setAttribute("xmi:id", id);
		element.setAttribute("direction", direction);  // not found
		element.setAttribute("FlowPort_direction", direction);  // modelio specific, but doesn't work
		return element;
	}
	
	private Element createSysMLItemFlow(String baseInformationFlow, String itemProperty, String id){
		Element element = doc.createElement("sysml:ItemFlow");
		element.setAttribute("base_InformationFlow", baseInformationFlow);
		element.setAttribute("xmi:id", id);
		element.setAttribute("itemProperty", itemProperty); // not found
		element.setAttribute("ItemFlow_itemProperty", itemProperty); // modelio specific, but doesn't work
		return element;
	}
	
//	<sysml:ItemFlow base_InformationFlow = "_598f6a90-adb8-434d-96dc-345a2441ec6f" xmi:id = "_c7ec64e8-cea8-410d-bb82-0d5e0684dfdc" itemProperty ="_8a391be9-56c3-4b36-95b3-7c696a6f40ca"/>
	
	public static void convert(FunctionStructure functionStructure){
		SysML sysML = new SysML(functionStructure);
		sysML.convert();
	}
	
	private void convert(){
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
       	 
			// root elements
			doc = docBuilder.newDocument();
			Element xmi = doc.createElement("xmi:XMI");
			xmi.setAttribute("xmlns:xmi", "http://schema.omg.org/spec/XMI/2.1");
			xmi.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			xmi.setAttribute("xmlns:ecore", "http://www.eclipse.org/emf/2002/Ecore");
			xmi.setAttribute("xmlns:sysml", "http://www.omg.org/spec/SysML/1.2"); // SysML 1.2
			xmi.setAttribute("xmlns:uml", "http://www.eclipse.org/uml2/3.0.0/UML");
			
			xmi.setAttribute("xsi:schemaLocation", "http://www.omg.org/spec/SysML/1.2 sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg");
			

			xmi.setAttribute("xmi:version", "2.1");
			
			doc.appendChild(xmi);
	 
			Element uml = doc.createElement("uml:Model");
			uml.setAttribute("name", "what so ever");
			uml.setAttribute("xmi:id", "_FUNKY_UML");
			xmi.appendChild(uml);
			
			// ---------------------------------------------
			
			convertLabels(functionStructure, uml, xmi);
			convertNodesAndEdges(functionStructure, uml, xmi);
			
			// ---------------------------------------------
			
			Element profileApplication = doc.createElement("profileApplication");
			
			profileApplication.setAttribute("xmi:type", "uml:ProfileApplication");
			profileApplication.setAttribute("xmi:id", "_profileApplication0");
			Element appliedProfile = doc.createElement("appliedProfile");
			appliedProfile.setAttribute("xmi:type", "uml:Profile");
			appliedProfile.setAttribute("href", "sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg");
			profileApplication.appendChild(appliedProfile);
			
			uml.appendChild(profileApplication);
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("convert.xmi"));

			transformer.transform(source, result);
	 
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	}
	
	private void convertLabels(FunctionStructure functionStructure, Element uml, Element xmi){
		
		// generic flow
		uml.appendChild(createPackagedElement("FLOW", "uml:Class", "_flow"));
		xmi.appendChild(createSysMLBlock("_flow", "_flow_block"));
		
		for(Entry<Integer, Label> entry : functionStructure.graph.labels.entrySet()){
			int id = entry.getKey();
			Label label = entry.getValue();
			if(label.content instanceof FunctionLabel){
				System.out.println("FunctionLabel " + label);
				
				Element element = createPackagedElement(label.toString(), "uml:Class", "_function"+id);
				uml.appendChild(element);
				xmi.appendChild(createSysMLBlock("_function"+id, "_function"+id+"_block"));
				
				element.appendChild(createOwnedAttribute("in", "uml:Port", "_flow", "_function"+id+"_in"));
				xmi.appendChild(createSysMLFlowPort("_function"+id+"_in", "in", "_function"+id+"_in_flowport"));
				
				element.appendChild(createOwnedAttribute("out", "uml:Port", "_flow", "_function"+id+"_out"));
				xmi.appendChild(createSysMLFlowPort("_function"+id+"_out", "out", "_function"+id+"_out_flowport"));
				
			} else if(label.content instanceof FlowLabel){
				System.out.println("FlowLabel " + label);
				
//				uml.appendChild(createPackagedElement(label.toString(), "uml:Class", "_UML_0"));
//				xmi.appendChild(createSysMLBlock("_UML_0", "_SysML_0"));
				
				
//				Element flowSpecification = createPackagedElement("FS1", "uml:Interface", "_UML_1");
//				flowSpecification.setAttribute("isAbstract", "true");
//				Element flowProperty = createOwnedAttribute("fp1", "uml:Property", "_UML_0", "_UML_2");
//				flowProperty.setAttribute("visibility", "public");
//				flowSpecification.appendChild(flowProperty);
//				uml.appendChild(flowSpecification);
//			
//				if(addSysML)xmi.appendChild(createSysMLFlowSpecification("_UML_1", "_SysML_1"));
//				if(addSysML)xmi.appendChild(createSysMLFlowProperty("_UML_2", "in", "_SysML_2")); // geht irgendwie net
			}
		}
	}
	
	private void convertNodesAndEdges(FunctionStructure functionStructure, Element uml, Element xmi){
		
		// blackbox
		Element block = createPackagedElement("Blackbox", "uml:Class", "_blackbox");
		uml.appendChild(block);
		xmi.appendChild(createSysMLBlock("_blackbox", "_blackbox_block"));
		
		for(Entry<Integer, Node> entry : functionStructure.graph.nodes.entrySet()){
			int id = entry.getKey();
			Node node = entry.getValue();
			
			String partID = "_part"+node.id;
			String associationID = "_part"+node.id+"_association";
			String blackboxAssociationEndID = "_part"+node.id+"_association_end";
			String functionID = "_function"+node.labels.get(0).id;

			Element part = createOwnedAttribute(""+node.id, "uml:Property", functionID, partID);
			part.setAttribute("association", associationID);
			block.appendChild(part);
			
			Element association = createPackagedElement("uml:Association", associationID);
			association.setAttribute("memberEnd", blackboxAssociationEndID + " " + partID);
			Element end = createOwnedEnd("uml:Property", "_blackbox", blackboxAssociationEndID);
			end.setAttribute("association", associationID);
			end.setAttribute("visibility", "private");
			association.appendChild(end);
			uml.appendChild(association);
		}
		
		for(Edge edge : functionStructure.graph.edges){
			
			String outPartID = "_part"+edge.sourceNode.id;
			String outPortID = "_function"+edge.sourceNode.labels.get(0).id+"_out";
			String inPartID = "_part"+edge.targetNode.id;
			String inPortID = "_function"+edge.targetNode.labels.get(0).id+"_in";
			
			Element connector = createOwnedConnector("Connects", "uml:Connector", "_edge"+edge.hashCode()); // kind="delegation" benoetigt nach aussen dabei kein partwithport fuer end
			connector.appendChild(createEnd("uml:ConnectorEnd", outPartID, outPortID, "_edge"+edge.hashCode()+"_end1"));
			connector.appendChild(createEnd("uml:ConnectorEnd", inPartID, inPortID, "_edge"+edge.hashCode()+"_end2"));
			block.appendChild(connector);
			
			Element informationFlow = createPackagedElement("i", "uml:InformationFlow", "_edge"+edge.hashCode()+"_informationflow");
//			informationFlow.setAttribute("realizingConnector", "_edge"+edge.hashCode()); // not accepted by modelio, specifically commented out in modelio source code
			informationFlow.setAttribute("channel", "_edge"+edge.hashCode()+"_end1"); // not accepted by modelio
			informationFlow.setAttribute("conveyed", "_flow");
			informationFlow.setAttribute("informationSource", outPortID);
			informationFlow.setAttribute("informationTarget", inPortID);
			uml.appendChild(informationFlow);
			
			Element itemFlow = createSysMLItemFlow("_edge"+edge.hashCode()+"_informationflow", "_flow", "_edge"+edge.hashCode()+"_itemflow");
			xmi.appendChild(itemFlow);
		}
	}
	
		
	
	public void write(FunctionStructure functionStructure){
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
       	 
			boolean addSysML = true;
			
			// root elements
			doc = docBuilder.newDocument();
			Element xmi = doc.createElement("xmi:XMI");
			xmi.setAttribute("xmlns:xmi", "http://schema.omg.org/spec/XMI/2.1");
			xmi.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			xmi.setAttribute("xmlns:ecore", "http://www.eclipse.org/emf/2002/Ecore");
			if(addSysML) xmi.setAttribute("xmlns:sysml", "http://www.omg.org/spec/SysML/1.2"); // SysML 1.2
//			xmi.setAttribute("xmlns:uml", "http://schema.omg.org/spec/UML/2.3"); // UML 2.3
			xmi.setAttribute("xmlns:uml", "http://www.eclipse.org/uml2/3.0.0/UML");
			
			if(addSysML){
//				xmi.setAttribute("xsi:schemaLocation", "http://www.eclipse.org/uml2/3.0.0/UML http://schema.omg.org/spec/UML/20090901"
//						+ " http://www.omg.org/spec/SysML/1.2 sysml.profile.xmi#_eRVxh4HhEeO_UN4JkIhOpQ");
//				xmi.setAttribute("xsi:schemaLocation", "http://www.omg.org/spec/SysML/1.2 sysml.profile.xmi#_eRVxh4HhEeO_UN4JkIhOpQ");
				xmi.setAttribute("xsi:schemaLocation", "http://www.omg.org/spec/SysML/1.2 sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg");
			}
//			xmi.setAttribute("xsi:schemaLocation", "http://www.omg.org/spec/SysML/1.2 http://schema.omg.org/spec/UML/20090901"
//					+ " http://www.omg.org/spec/SysML/1.2 http://www.omg.org/spec/SysML/20100301/SysML-profile.uml#_0");
			

			xmi.setAttribute("xmi:version", "2.1");
			
			doc.appendChild(xmi);
	 
			Element uml = doc.createElement("uml:Model");
			uml.setAttribute("name", "what so ever");
			uml.setAttribute("xmi:id", "_FUNKY_UML");
			xmi.appendChild(uml);
			
			// ---------------------------------------------
			
			
			uml.appendChild(createPackagedElement("F0", "uml:Class", "_UML_0"));
			if(addSysML) xmi.appendChild(createSysMLBlock("_UML_0", "_SysML_0"));
			
			
//			Element flowSpecification = createPackagedElement("FS1", "uml:Interface", "_UML_1");
//			flowSpecification.setAttribute("isAbstract", "true");
//			Element flowProperty = createOwnedAttribute("fp1", "uml:Property", "_UML_0", "_UML_2");
//			flowProperty.setAttribute("visibility", "public");
//			flowSpecification.appendChild(flowProperty);
//			uml.appendChild(flowSpecification);
//		
//			if(addSysML)xmi.appendChild(createSysMLFlowSpecification("_UML_1", "_SysML_1"));
//			if(addSysML)xmi.appendChild(createSysMLFlowProperty("_UML_2", "in", "_SysML_2")); // geht irgendwie net
			
			
			
			Element block = createPackagedElement("B1", "uml:Class", "_UML_3");
			Element port = createOwnedAttribute("p1", "uml:Port", "_UML_0", "_UML_4");
			block.appendChild(port);
			uml.appendChild(block);
			
			if(addSysML)xmi.appendChild(createSysMLBlock("_UML_3", "_SysML_3"));
			if(addSysML)xmi.appendChild(createSysMLFlowPort("_UML_4", "out", "_SysML_101"));
			
			
			block = createPackagedElement("B2", "uml:Class", "_UML_5");
			port = createOwnedAttribute("p2", "uml:Port", "_UML_0", "_UML_6");
			block.appendChild(port);
			uml.appendChild(block);
			
			if(addSysML)xmi.appendChild(createSysMLBlock("_UML_5", "_SysML_4"));
			if(addSysML)xmi.appendChild(createSysMLFlowPort("_UML_6", "in", "_SysML_102"));
			
			
//			<sysml:FlowPort base_Port = "_ad4945c2-9061-41a0-92d9-4f70544e219a" xmi:id = "_13b1eeb7-4c5c-4dd9-9d56-f94b3bd8f5b4" direction = "out" />
			
			String id1 = "_UML_7";
			
			block = createPackagedElement("B0", "uml:Class", id1);
			if(addSysML)xmi.appendChild(createSysMLBlock(id1, "_SysML_5"));
			uml.appendChild(block);
			
			String id2 = "_UML_8";
			String id3 = "_UML_9";
			String id4 = "_UML_10";

			Element part = createOwnedAttribute("b1", "uml:Property", "_UML_3", id2);
			part.setAttribute("association", id3);
			block.appendChild(part);
			
			Element association = createPackagedElement("uml:Association", id3);
			association.setAttribute("memberEnd", id4 + " " + id2);
			Element end = createOwnedEnd("uml:Property", id1, id4);
			end.setAttribute("association", id3);
			end.setAttribute("visibility", "private");
			association.appendChild(end);
			uml.appendChild(association);
			
			id2 = "_UML_11";
			id3 = "_UML_12";
			id4 = "_UML_13";
			
			part = createOwnedAttribute("b2", "uml:Property", "_UML_5", id2);
			part.setAttribute("association", id3);
			block.appendChild(part);
			
			association = createPackagedElement("uml:Association", id3);
			association.setAttribute("memberEnd", id4 + " " + id2);
			end = createOwnedEnd("uml:Property", id1, id4);
			end.setAttribute("association", id3);
			end.setAttribute("visibility", "private");
			association.appendChild(end);
			uml.appendChild(association);
			
			
			id2 = "_UML_011";
			id3 = "_UML_012";
			id4 = "_UML_013";
			
			part = createOwnedAttribute("flo", "uml:Property", "_UML_0", id2);
			part.setAttribute("association", id3);
			block.appendChild(part);
			
			association = createPackagedElement("uml:Association", id3);
			association.setAttribute("memberEnd", id4 + " " + id2);
			end = createOwnedEnd("uml:Property", id1, id4);
			end.setAttribute("association", id3);
			end.setAttribute("visibility", "private");
			association.appendChild(end);
			uml.appendChild(association);
			
			
			Element connector = createOwnedConnector("Connects", "uml:Connector", "_UML_14"); // kind="delegation" benoetigt nach aussen dabei kein partwithport fuer end
			end = createEnd("uml:ConnectorEnd", "_UML_8", "_UML_4", "_8ec81a66-b782-42d9-89be-7405affd1856");
			connector.appendChild(end);
			end = createEnd("uml:ConnectorEnd", "_UML_11", "_UML_6", "_5cb97e53-2c3e-41cd-8750-331bca1f98ce");
			connector.appendChild(end);
			block.appendChild(connector);
			
//			if(addSysML)xmi.appendChild(createSysMLFlowPort("_8ec81a66-b782-42d9-89be-7405affd1856", "out", "_SysML_1011"));
//			if(addSysML)xmi.appendChild(createSysMLFlowPort("_5cb97e53-2c3e-41cd-8750-331bca1f98ce", "in", "_SysML_1022"));
			
			// _UML_8 property impl
			// _UML_4 port impl
			
			// http://www.oose.de/metamodellUML/
			
			Element informationFlow = createPackagedElement("i", "uml:InformationFlow", "_UML_15");
			informationFlow.setAttribute("realizingConnector", "_UML_14"); // not accepted by modelio
			informationFlow.setAttribute("conveyed", "_UML_0");
			informationFlow.setAttribute("informationSource", "_UML_4");
			informationFlow.setAttribute("informationTarget", "_UML_6");
			uml.appendChild(informationFlow);
			
			Element itemFlow = createSysMLItemFlow("_UML_15", "_UML_0", "_SysML_16");
			xmi.appendChild(itemFlow);
			
			
//			<sysml:ItemFlow base_InformationFlow = "_598f6a90-adb8-434d-96dc-345a2441ec6f" xmi:id = "_c7ec64e8-cea8-410d-bb82-0d5e0684dfdc" itemProperty ="_8a391be9-56c3-4b36-95b3-7c696a6f40ca"/>
			
//			<packagedElement xmi:type = "uml:InformationFlow" xmi:id = "_598f6a90-adb8-434d-96dc-345a2441ec6f" name = "i" realizingConnector = "_ac15c7cb-4c4b-4cce-90bb-414e6a7afcdb" conveyed = "_c6d41703-7dfc-4ef2-b324-e09cc1acc959" 
//			informationSource = "_ad4945c2-9061-41a0-92d9-4f70544e219a" informationTarget = "_a6515d86-7b4d-48e5-a86e-aeba15892ef1">
//		    </packagedElement>
			
			
			
//		      <ownedConnector xmi:type = "uml:Connector" name = "Connects" xmi:id = "_ac15c7cb-4c4b-4cce-90bb-414e6a7afcdb">
//		        <end xmi:type = "uml:ConnectorEnd" xmi:id = "_8ec81a66-b782-42d9-89be-7405affd1856" partWithPort = "_3a0f1fb7-2b45-4285-bb4f-0dcef2dd459b" role = "_ad4945c2-9061-41a0-92d9-4f70544e219a">
//		          <upperValue xmi:type = "uml:LiteralInteger" xmi:id = "_8ec81a66-b782-42d9-89be-7405affd1856upper" value = "1"/>
//		          <lowerValue xmi:type = "uml:LiteralInteger" xmi:id = "_8ec81a66-b782-42d9-89be-7405affd1856lower" value = "1"/>
//		        </end>
//		        <end xmi:type = "uml:ConnectorEnd" xmi:id = "_5cb97e53-2c3e-41cd-8750-331bca1f98ce" partWithPort = "_d143e812-5d3c-448f-aeee-88517a6a885a" role = "_a6515d86-7b4d-48e5-a86e-aeba15892ef1">
//		          <upperValue xmi:type = "uml:LiteralInteger" xmi:id = "_5cb97e53-2c3e-41cd-8750-331bca1f98ceupper" value = "1"/>
//		          <lowerValue xmi:type = "uml:LiteralInteger" xmi:id = "_5cb97e53-2c3e-41cd-8750-331bca1f98celower" value = "1"/>
//		        </end>
//		      </ownedConnector>
			
			
		
			
			// ---------------------------------------------
			if(addSysML){
				/*
				<profileApplication xmi:id="_pU-ZkINLEeOKGP636-G5Hg">
			      <eAnnotations xmi:id="_pU-ZkYNLEeOKGP636-G5Hg" source="http://www.eclipse.org/uml2/2.0.0/UML">
			        <references xmi:type="ecore:EPackage" href="sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg"/>
			      </eAnnotations>
			      <appliedProfile href="sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg"/>
			    </profileApplication>
			    */
				/*
				Element profileApplication = doc.createElement("profileApplication");
				
				profileApplication.setAttribute("xmi:type", "uml:ProfileApplication");
				profileApplication.setAttribute("xmi:id", "_profileApplication0");
				uml.appendChild(profileApplication);
				
				Element annotations = doc.createElement("eAnnotations");
				annotations.setAttribute("xmi:id", "_pU-ZkYNLEeOKGP636-G5Hg");
				annotations.setAttribute("source", "http://www.eclipse.org/uml2/3.0.0/UML");
				profileApplication.appendChild(annotations);
				
				Element references = doc.createElement("references");
				references.setAttribute("xmi:type", "ecore:EPackage");
				references.setAttribute("href", "sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg");
				annotations.appendChild(references);
				
				Element appliedProfile = doc.createElement("appliedProfile");
				appliedProfile.setAttribute("xmi:type", "uml:Profile");
				appliedProfile.setAttribute("href", "sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg");
				profileApplication.appendChild(appliedProfile);
				*/
				
				
				Element profileApplication = doc.createElement("profileApplication");
				
				profileApplication.setAttribute("xmi:type", "uml:ProfileApplication");
				profileApplication.setAttribute("xmi:id", "_profileApplication0");
				Element appliedProfile = doc.createElement("appliedProfile");
				appliedProfile.setAttribute("xmi:type", "uml:Profile");
				appliedProfile.setAttribute("href", "sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg");
				profileApplication.appendChild(appliedProfile);
				
				uml.appendChild(profileApplication);
			}
			

//			if(addSysML){
//				/*
//				<profileApplication xmi:id="_pU-ZkINLEeOKGP636-G5Hg">
//			      <eAnnotations xmi:id="_pU-ZkYNLEeOKGP636-G5Hg" source="http://www.eclipse.org/uml2/2.0.0/UML">
//			        <references xmi:type="ecore:EPackage" href="sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg"/>
//			      </eAnnotations>
//			      <appliedProfile href="sysml.profile.xmi#_pT73wINLEeOKGP636-G5Hg"/>
//			    </profileApplication>
//			    */
//				
//				Element profileApplication = doc.createElement("profileApplication");
//				
//				profileApplication.setAttribute("xmi:type", "uml:ProfileApplication");
//				profileApplication.setAttribute("xmi:id", "_profileApplication0");
//				Element appliedProfile = doc.createElement("appliedProfile");
//				appliedProfile.setAttribute("xmi:type", "uml:Profile");
//				appliedProfile.setAttribute("href", "sysml.profile.xmi#_f1akV4NKEeOKGP636-G5Hg");
//				profileApplication.appendChild(appliedProfile);
//				
//				
//				uml.appendChild(profileApplication);
//			}		   
			
				
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("export.xml"));

			transformer.transform(source, result);
	 
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	}
	
	
}
