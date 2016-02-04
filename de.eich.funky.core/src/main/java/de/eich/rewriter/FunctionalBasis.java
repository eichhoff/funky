/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.rewriter;

import de.eich.rewriter.functionstructure.IdentifiableLabelContent;

public class FunctionalBasis {

	private static int nextID = 0;
	
	public enum AttributeLabel implements IdentifiableLabelContent {
		REACTION, 
		PRIMARY;
		
		public final int id = nextID++;
		
		public int getID() {
			return id;
		}
		
		public int getIndex(){
			AttributeLabel[] values = AttributeLabel.values();
			for(int i = 0; i < values.length; i++)
				if(values[i].equals(this))
					return i;
			return -1;
		}
	}
		
	public enum FunctionLabel implements IdentifiableLabelContent {
		ANY_FUNCTION(null),
		
		BRANCH(ANY_FUNCTION), 
		SEPARATE(BRANCH), SEPARATE_DIVIDE(SEPARATE), SEPARATE_EXTRACT(SEPARATE), SEPARATE_REMOVE(SEPARATE), 
		DISTRIBUTE(BRANCH),

		CHANNEL(ANY_FUNCTION), 
		IMPORT(CHANNEL), 
		EXPORT(CHANNEL), 
		TRANSFER(CHANNEL), TRANSFER_TRANSPORT(TRANSFER), TRANSFER_TRANSMIT(TRANSFER), 
		GUIDE(CHANNEL), GUIDE_TRANSLATE(GUIDE), GUIDE_ROTATE(GUIDE), GUIDE_ALLOWDOF(GUIDE),

		CONNECT(ANY_FUNCTION), 
		COUPLE(CONNECT), COUPLE_JOIN(COUPLE), COUPLE_LINK(COUPLE), 
		MIX(CONNECT),

		CONTROLMAGNITUDE(ANY_FUNCTION), 
		ACTUATE(CONTROLMAGNITUDE), 
		REGULATE(CONTROLMAGNITUDE), REGULATE_INCREASE(REGULATE), REGULATE_DECREASE(REGULATE), 
		CHANGE(CONTROLMAGNITUDE), CHANGE_INCREMENT(CHANGE), CHANGE_DECREMENT(CHANGE), CHANGE_SHAPE(CHANGE), CHANGE_CONDITION(CHANGE), 
		STOP(CONTROLMAGNITUDE), STOP_PREVENT(STOP), STOP_INHIBIT(STOP),

		CONVERT(ANY_FUNCTION),

		PROVISION(ANY_FUNCTION), 
		STORE(PROVISION), STORE_CONTAIN(STORE), STORE_COLLECT(STORE), 
		SUPPLY(PROVISION),

		SIGNAL(ANY_FUNCTION), 
		SENSE(SIGNAL), SENSE_CONTAIN(SENSE), SENSE_MEASURE(SENSE), 
		INDICATE(SIGNAL), INDICATE_TRACK(INDICATE), INDICATE_DISPLAY(INDICATE), 
		PROCESS(SIGNAL),

		SUPPORT(ANY_FUNCTION), 
		STABILIZE(SUPPORT), 
		SECURE(SUPPORT), 
		POSITION(SUPPORT);

		public final FunctionLabel parent;
		public final int id = nextID++;

		FunctionLabel(FunctionLabel parent) {
			this.parent = parent;
		}

		public static boolean related(FunctionLabel label1, FunctionLabel label2) {
			if(label1 == ANY_FUNCTION || label2 == ANY_FUNCTION) return true;
			do {
				if (label1 == label2)
					return true;
				label1 = label1.parent;
			} while (label1 != null);
			do {
				if (label2 == label1)
					return true;
				label2 = label2.parent;
			} while (label2 != null);
			return false;
		}

		public static FunctionLabel getLabelClass(FunctionLabel label) {
			while (label.parent != null) {
				label = label.parent;
			}
			return label;
		}

		public static FunctionLabel getTopLevelLabel(FunctionLabel label) {
			FunctionLabel previousLabel = label;
			while (label.parent != null) {
				previousLabel = label;
				label = label.parent;
			}
			return previousLabel;
		}

		public static FunctionLabel find(String string) {
			FunctionLabel found = null;
			String searchString1 = string.toUpperCase();
			String searchString2 = searchString1;
			int indexOfSpace = string.indexOf(" ");
			if (indexOfSpace != -1) {
				searchString1 = searchString1.replaceAll(" ", "");
				searchString2 = searchString2.substring(0, indexOfSpace);
			}
			try {
				found = FunctionLabel.valueOf(searchString1);
			} catch (Exception e1) {
				for (FunctionLabel label : FunctionLabel.values()) {
					if (label.name().contains(searchString1)) {
						found = label;
						break;
					}
				}
				if (found == null)
					for (FunctionLabel label : FunctionLabel.values()) {
						if (label.name().contains(searchString2)) {
							found = label;
							break;
						}
					}
			}
			return found;
		}

