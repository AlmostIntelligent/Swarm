package org.gethydrated.swarm.launcher;

import org.gethydrated.hydra.api.service.deploy.ArchiveSpec;
import org.gethydrated.hydra.api.service.deploy.ArchiveSpec.Builder;
import org.gethydrated.hydra.api.service.deploy.ModuleDependency;
import org.gethydrated.hydra.api.service.deploy.ServiceResolver;
import org.gethydrated.hydra.api.service.deploy.ServiceSpec;

/**
 *
 */
public class SwarmServiceResolver implements ServiceResolver {

    private ArchiveSpec swarmArchive;

    public SwarmServiceResolver() {
        Builder builder = ArchiveSpec.build();
        builder.setName("swarm");
        builder.setVersion("1.0");
        addServices(builder);
        addDependencies(builder);
        swarmArchive = builder.create();
    }

    private void addDependencies(Builder builder) {
        builder.addDependency(new ModuleDependency("org.gethydrated.swarm.server"));
        builder.addDependency(new ModuleDependency("org.gethydrated.swarm.servlet"));
    }

    private void addServices(Builder builder) {
        ServiceSpec.Builder b = ServiceSpec.build();
        b.setName("mapping");
        b.setVersion("1.0");
        b.setActivator("org.gethydrated.swarm.mapping.MappingService");
        builder.addService(b.create());
        b = ServiceSpec.build();
        b.setName("server");
        b.setVersion("1.0");
        b.setActivator("org.gethydrated.swarm.server.ServerService");
        builder.addService(b.create());
        b = ServiceSpec.build();
        b.setName("scanner");
        b.setVersion("1.0");
        b.setActivator("org.gethydrated.swarm.mapping.ScannerService");
        builder.addService(b.create());
        b = ServiceSpec.build();
        b.setName("webapp");
        b.setVersion("1.0");
        b.setActivator("org.gethydrated.swarm.container.WebAppService");
        builder.addService(b.create());
    }

    @Override
    public ArchiveSpec resolveArchive(String name) {
        return resolveArchive(name, null);
    }

    @Override
    public ArchiveSpec resolveArchive(String name, String version) {
        if (!name.equals("swarm")) {
            return null;
        }
        return swarmArchive;
    }

    @Override
    public ServiceSpec resolveService(String archive, String name) {
        return resolveService(archive, null, name, null);
    }

    @Override
    public ServiceSpec resolveService(String archive, String archiveVersion, String name, String version) {
        if (!name.equals("archive")) {
            return null;
        }
        for (ServiceSpec spec : swarmArchive.getServiceSpecs()) {
            if (spec.getName().equals(name)) {
                return spec;
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
