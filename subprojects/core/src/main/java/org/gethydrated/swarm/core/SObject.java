package org.gethydrated.swarm.core;

import java.io.Serializable;

public class SObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4969353603780102038L;

	private final String clazz;
	
	private final byte[] data;
	
	public SObject(String clazz, byte[] array) {
		this.clazz = clazz;
		this.data = array;
	}

	public String getClazz() {
		return clazz;
	}

	public byte[] getData() {
		return data;
	}
	
}
