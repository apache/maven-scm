package org.apache.maven.scm.command.checkout;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 *
 */
public abstract class AbstractCheckOutCommand
    extends AbstractCommand
{
    /**
     * Execute Check out command line in a recursive check out way.
     *
     * @param repository not null
     * @param fileSet not null
     * @param scmVersion not null
     * @return the checkout result
     * @throws ScmException if any
     * @see #executeCheckOutCommand(ScmProviderRepository, ScmFileSet, ScmVersion, boolean, boolean)
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                                 ScmVersion scmVersion )
        throws ScmException
    {
        return executeCheckOutCommand( repository, fileSet, scmVersion, true, false );
    }

    /**
     * Execute Check out command line.
     *
     * @param repository not null
     * @param fileSet not null
     * @param scmVersion not null
     * @param recursive <code>true</code> if recursive check out is wanted, <code>false</code> otherwise.
     * @param shallow <code>true</code> if shallow check out is wanted, <code>false</code> otherwise.
     * @return the checkout result
     * @throws ScmException if any
     * @since 1.1.1
     */
    protected abstract CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                                ScmVersion scmVersion, boolean recursive,
                                                                boolean shallow )
        throws ScmException;

    /** {@inheritDoc} */
    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        ScmVersion scmVersion = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );
        boolean recursive = parameters.getBoolean( CommandParameter.RECURSIVE, true );
        boolean shallow = parameters.getBoolean( CommandParameter.SHALLOW, false );
        return executeCheckOutCommand( repository, fileSet, scmVersion, recursive, shallow);
    }
}
