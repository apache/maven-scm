package org.apache.maven.scm.repository;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public final class ScmRepository
{
    /** */
    private String provider;

    /** */
    private String scmSpecificUrl;

    /** */
    private ScmProviderRepository providerRepository;

    public ScmRepository( String provider, String scmSpecificUrl, ScmProviderRepository providerRepository )
    {
        this.provider = provider;

        this.scmSpecificUrl = scmSpecificUrl;

        this.providerRepository = providerRepository;
    }

    /**
     * @return Returns the provider.
     */
    public String getProvider()
    {
        return provider;
    }

    /**
     * @return Returns the scmSpecificUrl.
     */
    public String getScmSpecificUrl2()
    {
        return scmSpecificUrl;
    }

    /**
     * @return Returns the provider repository.
     */
    public ScmProviderRepository getProviderRepository()
    {
        return providerRepository;
    }
}
