/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.maven.scm.provider.ScmProvider;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class BasicScmManager extends AbstractScmManager {

    /**
     * Creates a manager populated with every {@link ScmProvider} registered through
     * {@link ServiceLoader} ({@code META-INF/services/org.apache.maven.scm.provider.ScmProvider})
     * in the given class loader, without needing a dependency injection container.
     * <p>
     * Each provider is registered under the value of its {@code javax.inject.Named} (or
     * {@code jakarta.inject.Named}) annotation, which is the same key used by the Sisu-based manager
     * (for example {@code git} for the git executable provider and {@code jgit} for the JGit one).
     * A provider without that annotation is registered under its {@link ScmProvider#getScmType() SCM type}.
     * If two providers share a key, the first one found wins.
     * <p>
     * A provider that cannot be loaded, for example because one of its dependencies is missing from
     * the class loader, is skipped with a warning; the other providers are still registered.
     *
     * @param classLoader the class loader to look providers up in
     * @return a new manager holding all discovered providers
     * @since 2.3.0
     */
    public static BasicScmManager fromServiceLoader(ClassLoader classLoader) {
        requireNonNull(classLoader);
        BasicScmManager manager = new BasicScmManager();
        Map<String, ScmProvider> providers = new LinkedHashMap<>();
        Iterator<ScmProvider> iterator =
                ServiceLoader.load(ScmProvider.class, classLoader).iterator();
        while (true) {
            try {
                if (!iterator.hasNext()) {
                    break;
                }
            } catch (ServiceConfigurationError e) {
                // the provider list itself cannot be read; retrying could loop forever
                manager.logger.warn("Cannot look up further SCM providers: {}", e.getMessage(), e);
                break;
            }
            ScmProvider provider;
            try {
                provider = iterator.next();
            } catch (ServiceConfigurationError e) {
                manager.logger.warn("Skipping an SCM provider that cannot be loaded: {}", e.getMessage(), e);
                continue;
            }
            providers.putIfAbsent(namedValue(manager, provider), provider);
        }
        manager.setScmProviders(providers);
        return manager;
    }

    // javax.inject is not a dependency of this module, so the annotation is read reflectively
    private static String namedValue(BasicScmManager manager, ScmProvider provider) {
        for (Annotation annotation : provider.getClass().getAnnotations()) {
            String type = annotation.annotationType().getName();
            if ("javax.inject.Named".equals(type) || "jakarta.inject.Named".equals(type)) {
                try {
                    Method value = annotation.annotationType().getMethod("value");
                    String name = (String) value.invoke(annotation);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot read @Named of " + provider.getClass(), e);
                }
            }
        }
        manager.logger.debug("No @Named on {}, registering it under its SCM type", provider.getClass());
        return provider.getScmType();
    }
}
