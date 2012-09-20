package org.apache.maven.scm.provider.cvslib.command.branch;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 *
 */
public abstract class AbstractCvsBranchCommand
    extends AbstractBranchCommand
    implements CvsCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeBranchCommand( ScmProviderRepository repo, ScmFileSet fileSet, String branchName,
                                           String message )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "tag", repository, fileSet, false );

        cl.createArg().setValue( "-b" );

        cl.createArg().setValue( "-F" );

        cl.createArg().setValue( "-c" );

        cl.createArg().setValue( branchName );

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + cl );
            getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        return executeCvsCommand( cl );
    }

    protected abstract BranchScmResult executeCvsCommand( Commandline cl )
        throws ScmException;
}
