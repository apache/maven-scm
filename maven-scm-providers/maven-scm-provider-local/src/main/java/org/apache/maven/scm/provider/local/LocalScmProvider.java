package org.apache.maven.scm.provider.local;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.add.LocalAddCommand;
import org.apache.maven.scm.provider.local.command.changelog.LocalChangeLogCommand;
import org.apache.maven.scm.provider.local.command.checkin.LocalCheckInCommand;
import org.apache.maven.scm.provider.local.command.checkout.LocalCheckOutCommand;
import org.apache.maven.scm.provider.local.command.list.LocalListCommand;
import org.apache.maven.scm.provider.local.command.update.LocalUpdateCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="local"
 */
public class LocalScmProvider
    extends AbstractScmProvider
{
    /** {@inheritDoc} */
    public String getScmType()
    {
        return "local";
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        String[] tokens = StringUtils.split( scmSpecificUrl, Character.toString( delimiter ) );

        if ( tokens.length != 2 )
        {
            throw new ScmRepositoryException(
                "The connection string didn't contain the expected number of tokens. Expected 2 tokens but got " +
                    tokens.length + " tokens." );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String root = tokens[0];

        File rootFile = new File( root );

        if ( !rootFile.isAbsolute() )
        {
            String basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );

            rootFile = new File( basedir, root );
        }

        if ( !rootFile.exists() )
        {
            throw new ScmRepositoryException( "The root doesn't exists (" + rootFile.getAbsolutePath() + ")." );
        }

        if ( !rootFile.isDirectory() )
        {
            throw new ScmRepositoryException( "The root isn't a directory (" + rootFile.getAbsolutePath() + ")." );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String module = tokens[1];

        File moduleFile = new File( rootFile, module );

        if ( !moduleFile.exists() )
        {
            throw new ScmRepositoryException(
                "The module doesn't exist (root: " + rootFile.getAbsolutePath() + ", module: " + module + ")." );
        }

        if ( !moduleFile.isDirectory() )
        {
            throw new ScmRepositoryException( "The module isn't a directory." );
        }

        return new LocalScmProviderRepository( rootFile.getAbsolutePath(), module );
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static String fixModuleName( String module )
    {
        if ( module.endsWith( "/" ) )
        {
            module = module.substring( 0, module.length() - 1 );
        }

        if ( module.startsWith( "/" ) )
        {
            module = module.substring( 1 );
        }

        return module;
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        LocalAddCommand command = new LocalAddCommand();

        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                            CommandParameters parameters )
        throws ScmException
    {
        LocalChangeLogCommand command = new LocalChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        LocalCheckInCommand command = new LocalCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        LocalCheckOutCommand command = new LocalCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected ListScmResult list( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        LocalListCommand command = new LocalListCommand();

        command.setLogger( getLogger() );

        return (ListScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        LocalUpdateCommand command = new LocalUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }
}
