package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.container.LifecycleState;
import org.gethydrated.swarm.container.connector.ServletRequestWrapper;
import org.gethydrated.swarm.container.connector.ServletResponseWrapper;
import org.gethydrated.swarm.modules.DeploymentModuleLoader;
import org.gethydrated.swarm.server.SwarmHttpRequest;
import org.gethydrated.swarm.server.SwarmHttpResponse;
import org.gethydrated.swarm.sessions.SessionObject;
import org.gethydrated.swarm.sessions.SessionService;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.vfs.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.net.URLConnection;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 *
 */
public class ApplicationContext extends AbstractContainer implements ServletContext, Serializable {

    private static final int MAJOR_VERSION = 3;

    private static final int MINOR_VERSION = 1;

    private final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    private final Map<String, ServletContainer> servlets = new HashMap<>();

    private final Map<String, FilterContainer> filters = new HashMap<>();

    private final Map<String, ServletContainer> servletMappings = new HashMap<>();

    private final LinkedList<FilterMapping> filterMappings = new LinkedList();

    private final Map<String, Object> attributes = new HashMap<>();

    //will be done by hydra later on
    private final SessionService sessionService = new SessionService();

    private ModuleLoader contextLoader;
    private Module module;
    private VirtualFile root;
    private Set<String> welcomeFiles = new HashSet<>();
    private String sessionId;

    public ApplicationContext() {
        this("ROOT");
    }

    public ApplicationContext(String name) {
        super(name);
        contextLoader = new DeploymentModuleLoader(Module.getBootModuleLoader());
        welcomeFiles.add("index.html");
        welcomeFiles.add("index.jsp");
    }

    public void invoke(SwarmHttpRequest request, SwarmHttpResponse response) {
        try {
            response.setHttpVersion(request.getHttpVersion());
            response.setRequestId(request.getRequestId());
            invoke(new ServletRequestWrapper(request, this), new ServletResponseWrapper(response, this));
        } catch (Exception e) {
            getLogger().error("Error while processing servlet: {}",e);
        }
    }

    public Set<String> addServletMapping(ServletContainer container, String... urlPatterns) {
        Set<String> duplicates = new HashSet<>();
        for (String s : urlPatterns) {
            if (servletMappings.containsKey(s)) {
                duplicates.add(s);
            } else {
                servletMappings.put(s, container);
            }
        }
        return duplicates;
    }

