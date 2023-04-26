package org.osate.dfmea.export;

public class Pair<E extends Object, F extends Object> {
	private E first;
	private F second;

	public Pair(E key, F value) {
		this.first = key;
		this.second = value;
    }


	public E getKey() {
		return first;
	}

	public void setKey(E key) {
		this.first = key;
	}

	public F getValue() {
		return second;
	}

	public void setValue(F value) {
		this.second = value;
	}
}