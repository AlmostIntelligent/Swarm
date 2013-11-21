package org.gethydrated.swarm.core.messages.deploy;

import java.io.Serializable;

public class Deployment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6561857535841314886L;
	protected String fileBase;
	
	private Deployment() {
		fileBase = null;
	}
	
	public String getFile() {
		return fileBase;
	}
	
	public static class Deploy extends Deployment {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8535253397806677286L;

		public Deploy(String file) {
			fileBase = file;
		}
		
	}
	
	public static class Undeploy extends Deployment {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7271550274091159098L;

		public Undeploy(String file) {
			fileBase = file;
		}
		
	}
	
}
