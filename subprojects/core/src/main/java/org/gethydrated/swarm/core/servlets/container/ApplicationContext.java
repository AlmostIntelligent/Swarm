package org.gethydrated.swarm.core.servlets.container;

import org.gethydrated.swarm.core.messages.container.ApplicationFilterChain;
import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;
import org.gethydrated.swarm.core.messages.session.SessionObject;
import org.gethydrated.swarm.core.messages.session.SessionRequest;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletRequestWrapper;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletResponseWrapper;
import org.gethydrated.swarm.core.servlets.modules.DeploymentModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.FilterVirtualFileVisitor;
import org.jboss.vfs.util.MatchAllVirtualFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;

import javax.servlet.*;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 *
 */
public class ApplicationContext implements LifecycleAware, ServletContext, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6279484730466841682L;

	private static final int MAJOR_VERSION = 3;

    private static final int MINOR_VERSION = 1;

    private LoggingAdapter logger;

    private final Map<String, ServletContainerFacade> servlets = new HashMap<>();
    
    private final Map<String, String> servletMappings = new HashMap<>();
    
    private final Map<String, FilterContainer> filters = new HashMap<>();

    private final LinkedList<FilterMapping> filterMappings = new LinkedList<>();

    private final Map<String, Object> attributes = new HashMap<>();

    private final Set<TaglibDescriptor> taglibs = new HashSet<>();

    //will be done by hydra later on
    //private final SessionService sessionService = new SessionService();

    private ModuleLoader contextLoader;
    private Module module;
    private VirtualFile root;
    private Set<String> welcomeFiles = new HashSet<>();
    private String sessionId;
    private final String ctxName;

	private ActorSystem actorsystem;
	private ActorContext rootContext;
	private ActorRef rootRef;

	private LifecycleState state = LifecycleState.CREATED;
    
    public ApplicationContext() {
        this("ROOT");
    }

    public ApplicationContext(String name) {
    	ctxName = name;
        contextLoader = new DeploymentModuleLoader(Module.getBootModuleLoader());
        welcomeFiles.add("index.html");
        welcomeFiles.add("index.jsp");
    }

    public void addServletMapping(String name, String mapping) {
        servletMappings.put(mapping, name);
    }

    public Set<String> getServletMapping(String name) {
        Set<String> mappings = new HashSet<>();
        for (Entry<String, String> e : servletMappings.entrySet()) {
            if (name.equals(e.getValue())) {
                mappings.add(e.getKey());
            }
        }
        return mappings;
    }

    public void addFilterMappingUrl(EnumSet<DispatcherType> dispatcherTypes, boolean matchAfter, String[] urlPatterns, FilterContainer container) {
        FilterMapping fm = new FilterMapping(dispatcherTypes, urlPatterns, container);
        if (matchAfter) {
            filterMappings.add(fm);
        } else {
            filterMappings.addFirst(fm);
        }
    }

    public String mapServlet(String request) {
        String matchedPath = null;
        String matchedType = null;
        for (Entry<String, String> e : servletMappings.entrySet()) {
            Pattern p = Pattern.compile(e.getKey().replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*"));
            if (p.matcher(request.substring(ctxName.length()+1)).matches()) {
                if (e.getKey().endsWith("*")) {
                    if (matchedPath == null) {
                        matchedPath = e.getKey();
                    } else {
                        if (matchedPath.length() < e.getKey().length()) {
                            matchedPath = e.getKey();
                        }
                    }
                } else if (e.getKey().startsWith("*.")) {
                    matchedType = e.getKey();
                } else {
                    return e.getKey();
                }
            //retry wildcards as directory mapping
            } else if (e.getKey().endsWith("*") && p.matcher(request+"/").matches()) {
                if (matchedPath == null) {
                    matchedPath = e.getKey();
                } else {
                    if (matchedPath.length() < e.getKey().length()) {
                        matchedPath = e.getKey();
                    }
                }
            }
        }
        logger.info("Matching: path {}, type {}", matchedPath, matchedType);
        if (matchedPath == null && matchedType == null) {
            String def = servletMappings.get("/");
            if (def == null) {
                throw new RuntimeException("no match");
            }
            return "/";
        }

        return (matchedPath != null) ? matchedPath : matchedType;
    }

    private List<FilterFacade> mapFilters(String request, String servlet) {
        List<FilterFacade> mapped = new LinkedList<>();
        for (FilterMapping fm : filterMappings) {
            mapped.add(new FilterFacade(fm.filter));
        }
        return mapped;
    }

    public Set<String> getWelcomeFiles() {
        return Collections.unmodifiableSet(welcomeFiles);
    }

    public void setRoot(VirtualFile root) {
        this.root = root;
    }

    public void setModuleLoader(ModuleLoader contextLoader) {
        this.contextLoader = contextLoader;
    }

    public VirtualFile getResourceAsFile(String path) {
        return root.getChild(path);
    }

    public ApplicationFilterChain createFilterChain(SwarmHttpRequest request, SwarmHttpResponse response, ActorRef source) {
        response.setHttpVersion(request.getHttpVersion());
        response.setRequestId(request.getRequestId());
        SwarmServletRequestWrapper requestWrapper = new SwarmServletRequestWrapper(request);
        SwarmServletResponseWrapper responseWrapper = new SwarmServletResponseWrapper(response);
        return createFilterChain(requestWrapper, responseWrapper, source);
    }
    
    public ApplicationFilterChain createFilterChain(SwarmServletRequestWrapper request, SwarmServletResponseWrapper response, ActorRef source) {
    	String matched = mapServlet(request.getRequestURI());
    	String name = servletMappings.get(matched);
        ServletContainerFacade container = servlets.get(name);
        logger.info("matched {}", matched);
        logger.info("matched container {}", container.getName());
        request.setServletPath(matched);
        ApplicationFilterChain chain = new ApplicationFilterChain(request, response, container.ref(), source);
        chain.addFilters(mapFilters(request.getRequestURI(), matched));
    	return chain;
    }

    public SessionObject getSessionObject(boolean create) {
        ActorSelection sessions = actorsystem.actorSelection("/user/singleton/sessions");
    	if (sessionId != null) {
        	
        	Timeout timeout = new Timeout(Duration.create(1, "seconds"));
        	Future<Object> f = Patterns.ask(sessions, new SessionRequest(sessionId), timeout);
            SessionObject s;
			try {
				s = (SessionObject) Await.result(f, timeout.duration());
			} catch (Exception e) {
				s = null;
			}
            if (s == null && create) {
                s = new SessionObject();
                sessionId = s.getId();
                sessions.tell(s, null);
            }
            return s;
        } else if (create) {
            SessionObject s = new SessionObject();
            sessionId = s.getId();
            sessions.tell(s, null);
            return s;
        }
        return null;
    }

    public void addJspTagLib(final String tagName, final String path) {
        taglibs.add(new TaglibDescriptor() {
            @Override
            public String getTaglibURI() {
                return tagName;
            }

            @Override
            public String getTaglibLocation() {
                return path;
            }

            @Override
            public String toString() {
                return tagName + ":" + path;
            }
        });
    }
    
    /* ----- LifecycleAware methods ---------------------*/
    
	@Override
	public void init() {
		if (state == LifecycleState.CREATED) {
			state = LifecycleState.INIT;
			try {
				module = contextLoader.loadModule(ModuleIdentifier.fromString(DeploymentModuleLoader.MODULE_PREFIX + getName()));
			} catch (Throwable t) {
				getLogger().error(t, "Failed to initialize context '{}'", ctxName);
				state = LifecycleState.FAILED;
			}
		}
	}

	@Override
	public void destroy() {
		if (state == LifecycleState.RUNNING || state == LifecycleState.FAILED) {
			state = LifecycleState.DESTROY;
			try {
				
			} catch (Throwable t) {
				getLogger().error(t, "Failed to destroy context '{}'", ctxName);
			} finally {
				state = LifecycleState.STOPPED;
			}
		}
	}

	@Override
	public LifecycleState getState() {
		return state;
	}

	@Override
	public String getStateName() {
		return state.toString();
	}
    

    /* ----- ServletContext methods ---------------------*/

    @Override
    public String getContextPath() {
        return (ctxName.equals("ROOT")) ? "" : "/" + ctxName;
    }

    @Override
    public ServletContext getContext(String uripath) {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return MINOR_VERSION;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 3;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return (file != null) ? URLConnection.getFileNameMap().getContentTypeFor(file) : null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        VirtualFile file = getResourceAsFile(path);
        if (!file.exists()) {
            return null;
        }
        FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(MatchAllVirtualFileFilter.INSTANCE, VisitorAttributes.DEFAULT);
        try {
            file.visit(visitor);
            Set<String> result = new HashSet<>();
            for (VirtualFile f : visitor.getMatched()) {
                result.add("/" + f.getPathNameRelativeTo(root) + (f.isDirectory() ? "/" : ""));
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        logger.info(path);
        return module.getClassLoader().getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        logger.info(path);
        InputStream s =module.getClassLoader().findResourceAsStream(path, false);
        logger.info("{}", s);
        return s;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return Collections.emptyEnumeration();
    }

    @Override
    public Enumeration<String> getServletNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public void log(String msg) {
        getLogger().info(msg);
    }

    @Override
    public void log(Exception exception, String msg) {
    	getLogger().error(exception, msg);
    }

    @Override
    public void log(String message, Throwable throwable) {
        getLogger().info(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        return root.getPathName()+path;
    }

    @Override
    public String getServerInfo() {
        return "Swarm Servlets";
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        if (object == null) {
            removeAttribute(name);
        } else {
            attributes.put(name, object);
        }
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getServletContextName() {
        return ctxName;
    }

    @Override
    public Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null);
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, null, servlet);
    }

    @Override
    public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName(), null);
    }

    private Dynamic addServlet(String servletName, String className, Servlet servlet) {
        if (servletName == null || servletName.equals("")) {
            throw new IllegalArgumentException("Illegal servlet name '" + servletName + "'.");
        }

        className = (className != null && className.equals("")) ? null : className;

        ServletContainerFacade facade = servlets.get(servletName);

        if (facade != null) {
        	ServletRegistrationWrapper registration = (ServletRegistrationWrapper) facade.getRegistration();
            if (registration.isComplete()) {
                return null;
            }
            registration.setServletClass(className);
            registration.setServletClass(servlet);
            return facade.getRegistration();
        }
        facade = new ServletContainerFacade(servletName, this);
        ServletRegistrationWrapper registration = (ServletRegistrationWrapper) facade.getRegistration();
        registration.setServletClass(className);
        registration.setServletClass(servlet);
        servlets.put(servletName, facade);
        return registration;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        try {
			return clazz.newInstance();
		} catch (Throwable e) {
			throw new ServletException(e);
		}
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return servlets.get(servletName).getRegistration();
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    	Map<String, ServletRegistration> results = new HashMap<>();
        for (Entry<String, ServletContainerFacade> e : servlets.entrySet()) {
        	results.put(e.getKey(), e.getValue().getRegistration());
        }
    	return results;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return addFilter(filterName, className, null);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, null, filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName(), null);
    }

    private FilterRegistration.Dynamic addFilter(String filterName, String className, Filter filter) {
        className = (className != null && className.equals("")) ? null : className;

        FilterContainer container = filters.get(filterName);

        if (container != null) {
            if (container.isComplete()) {
                return null;
            }
            container.setFilterClass(className);
            container.setFilterClass(filter);
            return new FilterRegistrationWrapper(container);
        }
        container = new FilterContainer(filterName, this);
        container.setFilterClass(className);
        container.setFilterClass(filter);
        filters.put(filterName, container);
        return new FilterRegistrationWrapper(container);
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        try {
			return clazz.newInstance();
		} catch (Throwable e) {
			throw new ServletException(e);
		}
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        FilterContainer fc = filters.get(filterName);
    	return (fc != null) ? new FilterRegistrationWrapper(fc) : null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    	Map<String, FilterRegistration> results = new HashMap<>();
    	for (Entry<String, FilterContainer> e : filters.entrySet()) {
    		results.put(e.getKey(), new FilterRegistrationWrapper(e.getValue()));
    	}
        return results;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String className) {
        logger.warning("adding listener {}",className);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        logger.warning("adding listener {}",t);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        logger.warning("adding listener {}",listenerClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        logger.warning("adding listener {}",clazz);
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        System.out.println(taglibs);
        return new JspConfigDescriptor() {
            @Override
            public Collection<TaglibDescriptor> getTaglibs() {
                return new HashSet<>(taglibs);
            }

            @Override
            public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
                Set<JspPropertyGroupDescriptor> set = new HashSet<>();
                return set;
            }
        };
    }

    @Override
    public ClassLoader getClassLoader() {
        return new URLClassLoader(new URL[0],module.getClassLoader());
        //return module.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return "swarm";
    }

    @Override
    public String toString() {
        return "ApplicationContext{" +
                "servlets=" + servlets +
                ", filters=" + filters +
                '}';
    }

    private static class FilterMapping {

        private final EnumSet<DispatcherType> dispatcherTypes;
        private final String[] urlPatterns;
        private final FilterContainer filter;

        public FilterMapping(EnumSet<DispatcherType> dispatcherTypes, String[] urlPatterns, FilterContainer container) {
            if (dispatcherTypes == null) {
                this.dispatcherTypes = EnumSet.of(DispatcherType.REQUEST);
            } else {
                this.dispatcherTypes = dispatcherTypes;
            }
            this.urlPatterns = urlPatterns;
            this.filter = container;
        }
    }

	public void setActorSystem(ActorSystem system) {
		this.actorsystem = system;
	}

	public void setRootContext(ActorContext ctx) {
		rootContext = ctx;
	}
	
	public void setRootRef(ActorRef ref) {
		rootRef = ref;
	}
	
	public void setLogger(LoggingAdapter logger) {
		this.logger = logger;
	}
	
	public LoggingAdapter getLogger() {
		return logger;
	}

	public String getName() {
		return ctxName;
	}

	public FilterContainer getFilter(String name) {
		return filters.get(name);
	}

	public ServletContainerFacade getServletFacade(String name) {
		return servlets.get(name);
	}
}