		public int getID() {
			return id;
		}
		
		public int getIndex(){
			FunctionLabel[] values = FunctionLabel.values();
			for(int i = 0; i < values.length; i++)
				if(values[i].equals(this))
					return i;
			return -1;
		}
	}

	public enum FlowLabel implements IdentifiableLabelContent {
		ANY_FLOW(null),
		
		MATERIAL(ANY_FLOW), 
		HUMAN(MATERIAL), 
		GAS(MATERIAL), 
		LIQUID(MATERIAL), 
		SOLID(MATERIAL), SOLID_OBJECT(SOLID), SOLID_PARTICULATE(SOLID), SOLID_COMPOSITE(SOLID), 
		PLASMA(MATERIAL), 
		MIXTURE(MATERIAL), MIXTURE_GAS_GAS(MIXTURE), MIXTURE_LIQUID_LIQUID(MIXTURE), MIXTURE_SOLID_SOLID(MIXTURE), MIXTURE_SOLID_LIQUID(MIXTURE), MIXTURE_LIQUID_GAS(MIXTURE), MIXTURE_SOLID_GAS(MIXTURE), MIXTURE_SOLID_LIQUID_GAS(MIXTURE), MIXTURE_COLLOIDAL(MIXTURE),

		SIGNAL(ANY_FLOW), 
		STATUS(SIGNAL), STATUS_AUDITORY(STATUS), STATUS_OLFACTORY(STATUS), STATUS_TACTILE(STATUS), STATUS_TASTE(STATUS), STATUS_VISUAL(STATUS), 
		CONTROL(SIGNAL), CONTROL_ANALOG(CONTROL), CONTROL_DISCRETE(CONTROL),

		ENERGY(ANY_FLOW), 
		HUMANENERGY(ENERGY), 
		ACOUSTICENERGY(ENERGY), 
		BIOLOGICALENERGY(ENERGY), 
		CHEMICALENERGY(ENERGY), 
		ELECTRICALENERGY(ENERGY), 
		ELECTROMAGNETICENERGY(ENERGY), ELECTROMAGNETICENERGY_OPTICAL(ELECTROMAGNETICENERGY), ELECTROMAGNETICENERGY_SOLAR(ELECTROMAGNETICENERGY), 
		HYDRAULICENERGY(ENERGY), 
		MAGNETICENERGY(ENERGY), 
		MECHANICALENERGY(ENERGY), MECHANICALENERGY_ROTATIONAL(MECHANICALENERGY), MECHANICALENERGY_TRANSLATIONAL(MECHANICALENERGY), 
		PNEUMATICENERGY(ENERGY), 
		RADIOACTIVENUCLEARENERGY(ENERGY), 
		THERMALENERGY(ENERGY);

		public final FlowLabel parent;
		public final int id = nextID++;

		FlowLabel(FlowLabel parent) {
			this.parent = parent;
		}

		public static boolean related(FlowLabel label1, FlowLabel label2) {
			if(label1 == ANY_FLOW || label2 == ANY_FLOW) return true;
			do {
				if (label1 == label2)
					return true;
				label1 = label1.parent;
			} while (label1 != null);
			do {
				if (label2 == label1)
					return true;
				label2 = label2.parent;
			} while (label2 != null);
			return false;
		}

		public static FlowLabel getLabelClass(FlowLabel label) {
			while (label.parent != null) {
				label = label.parent;
			}
			return label;
		}

		public static FlowLabel getTopLevelLabel(FlowLabel label) {
			FlowLabel previousLabel = label;
			while (label.parent != null) {
				previousLabel = label;
				label = label.parent;
			}
			return previousLabel;
		}

		public static FlowLabel find(String string) {
			FlowLabel found = null;
			String searchString1 = string.toUpperCase().replaceAll("-", "_").replaceAll("/", "");
			String searchString2 = searchString1;
			int indexOfSpace = string.indexOf(" ");
			if (indexOfSpace != -1) {
				searchString1 = searchString1.replaceAll(" ", "");
				searchString2 = searchString2.substring(0, indexOfSpace);
			}
			try {
				found = FlowLabel.valueOf(searchString1);
			} catch (Exception e1) {
				for (FlowLabel label : FlowLabel.values()) {
					if (label.name().contains(searchString1)) {
						found = label;
						break;
					}
				}
				if (found == null)
					for (FlowLabel label : FlowLabel.values()) {
						if (label.name().contains(searchString2)) {
							found = label;
							break;
						}
					}
			}
			return found;
		}
		
		public int getID() {
			return id;
		}
		
		public int getIndex(){
			FlowLabel[] values = FlowLabel.values();
			for(int i = 0; i < values.length; i++)
				if(values[i].equals(this))
					return i;
			return -1;
		}
	}

}
