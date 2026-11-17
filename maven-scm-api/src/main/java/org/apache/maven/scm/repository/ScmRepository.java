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
package org.apache.maven.scm.repository;

import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * A repository as seen by clients of the {@link org.apache.maven.scm.manager.ScmManager}: it couples the type of the
 * SCM (the provider name) with the {@link ScmProviderRepository} holding the provider specific settings
 * parsed from the SCM URL (location, credentials and the like). Instances are created by
 * {@link org.apache.maven.scm.manager.ScmManager#makeScmRepository(String)}.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class ScmRepository {
    private String provider;

    private ScmProviderRepository providerRepository;

    public ScmRepository(String provider, ScmProviderRepository providerRepository) {
        this.provider = provider;

        this.providerRepository = providerRepository;
    }

    /**
     * The SCM provider type, such as "svn", "git", etc.
     * A value as returned by {@link org.apache.maven.scm.provider.ScmProvider#getScmType()}.
     * @return returns the provider type
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @return returns the provider repository
     */
    public ScmProviderRepository getProviderRepository() {
        return providerRepository;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return provider + ":" + providerRepository;
    }
}
