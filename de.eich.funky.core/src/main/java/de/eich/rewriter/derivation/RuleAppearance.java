/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

public class RuleAppearance implements Comparable<RuleAppearance>{

	public final int id;
	public final int count; // max 99
	private final int comparator;
	
	public RuleAppearance(int ruleID, int ruleCount){
		this.id = ruleID;
		this.count = ruleCount;
		comparator = ruleID*100 + ruleCount;
	}
	
	public int compareTo(RuleAppearance ruleAppearance) {
		return comparator - ruleAppearance.comparator;
	}
	
	public boolean equals(Object object){
		if(object instanceof RuleAppearance)
			return comparator == ((RuleAppearance) object).comparator;
		return super.equals(object);
	}
	
}
