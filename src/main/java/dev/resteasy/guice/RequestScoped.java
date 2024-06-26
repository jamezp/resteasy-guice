/*
 * Copyright The RESTEasy Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.resteasy.guice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Scope;

/**
 * Provides an instance-per-request.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestScoped {
}
