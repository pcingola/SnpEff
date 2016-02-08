package org.snpeff.stats;

/**
 * A mutable boolean
 */
public class BooleanMutable {

	public boolean value = false;

	public BooleanMutable() {
	}

	public BooleanMutable(boolean value) {
		this.value = value;
	}

	public boolean is() {
		return value;
	}

	public void set(boolean value) {
		this.value = value;
	}

	public void setFalse() {
		value = false;
	}

	public void setTrue() {
		value = true;
	}

	public void toggle() {
		value = !value;
	}
}
