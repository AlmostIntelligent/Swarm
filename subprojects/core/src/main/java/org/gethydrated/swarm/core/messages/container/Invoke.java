package org.gethydrated.swarm.core.messages.container;

import java.io.Serializable;

import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;

public class Invoke implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4077560303732615954L;

	protected SwarmHttpRequest request;
	
	protected SwarmHttpResponse response;
	
	public SwarmHttpRequest request() {
		return request;
	}
	
	public SwarmHttpResponse response() {
		return response;
	}
	
	public static class InvokeServlet extends Invoke {

		/**
		 * 
		 */
		private static final long serialVersionUID = -419221254233885152L;

		public InvokeServlet(SwarmHttpRequest request,
				SwarmHttpResponse response) {
			this.request = request;
			this.response = response;
		}
		
	}
	
	public static class InvokationResult extends Invoke {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3459462063291445626L;
		
		public InvokationResult(SwarmHttpRequest request, SwarmHttpResponse response) {
			this.request = request;
			this.response = response;
		}
	}
}
