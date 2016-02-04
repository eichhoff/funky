/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.utils;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("serial")
public class ComparableList<T> extends ArrayList<T> implements Comparable<ComparableList<T>>{

	public ComparableList(){
		super();
	}
	
	public ComparableList(Collection<T> collection) {
		super(collection);
	}
	
	public ComparableList(int size) {
		super(size);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int compareTo(ComparableList<T> list) {
		int length1, length2;
		if((length1 = size()) > (length2 = list.size())) return 1;
		else if (length1 < length2) return -1;
		
		int comparison;
		for(int i = 0; i < length1; i++){
			if((comparison = ((Comparable) get(i)).compareTo(list.get(i))) != 0) return comparison;
		}
		
		return 0;
	}

}
