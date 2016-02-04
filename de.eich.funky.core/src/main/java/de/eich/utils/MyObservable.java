/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */

package de.eich.utils;

import java.util.Observable;

public class MyObservable extends Observable {

	public void setChangedAndNotifyObservers() {
		setChanged();
		notifyObservers();
	}
	
	public void setChangedAndNotifyObservers(Object data) {
		setChanged();
		notifyObservers(data);
	}

}
