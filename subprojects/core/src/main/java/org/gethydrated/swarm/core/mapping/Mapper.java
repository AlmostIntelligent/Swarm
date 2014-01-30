package org.gethydrated.swarm.core.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Mapper {

	Map<String, String> exacts = new HashMap<>();
	Map<String, String> paths = new HashMap<>();
	Map<String, String> extensions = new HashMap<>();
	String defaultServlet = null;
	String rootServlet = null;
	String ctxName = null;
	
	public Mapper(String ctxName) {
		this.ctxName = ctxName;
	}
	
	public void addMapping(String path, String name) {
		if(isPathMapping(path) && !paths.containsKey(path)) {
			paths.put(path, name);
		}
		if(isExactMapping(path) && !exacts.containsKey(path)) {
			exacts.put(path, name);
		}
		if(isExtensionMapping(path) && !extensions.containsKey(path)) {
			extensions.put(path, name);
		}
		if(isDefaultMapping(path) && defaultServlet == null) {
			defaultServlet = name;
		}
		if(isRootMapping(path) && rootServlet == null) {
			rootServlet = name;
		}
	}
	
	public void removeMapping(String path, String name) {
		if(isPathMapping(path)) {
			paths.remove(path);
		}
		if(isExactMapping(path)) {
			exacts.remove(path);
		}
		if(isExtensionMapping(path)) {
			extensions.remove(path);
		}
		if(isDefaultMapping(path)) {
			defaultServlet = null;
		}
		if(isRootMapping(path)) {
			rootServlet = null;
		}
	}
	
	/**
	 * Maps request uri to servlet resources. Will not
	 * serve META-INF or WEB-INF dirs.
	 * @param uri
	 * @param info
	 */
	public void map(String uri, MappingInfo info) {
		if (uri.startsWith("/"+ctxName+"/META-INF") || uri.startsWith("/"+ctxName+"/WEB-INF")) {
			info.clear();
			return;
		}
		mapInternal(uri, info);
	}
	
	public void mapInternal(String request, MappingInfo info) {
		if (!request.startsWith("/"+ctxName)) {
			return;
		}
		
		String uri = request.substring(ctxName.length()+1);
		
		//Check root match
		if ((uri.equals("/") || uri.equals("")) && rootServlet != null) {
			info.servletName = rootServlet;
			info.servletPath = "";
			info.contextPath = "";
			info.pathInfo = "/";
			return;
		}
		//Check exact matches
		for (Entry<String, String> e : exacts.entrySet()) {
			if(e.getKey().equals(uri)) {
				info.pathInfo = null;
				info.servletName = e.getValue();
				info.servletPath = e.getKey();
				info.contextPath = "/"+ctxName;
				return;
			}
		}
		
		//Check path matches
		String pathUri = uri;
		if(pathUri.endsWith("/")) {
			pathUri = pathUri.substring(0, pathUri.length()-1);
		}
		boolean stop = false;
		do {
			stop = (pathUri.equals("")) ? true : false;
			for(Entry<String, String> e : paths.entrySet()) {
				String s = e.getKey().substring(0, e.getKey().length()-2);
				if (s.equals(pathUri)) {
					info.contextPath = "/"+ctxName;
					info.servletName = e.getValue();
					info.servletPath = (e.getKey().equals("/*")) ? "" : e.getKey().substring(0, e.getKey().length()-2);
					info.pathInfo = uri.substring(info.servletPath.length());
					if(info.pathInfo.equals("") && info.servletPath.equals("") && e.getKey().equals("/*")) {
						info.pathInfo = "/";
					}
					if(info.pathInfo.equals("")) {
						info.pathInfo = null;
					}
					return;
				}
			}
			pathUri = popDirectory(pathUri);
		} while(!stop);
		
		//Check extension
		for (Entry<String, String> e : extensions.entrySet()) {
			if (uri.endsWith(e.getKey().substring(1, e.getKey().length()))) {
				info.servletName = e.getValue();
				info.servletPath = uri;
				info.contextPath = "/"+ctxName;
				return;
			}
		}
		
		//Redirect context root
		if (uri.equals("")) {
			info.redirect = "/";
			return;
		}
		
		//Check default servlet
		if (defaultServlet != null) {
			info.servletName = defaultServlet;
			info.servletPath = uri;
			info.contextPath = "/"+ctxName;
		}
	}

	private String popDirectory(String pathUri) {
		int i = pathUri.lastIndexOf("/");
		return (i>=0) ? pathUri.substring(0, i) : "";
	}

	private boolean isPathMapping(String path) {
		return path.startsWith("/") && path.endsWith("/*");
	}
	
	private boolean isExtensionMapping(String path) {
		return path.startsWith("*.");
	}
	
	private boolean isRootMapping(String path) {
		return path.equals("");
	}
	
	private boolean isDefaultMapping(String path) {
		return path.equals("/");
	}
		
	private boolean isExactMapping(String path) {
		return !isPathMapping(path) && !isExtensionMapping(path) && !isRootMapping(path) && !isDefaultMapping(path);
	}

	public Set<String> getMappings(String name) {
        Set<String> mappings = new HashSet<>();
        for (Entry<String, String> e : exacts.entrySet()) {
            if (name.equals(e.getValue())) {
                mappings.add(e.getKey());
            }
        }
        for (Entry<String, String> e : paths.entrySet()) {
            if (name.equals(e.getValue())) {
                mappings.add(e.getKey());
            }
        }
        for (Entry<String, String> e : extensions.entrySet()) {
            if (name.equals(e.getValue())) {
                mappings.add(e.getKey());
            }
        }
        if (rootServlet.equals(name)) {
        	mappings.add("");
        }
        if (defaultServlet.equals(name)) {
        	mappings.add("/");
        }
        return mappings;
	}
	
	public boolean containsMapping(String path) {
		return exacts.containsKey(path) || 
				paths.containsKey(path) || 
				extensions.containsKey(path) || 
				(isRootMapping(path) && rootServlet != null) ||
				(isDefaultMapping(path) && defaultServlet != null);
	}
}
