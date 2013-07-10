package org.gethydrated.swarm.modules;

import org.gethydrated.swarm.container.Lifecycle;
import org.gethydrated.swarm.container.LifecycleListener;
import org.gethydrated.swarm.container.LifecycleState;
import org.jboss.modules.*;
import org.jboss.modules.ModuleSpec.Builder;
import org.jboss.modules.filter.PathFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DeploymentModuleLoader extends ModuleLoader implements Lifecycle {

    public static final String MODULE_PREFIX = "deploy.";
    private final Logger logger = LoggerFactory.getLogger(DeploymentModuleLoader.class);

    private final ModuleLoader parentLoader;
    private final Set<ResourceLoader> resourceLoaders = new HashSet<>();

    public DeploymentModuleLoader(ModuleLoader parentLoader) {
        this.parentLoader = parentLoader;
    }

    public void addResourceLoader(VFSResourceLoader vfsResourceLoader) {
        resourceLoaders.add(vfsResourceLoader);
    }

    public void relinkModule(Module module) throws ModuleLoadException {
        relink(module);
    }

    /* ----------------- ModuleLoader methods --------------------------*/

    @Override
    protected Module preloadModule(final ModuleIdentifier identifier) throws ModuleLoadException {
        if (isDeploymentModule(identifier)) {
            logger.info("Preloading {} module.", identifier.getName());
            return super.preloadModule(identifier);
        } else {
            logger.info("Module {} not found, delegating to parent loader.", identifier.getName());
            return preloadModule(identifier, parentLoader);
        }
    }

    @Override
    public ModuleSpec findModule(final ModuleIdentifier identifier) throws ModuleLoadException {
        logger.info("Find module {}.", identifier.getName());
        Builder spec = ModuleSpec.build(identifier);
        for (ResourceLoader rl : resourceLoaders) {
            spec.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(rl));
            spec.addDependency(DependencySpec.createLocalDependencySpec(PathFilters.acceptAll(), PathFilters.acceptAll()));
            spec.addDependency(DependencySpec.createModuleDependencySpec(PathFilters.acceptAll(), ModuleIdentifier.fromString("org.gethydrated.swarm.web"), false));

        }
        return spec.create();
    }

    /* ----------------- Lifecycle methods --------------------------*/

    @Override
    public void addListener(LifecycleListener listener) {

    }

    @Override
    public List<LifecycleListener> getListeners() {
        return null;
    }

    @Override
    public void removeListener(LifecycleListener listener) {

    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public LifecycleState getState() {
        return null;
    }

    @Override
    public String getStateName() {
        return null;
    }

    public static boolean isDeploymentModule(ModuleIdentifier identifier) {
        return identifier.getName().startsWith(MODULE_PREFIX);
    }

}
