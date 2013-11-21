package org.gethydrated.swarm.core.servlets.modules;

import org.gethydrated.swarm.core.servlets.container.Lifecycle;
import org.jboss.modules.DependencySpec;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ModuleSpec.Builder;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.filter.PathFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DeploymentModuleLoader extends ModuleLoader {

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
        }
        spec.addDependency(DependencySpec.createLocalDependencySpec(PathFilters.acceptAll(), PathFilters.acceptAll()));
        spec.addDependency(DependencySpec.createModuleDependencySpec(PathFilters.acceptAll(), ModuleIdentifier.fromString("org.gethydrated.swarm.spec"), false));
        return spec.create();
    }

    public static boolean isDeploymentModule(ModuleIdentifier identifier) {
        return identifier.getName().startsWith(MODULE_PREFIX);
    }

}