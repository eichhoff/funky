/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.utils;

public class StringUtils {

	public static String tabs(int n){
		String tabs = "";
		for(int i=0; i<n; i++)
			tabs += "\t";
		return tabs;
	}
	
}
