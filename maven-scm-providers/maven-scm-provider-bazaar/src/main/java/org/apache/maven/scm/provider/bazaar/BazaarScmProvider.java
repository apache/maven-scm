package org.apache.maven.scm.provider.bazaar;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.command.BazaarConstants;
import org.apache.maven.scm.provider.bazaar.command.add.BazaarAddCommand;
import org.apache.maven.scm.provider.bazaar.command.blame.BazaarBlameCommand;
import org.apache.maven.scm.provider.bazaar.command.changelog.BazaarChangeLogCommand;
import org.apache.maven.scm.provider.bazaar.command.checkin.BazaarCheckInCommand;
import org.apache.maven.scm.provider.bazaar.command.checkout.BazaarCheckOutCommand;
import org.apache.maven.scm.provider.bazaar.command.diff.BazaarDiffCommand;
import org.apache.maven.scm.provider.bazaar.command.remove.BazaarRemoveCommand;
import org.apache.maven.scm.provider.bazaar.command.status.BazaarStatusCommand;
import org.apache.maven.scm.provider.bazaar.command.tag.BazaarTagCommand;
import org.apache.maven.scm.provider.bazaar.command.update.BazaarUpdateCommand;
import org.apache.maven.scm.provider.bazaar.repository.BazaarScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Bazaar NG http://bazaar-vcs.org/ is a decentralized revision control system. <br>
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="bazaar"
 */
public class BazaarScmProvider
    extends AbstractScmProvider
{
    /** {@inheritDoc} */
    public String getScmSpecificFilename()
    {
        return ".bzr";
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        return new BazaarScmProviderRepository( scmSpecificUrl );
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        if ( path == null || !path.isDirectory() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a valid directory." );
        }

        File bzrDir = new File( path, ".bzr" );

        if ( !bzrDir.exists() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a bazaar directory." );
        }

        return makeProviderScmRepository( "file:///" + path.getAbsolutePath(), ':' );
    }

    /** {@inheritDoc} */
    public List<String> validateScmUrl( String scmSpecificUrl, char delimiter )
    {

        List<String> errorMessages = new ArrayList<String>();

        String[] checkCmd = new String[]{BazaarConstants.CHECK, scmSpecificUrl};
        ScmResult result;
        try
        {
            File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
            result = BazaarUtils.execute( tmpDir, checkCmd );
            if ( !result.isSuccess() )
            {
                errorMessages.add( result.getCommandOutput() );
                errorMessages.add( result.getProviderMessage() );
            }
        }
        catch ( ScmException e )
        {
            errorMessages.add( e.getMessage() );
        }

        return errorMessages;
    }

    /** {@inheritDoc} */
    public String getScmType()
    {
        return "bazaar";
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        BazaarAddCommand command = new BazaarAddCommand();

        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        BazaarChangeLogCommand command = new BazaarChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        BazaarCheckInCommand command = new BazaarCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        BazaarCheckOutCommand command = new BazaarCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        BazaarDiffCommand command = new BazaarDiffCommand();

        command.setLogger( getLogger() );

        return (DiffScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        BazaarRemoveCommand command = new BazaarRemoveCommand();

        command.setLogger( getLogger() );

        return (RemoveScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        BazaarStatusCommand command = new BazaarStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        BazaarTagCommand command = new BazaarTagCommand();

        command.setLogger( getLogger() );

        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        BazaarUpdateCommand command = new BazaarUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        BazaarBlameCommand command = new BazaarBlameCommand();

        command.setLogger( getLogger() );

        return (BlameScmResult) command.execute( repository, fileSet, parameters );
    }
}