package org.apache.maven.scm.manager;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface ScmManager
{
    String ROLE = ScmManager.class.getName();

    // ----------------------------------------------------------------------
    // Repository
    // ----------------------------------------------------------------------

    /**
     * Generate a SCMRepository from a SCM url
     *
     * @param scmUrl the scm url
     * @return
     * @throws ScmRepositoryException
     * @throws NoSuchScmProviderException
     */
    ScmRepository makeScmRepository( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException;

    ScmRepository makeProviderScmRepository( String providerType, File path )
        throws ScmRepositoryException, UnknownRepositoryStructure, NoSuchScmProviderException;

    /**
     * Validate a SCM URL
     *
     * @param scmUrl the SCM URL to validate
     * @return <code>List</code> of <code>String</code> objects with the messages returned by the SCM provider
     */
    List validateScmRepository( String scmUrl );

    ScmProvider getProviderByUrl( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException;

    /**
     * Returns the default provider registered for this providerType or a specific implementation if the
     * 'maven.scm.provider.providerType.implementation' system proerty is defined.
     * For example:  maven.scm.provider.cvs.implementation=cvs_native
     *
     * @param providerType The provider type (cvs, svn...)
     * @return The scm provider
     * @throws NoSuchScmProviderException if the provider doesn't exist
     */
    ScmProvider getProviderByType( String providerType )
        throws NoSuchScmProviderException;

    ScmProvider getProviderByRepository( ScmRepository repository )
        throws NoSuchScmProviderException;
}
