package ca.mcgill.mcb.pcingola.reactome.events;

import ca.mcgill.mcb.pcingola.reactome.Entity;

/**
 * A reactome event (any generic event, from pathways to polymerizations)
 * 
 * @author pcingola
 *
 */
public class Event extends Entity {

	public Event(int id, String name) {
		super(id, name);
	}

}
