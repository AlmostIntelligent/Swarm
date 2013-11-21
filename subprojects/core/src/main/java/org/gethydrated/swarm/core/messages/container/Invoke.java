package org.gethydrated.swarm.core.messages.container;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gethydrated.swarm.core.servlets.container.ApplicationFilterChain;

public class Invoke {

	protected HttpServletRequest request;
	
	protected HttpServletResponse response;
	
	public HttpServletRequest request() {
		return request;
	}
	
	public HttpServletResponse response() {
		return response;
	}
	
	public static class InvokeServlet extends Invoke {

		public InvokeServlet(HttpServletRequest request,
				HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}
		
	}
	
	public static class InvokeFilter extends Invoke {

		private FilterChain chain;
		
		public InvokeFilter(HttpServletRequest request, HttpServletResponse response,
				ApplicationFilterChain applicationFilterChain) {
			this.request = request;
			this.response = response;
			this.chain = applicationFilterChain;
		}
		
		public FilterChain getChain() {
			return chain;
		}
		
	}
}
