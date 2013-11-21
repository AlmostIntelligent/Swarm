package org.gethydrated.swarm.core.messages.container;

import java.io.Serializable;

public class Beacon implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6375998806484248657L;

	public static class StartBeacon extends Beacon {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3264736181889838815L;}
	
	public static class WebAppBeacon extends Beacon {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8313823464758614401L;
		private String ctxName;
		
		public WebAppBeacon(String string) {
			this.ctxName = string;
		}
		
		public String getCtxName() {
			return ctxName;
		}
	}
}
