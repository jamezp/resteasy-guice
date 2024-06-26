/*
 * Copyright The RESTEasy Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.resteasy.guice;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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
import com.google.inject.Module;

public class GuiceProviderTest {
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
    public void testProvider() {
        final Module module = new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(TestExceptionProvider.class);
                binder.bind(TestResource.class).to(TestResourceException.class);
            }
        };
        final ModuleProcessor processor = new ModuleProcessor(dispatcher.getRegistry(), dispatcher.getProviderFactory());
        processor.processInjector(Guice.createInjector(module));
        final TestResource resource = TestPortProvider.createProxy(TestResource.class, TestPortProvider.generateBaseUrl());
        Assertions.assertEquals("exception", resource.getName());
        dispatcher.getRegistry().removeRegistrations(TestResource.class);
    }

    @Path("test")
    public interface TestResource {
        @GET
        String getName();
    }

    public static class TestResourceException implements TestResource {
        @Override
        public String getName() {
            throw new TestException();
        }
    }

    public static class TestException extends RuntimeException {
    }

    @Provider
    public static class TestExceptionProvider implements ExceptionMapper<TestException> {
        @Override
        public Response toResponse(final TestException exception) {
            return Response.ok("exception").build();
        }
    }
}
