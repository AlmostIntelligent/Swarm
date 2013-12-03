package org.gethydrated.swarm.core.messages.container;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Invoke implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4077560303732615954L;

	protected HttpServletRequest request;
	
	protected HttpServletResponse response;
	
	public HttpServletRequest request() {
		return request;
	}
	
	public HttpServletResponse response() {
		return response;
	}
	
	public static class InvokeServlet extends Invoke {

		/**
		 * 
		 */
		private static final long serialVersionUID = -419221254233885152L;

		public InvokeServlet(HttpServletRequest request,
				HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}
		
	}
	
	public static class InvokationResult extends Invoke {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3459462063291445626L;
		
		public InvokationResult(HttpServletRequest request, HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}
	}
}
