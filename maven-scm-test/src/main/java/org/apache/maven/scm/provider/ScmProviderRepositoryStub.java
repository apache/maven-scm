package org.apache.maven.scm.provider;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
 */

/**
 * Stub for ScmProviderRepository for unit testing purposes.
 * More information about Stubs on <a href="http://martinfowler.com/bliki/TestDouble.html">Martin Fowler's TestDouble</a>
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ScmProviderRepositoryStub
    extends ScmProviderRepository
{

    private boolean persistCheckout = false;

    /**
     * Creates a ScmProviderRepositoryStub with null user and password, and persistCheckout false
     */
    public ScmProviderRepositoryStub()
    {
    }

    public boolean isPersistCheckout()
    {
        return persistCheckout;
    }

    public void setPersistCheckout( boolean persistCheckout )
    {
        this.persistCheckout = persistCheckout;
    }

}