    public Set<String> getServletMapping(ServletContainer container) {
        Set<String> mappings = new HashSet<>();
        for (Entry<String, ServletContainer> e : servletMappings.entrySet()) {
            if (container.getName().equals(e.getValue().getName())) {
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

    private String mapServlet(String request) {
        String matchedPath = null;
        String matchedType = null;
        for (Entry<String, ServletContainer> e : servletMappings.entrySet()) {
            Pattern p = Pattern.compile(e.getKey().replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*"));
            if (p.matcher(request).matches()) {
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

        if (matchedPath == null && matchedType == null) {
            ServletContainer def = servletMappings.get("/");
            if (def == null) {
                throw new RuntimeException("no match");
            }
            return "/";
        }

        return (matchedPath != null) ? matchedPath : matchedType;
    }

    private List<FilterContainer> mapFilters(String request, String servlet) {
        List<FilterContainer> mapped = new LinkedList<>();
        for (FilterMapping fm : filterMappings) {
            mapped.add(fm.filter);
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

    public void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (getState() != LifecycleState.RUNNING) {
            throw new IllegalStateException("Context not in RUNNING state.");
        }
        try {
            String matched = mapServlet(request.getRequestURI());
            ServletContainer container = servletMappings.get(matched);
            logger.info("matched {}", matched);
            logger.info("matched container {}", container.getName());
            ((ServletRequestWrapper)request).setServletPath(matched);

            ApplicationFilterChain chain = new ApplicationFilterChain(container);
            chain.addFilters(mapFilters(request.getRequestURI(), matched));
            response.setStatus(200);
            chain.doFilter(request, response);
        } catch (Throwable t) {
            t.printStackTrace(response.getWriter());
            t.printStackTrace();
            response.sendError(404);
        }
    }

    public SessionObject getSessionObject(boolean create) {
        if (sessionId != null) {
            SessionObject s = sessionService.get(sessionId);
            if (s == null && create) {
                s = new SessionObject();
                sessionId = s.getId();
                sessionService.put(s);
            }
            return s;
        } else if (create) {
            SessionObject s = new SessionObject();
            sessionId = s.getId();
            sessionService.put(s);
            return s;
        }
        return null;
    }

    /* ----- Container methods ---------------------*/

    @Override
    public Logger getLogger() {
        return logger;
    }

    private FilterChain buildFilterChain(final String uri) {
        return new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                response.getWriter().println(uri);
            }
        };
    }

    @Override
    public void doInit() throws Exception {
        module = contextLoader.loadModule(ModuleIdentifier.fromString(DeploymentModuleLoader.MODULE_PREFIX + getName()));
        for (ServletContainer c : servlets.values()) {
            c.init();
        }
        for (FilterContainer c : filters.values()) {
            c.init();
        }
    }

    @Override
    public void doStart() {
        for (ServletContainer c : servlets.values()) {
            c.start();
        }
        for (FilterContainer c : filters.values()) {
            c.start();
        }
    }

    @Override
    public void doStop() {
        for (ServletContainer c : servlets.values()) {
            c.stop();
        }
        for (FilterContainer c : filters.values()) {
            c.stop();
        }
    }

    @Override
    public void doDestroy() {
        for (ServletContainer c : servlets.values()) {
            c.destroy();
        }
        for (FilterContainer c : filters.values()) {
            c.destroy();
        }
    }

    /* ----- ServletContext methods ---------------------*/

    @Override
    public String getContextPath() {
        return (getName().equals("ROOT")) ? "" : "/" + getName();
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
        return MAJOR_VERSION;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return MINOR_VERSION;
    }

    @Override
    public String getMimeType(String file) {
        return URLConnection.getFileNameMap().getContentTypeFor(file);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return module.getClassLoader().getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return module.getClassLoader().getResourceAsStream(path);
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
        return null;
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

        if (getState() != LifecycleState.CREATED) {
            throw new IllegalStateException("Servlet context already initialized.");
        }

        className = (className != null && className.equals("")) ? null : className;

        ServletContainer container = servlets.get(servletName);

        if (container != null) {
            if (container.isComplete()) {
                return null;
            }
            container.setServletClass(className);
            container.setServletClass(servlet);
            return new ServletRegistrationWrapper(container);
        }
        container = new ServletContainer(servletName);
        container.setParent(this);
        container.setServletClass(className);
        container.setServletClass(servlet);
        servlets.put(servletName, container);
        return new ServletRegistrationWrapper(container);
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
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
        container = new FilterContainer(filterName);
        container.setParent(this);
        container.setFilterClass(className);
        container.setFilterClass(filter);
        filters.put(filterName, container);
        return new FilterRegistrationWrapper(container);
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
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

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return new JspConfigDescriptor() {
            @Override
            public Collection<TaglibDescriptor> getTaglibs() {
                Set<TaglibDescriptor> tags = new HashSet<>();
                tags.add(new TaglibDescriptor() {
                    @Override
                    public String getTaglibURI() {
                        return "http://tomcat.apache.org/debug-taglib";
                    }

                    @Override
                    public String getTaglibLocation() {
                        return "/WEB-INF/jsp/debug-taglib.tld";
                    }
                });
                tags.add(new TaglibDescriptor() {
                    @Override
                    public String getTaglibURI() {
                        return "http://tomcat.apache.org/example-taglib";
                    }

                    @Override
                    public String getTaglibLocation() {
                        return "/WEB-INF/jsp/example-taglib.tld";
                    }
                });
                tags.add(new TaglibDescriptor() {
                    @Override
                    public String getTaglibURI() {
                        return "http://tomcat.apache.org/jsp2-example-taglib";
                    }

                    @Override
                    public String getTaglibLocation() {
                        return "/WEB-INF/jsp2/jsp2-example-taglib.tld";
                    }
                });
                return tags;
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
        return module.getClassLoader();
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

        public FilterMapping(EnumSet<DispatcherType> dispatcherTypes, String[] urlPatterns, FilterContainer filter) {
            if (dispatcherTypes == null) {
                this.dispatcherTypes = EnumSet.of(DispatcherType.REQUEST);
            } else {
                this.dispatcherTypes = dispatcherTypes;
            }
            this.urlPatterns = urlPatterns;
            this.filter = filter;
        }
    }
}
