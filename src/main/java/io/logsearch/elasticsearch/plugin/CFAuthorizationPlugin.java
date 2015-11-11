package io.logsearch.elasticsearch.plugin;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.multibindings.Multibinder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.repositories.RepositoriesModule;
import org.elasticsearch.rest.action.cat.AbstractCatAction;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CFAuthorizationPlugin extends Plugin {

    private final Settings settings;

    public CFAuthorizationPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "cf-authorization plugin";
    }

    @Override
    public String description() {
        return "An Elasticsearch plugin that restricts access to log documents based on CF UAA credentials";
    }

    @Override
    public Collection<Module> nodeModules() {
        return Collections.<Module>singletonList(new ConfiguredExampleModule());
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        return services;
    }

    @Override
    public Settings additionalSettings() {;
        return Settings.EMPTY;
    }

     public void onModule(ActionModule actionModule) {
         actionModule.registerFilter(CFAuhorizationActionFilter.class);
     }

    public void onModule(RepositoriesModule repositoriesModule) {
    }

    /**
     * Module declaring some example configuration and a _cat action that uses
     * it.
     */
    public static class ConfiguredExampleModule extends AbstractModule {
        @Override
        protected void configure() {
          bind(CFAuthorizationPluginConfiguration.class).asEagerSingleton();
          Multibinder<AbstractCatAction> catActionMultibinder = Multibinder.newSetBinder(binder(), AbstractCatAction.class);
          catActionMultibinder.addBinding().to(CFAuthorizationCatAction.class).asEagerSingleton();
        }
    }
}
