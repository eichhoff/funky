/**
 * @author Julian Eichhoff
 *
 * Copyright 2014 Julian Eichhoff
 */
package de.eich.decomposer.optimization;

import org.opt4j.core.common.archive.Crowding;
import org.opt4j.core.common.archive.FrontDensityIndicator;

public class SelectorModule extends org.opt4j.optimizers.ea.SelectorModule {

	protected int tournament = 0;

	public int getTournament() {
		return tournament;
	}
	
	public void setTournament(int tournament) {
		this.tournament = tournament;
	}
	
	public void config() {
		bindSelector(Selector.class);
		bind(FrontDensityIndicator.class).to(Crowding.class);
	}

}
