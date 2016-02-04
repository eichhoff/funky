/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter.derivation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.eich.utils.ComparableList;

@SuppressWarnings("serial")
public class DerivationGroupSet extends HashSet<DerivationGroup>{

	protected final Derivation canonicalForm;

	public final List<Integer> independentIndices = new ComparableList<Integer>();
	
	public DerivationGroupSet(Derivation canonicalForm){
		super();
		this.canonicalForm = canonicalForm;
	}
	
	public DerivationGroupSet(Derivation canonicalForm, Collection<DerivationGroup> collection) {
		super(collection);
		this.canonicalForm = canonicalForm;
	}
	
	public String toString(){
		String string = " " + independentIndices.toString() + " ( ";
		for(DerivationGroup derivationGroup : this){
			string += derivationGroup.independentIndices + " ";
		}
		string += ") ";
		return string;
	}
	
}
