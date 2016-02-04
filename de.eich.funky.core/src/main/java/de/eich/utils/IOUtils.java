/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class IOUtils {

	public static void write(String path, Object object){
		try {
			ObjectOutputStream objectStream = new ObjectOutputStream(new FileOutputStream(path));
			objectStream.writeObject(object);
			objectStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Object read(String path){
		try {
			ObjectInputStream objectStream = new ObjectInputStream(new FileInputStream(path));
			Object object = objectStream.readObject();
			objectStream.close();
			return object;
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static void writeRDFS(String path, Model model){
		try {
			FileOutputStream fileStream = new FileOutputStream(path);
			model.write(fileStream, "RDF/XML");
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Model readRDFS(String path){
		try {
			Model model = ModelFactory.createDefaultModel();
			FileInputStream fileStream = new FileInputStream(path);
			model.read(fileStream, "RDF/XML");
			fileStream.close();
			return model;
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static void writeTextFile(String path, String text){
		try {
			PrintWriter out = new PrintWriter(path);
			out.println(text);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
