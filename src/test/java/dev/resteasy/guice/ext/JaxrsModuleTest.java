/*
 * Copyright The RESTEasy Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.resteasy.guice.ext;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
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

import dev.resteasy.guice.ModuleProcessor;

public class JaxrsModuleTest {
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
    public void testInjection() {
        final Module module = new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(TestResource.class).to(JaxrsTestResource.class);
            }
        };
        final ModuleProcessor processor = new ModuleProcessor(dispatcher.getRegistry(), dispatcher.getProviderFactory());
        processor.processInjector(Guice.createInjector(module, new JaxrsModule()));
        final TestResource resource = TestPortProvider.createProxy(TestResource.class, TestPortProvider.generateBaseUrl());
        Assertions.assertEquals("ok", resource.getName());
        dispatcher.getRegistry().removeRegistrations(TestResource.class);
    }

    @Path("test")
    public interface TestResource {
        @GET
        String getName();
    }

    public static class JaxrsTestResource implements TestResource {
        private final ClientHttpEngine clientExecutor;
        private final RuntimeDelegate runtimeDelegate;
        private final Response.ResponseBuilder responseBuilder;
        private final UriBuilder uriBuilder;
        private final Variant.VariantListBuilder variantListBuilder;

        @Inject
        public JaxrsTestResource(final ClientHttpEngine clientExecutor, final RuntimeDelegate runtimeDelegate,
                final Response.ResponseBuilder responseBuilder, final UriBuilder uriBuilder,
                final Variant.VariantListBuilder variantListBuilder) {
            this.clientExecutor = clientExecutor;
            this.runtimeDelegate = runtimeDelegate;
            this.responseBuilder = responseBuilder;
            this.uriBuilder = uriBuilder;
            this.variantListBuilder = variantListBuilder;
        }

        @Override
        public String getName() {
            Assertions.assertNotNull(clientExecutor);
            Assertions.assertNotNull(runtimeDelegate);
            Assertions.assertNotNull(responseBuilder);
            Assertions.assertNotNull(uriBuilder);
            Assertions.assertNotNull(variantListBuilder);
            return "ok";
        }
    }
}
