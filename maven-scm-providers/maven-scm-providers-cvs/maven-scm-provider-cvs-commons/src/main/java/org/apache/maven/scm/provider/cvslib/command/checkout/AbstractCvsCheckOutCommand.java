package org.apache.maven.scm.provider.cvslib.command.checkout;

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
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.IOException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public abstract class AbstractCvsCheckOutCommand
    extends AbstractCheckOutCommand
    implements CvsCommand
{

    /**
     * The overriden {@link #executeCommand(ScmProviderRepository, ScmFileSet, CommandParameters)}
     * in this class will not call this method!
     * <p>
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
        throw new UnsupportedOperationException( "Should not get here" );
    }

    @Override
    public ScmResult executeCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        ScmVersion version = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );
        boolean binary = parameters.getBoolean( CommandParameter.BINARY, false );

        if ( fileSet.getBasedir().exists() )
        {
            try
            {
                FileUtils.deleteDirectory( fileSet.getBasedir() );
            }
            catch ( IOException e )
            {
                if ( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( "Can't delete " + fileSet.getBasedir().getAbsolutePath(), e );
                }
            }
        }

        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "checkout", repository, fileSet );

        cl.setWorkingDirectory( fileSet.getBasedir().getParentFile().getAbsolutePath() );

        if ( binary )
        {
            cl.createArg().setValue( "-kb" );
        }

        if ( version != null && !StringUtils.isEmpty( version.getName() ) )
        {
            cl.createArg().setValue( "-r" );
            cl.createArg().setValue( version.getName() );
        }

        cl.createArg().setValue( "-d" );

        cl.createArg().setValue( fileSet.getBasedir().getName() );

        cl.createArg().setValue( repository.getModule() );

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + cl );
            getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        return executeCvsCommand( cl );
    }

    protected abstract CheckOutScmResult executeCvsCommand( Commandline cl )
        throws ScmException;
}
