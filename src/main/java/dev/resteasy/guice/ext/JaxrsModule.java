/*
 * Copyright The RESTEasy Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.resteasy.guice.ext;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;

import com.google.inject.Binder;
import com.google.inject.Module;

public class JaxrsModule implements Module {

    public void configure(final Binder binder) {
        binder.bind(ClientHttpEngine.class).to(ApacheHttpClient43Engine.class);
        binder.bind(RuntimeDelegate.class).toInstance(RuntimeDelegate.getInstance());
        binder.bind(Response.ResponseBuilder.class).toProvider(ResponseBuilderProvider.class);
        binder.bind(UriBuilder.class).toProvider(UriBuilderProvider.class);
        binder.bind(Variant.VariantListBuilder.class).toProvider(VariantListBuilderProvider.class);
    }
}
