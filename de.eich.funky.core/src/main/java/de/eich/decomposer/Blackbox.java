/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.decomposer;

import java.util.ArrayList;

import org.javatuples.Pair;

import de.eich.rewriter.FunctionalBasis.FlowLabel;
import de.eich.rewriter.FunctionalBasis.FunctionLabel;
import de.eich.rewriter.functionstructure.FunctionStructure;

public class Blackbox {

	public enum Operation {
		ADD_EXPORT, ADD_IMPORT, ADD_REACTION_FORCE_EXPORT, ADD_PRIMARY_FUNCTION
	}

	private FunctionStructure functionStructure;
	//private ArrayList<Pair<Operation, String[]>> operations = new ArrayList<Pair<Operation, String[]>>();
	
	public Blackbox() {
		functionStructure = new FunctionStructure();
	}
	
	public Blackbox(FunctionStructure functionStructure) {
		this.functionStructure = functionStructure;
	}

	public FunctionStructure getFunctionStructure() {
		return functionStructure;
	}

	public void addImport(FlowLabel flow){
		functionStructure.addFunction(FunctionLabel.IMPORT, flow, flow);
//		operations.add(new Pair<Operation, String[]>(Operation.ADD_IMPORT, new String[]{flow.toString()}));
	}
	
	public void addExport(FlowLabel flow){
		functionStructure.addFunction(FunctionLabel.EXPORT, flow, flow);
//		operations.add(new Pair<Operation, String[]>(Operation.ADD_EXPORT, new String[]{flow.toString()}));
	}
	
	public void addReactionForceExport(FlowLabel flow){
		functionStructure.setReactionForce(functionStructure.addFunction(FunctionLabel.EXPORT, flow, flow));
//		operations.add(new Pair<Operation, String[]>(Operation.ADD_REACTION_FORCE_EXPORT, new String[]{flow.toString()}));
	}
	
	public void addPrimaryFunction(FunctionLabel function, FlowLabel flow1, FlowLabel flow2){
		functionStructure.setPrimaryFunction(functionStructure.addFunction(function, flow1, flow2));
//		operations.add(new Pair<Operation, String[]>(Operation.ADD_PRIMARY_FUNCTION, new String[]{function.toString(), flow1.toString(), flow2.toString()}));
	}
	
	public void readFromRDFS(String pathToRDFS){
		functionStructure.readFromRDFS(pathToRDFS);
	}
	
	public void writeToRDFS(String pathToRDFS){
		functionStructure.writeToRDFS(pathToRDFS);
	}
	
	public String toString(){
		return functionStructure.toString();
	}
}
