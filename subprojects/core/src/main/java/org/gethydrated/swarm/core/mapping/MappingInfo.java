package org.gethydrated.swarm.core.mapping;

public class MappingInfo {

	public String servletName = null;
	public String servletPath = null;
	public String pathInfo = null;
	public String redirect = null;
	public String contextPath = null;

	public void clear() {
		servletPath = null;
		servletName = null;
		pathInfo = null;
		redirect = null;
		contextPath = null;
	}
	
	public boolean isEmpty() {
		return servletPath == null && servletName == null && pathInfo == null && redirect == null && contextPath == null;
	}
	
}
