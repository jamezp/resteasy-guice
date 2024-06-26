/*
 * Copyright The RESTEasy Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.resteasy.guice;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;

public class ResourceTest {
    private static NettyJaxrsServer server;
    private static Dispatcher dispatcher;

    @BeforeAll
    public static void beforeClass() throws Exception {
        server = new NettyJaxrsServer();
        server.setPort(TestPortProvider.getPort());
        server.setRootResourcePath("/");
        ResteasyDeployment deployment = server.getDeployment();
        deployment.start();
        dispatcher = deployment.getDispatcher();
        server.start();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        server.stop();
        server = null;
        dispatcher = null;
    }

    @Test
    public void testResourceRegistered() {
        final Module module = new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(TestResource.class).to(TestResourceSimple.class);
            }
        };
        final ModuleProcessor processor = new ModuleProcessor(dispatcher.getRegistry(), dispatcher.getProviderFactory());
        processor.processInjector(Guice.createInjector(module));
        final TestResource resource = TestPortProvider.createProxy(TestResource.class, TestPortProvider.generateBaseUrl());
        Assertions.assertEquals("name", resource.getName());
        dispatcher.getRegistry().removeRegistrations(TestResource.class);
    }

    @Test
    public void testResourceInjected() {
        final Module module = new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(String.class).toInstance("injected-name");
                binder.bind(TestResource.class).to(TestResourceInjected.class);
            }
        };
        final ModuleProcessor processor = new ModuleProcessor(dispatcher.getRegistry(), dispatcher.getProviderFactory());
        processor.processInjector(Guice.createInjector(module));
        final TestResource resource = TestPortProvider.createProxy(TestResource.class, TestPortProvider.generateBaseUrl());
        Assertions.assertEquals("injected-name", resource.getName());
        dispatcher.getRegistry().removeRegistrations(TestResource.class);
    }

    @Path("test")
    public interface TestResource {
        @GET
        String getName();
    }

    public static class TestResourceSimple implements TestResource {
        @Override
        public String getName() {
            return "name";
        }
    }

    public static class TestResourceInjected implements TestResource {
        private final String name;

        @Inject
        public TestResourceInjected(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
