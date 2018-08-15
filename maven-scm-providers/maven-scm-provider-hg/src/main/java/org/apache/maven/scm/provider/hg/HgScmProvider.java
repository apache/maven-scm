package org.apache.maven.scm.provider.hg;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.command.add.HgAddCommand;
import org.apache.maven.scm.provider.hg.command.blame.HgBlameCommand;
import org.apache.maven.scm.provider.hg.command.branch.HgBranchCommand;
import org.apache.maven.scm.provider.hg.command.changelog.HgChangeLogCommand;
import org.apache.maven.scm.provider.hg.command.checkin.HgCheckInCommand;
import org.apache.maven.scm.provider.hg.command.checkout.HgCheckOutCommand;
import org.apache.maven.scm.provider.hg.command.diff.HgDiffCommand;
import org.apache.maven.scm.provider.hg.command.info.HgInfoCommand;
import org.apache.maven.scm.provider.hg.command.inventory.HgListCommand;
import org.apache.maven.scm.provider.hg.command.remove.HgRemoveCommand;
import org.apache.maven.scm.provider.hg.command.status.HgStatusCommand;
import org.apache.maven.scm.provider.hg.command.tag.HgTagCommand;
import org.apache.maven.scm.provider.hg.command.update.HgUpdateCommand;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

/**
 * Mercurial (HG) is a decentralized revision control system.
 * <a href="http://www.selenic.com/mercurial">http://www.selenic.com/mercurial</a>
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 *
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider"
 * role-hint="hg"
 */
public class HgScmProvider
    extends AbstractScmProvider
{
    /** {@inheritDoc} */
    public String getScmSpecificFilename()
    {
        return ".hg";
    }

    private static class HgUrlParserResult
    {
        private List<String> messages = new ArrayList<String>();

        private ScmProviderRepository repository;
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        HgUrlParserResult result = parseScmUrl( scmSpecificUrl );

        if ( result.messages.size() > 0 )
        {
            throw new ScmRepositoryException( "The scm url is invalid.", result.messages );
        }

        return result.repository;
    }

    private HgUrlParserResult parseScmUrl( String scmSpecificUrl )
    {
        HgUrlParserResult result = new HgUrlParserResult();

        String url = scmSpecificUrl;

        // ----------------------------------------------------------------------
        // Do some sanity checking of the SVN url
        // ----------------------------------------------------------------------

        if ( url.startsWith( "file" ) )
        {
            if ( !url.startsWith( "file:///" ) && !url.startsWith( "file://localhost/" ) )
            {
                result.messages.add( "An hg 'file' url must be on the form 'file:///' or 'file://localhost/'." );

                return result;
            }
        }
        else if ( url.startsWith( "https" ) )
        {
            if ( !url.startsWith( "https://" ) )
            {
                result.messages.add( "An hg 'http' url must be on the form 'https://'." );

                return result;
            }
        }
        else if ( url.startsWith( "http" ) )
        {
            if ( !url.startsWith( "http://" ) )
            {
                result.messages.add( "An hg 'http' url must be on the form 'http://'." );

                return result;
            }
        }
        else
        {
            try
            {
                @SuppressWarnings( "unused" )
                File file = new File( url );
            }
            catch ( Throwable e )
            {
                result.messages.add( "The filename provided is not valid" );

                return result;
            }

        }

        result.repository = new HgScmProviderRepository( url );

        return result;
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        if ( path == null )
        {
            throw new NullPointerException( "Path argument is null" );
        }

        if ( !path.isDirectory() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a valid directory." );
        }

        File hgDir = new File( path, ".hg" );

        if ( !hgDir.exists() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a hg directory." );
        }

        return makeProviderScmRepository( path.getAbsolutePath(), ':' );
    }

    /** {@inheritDoc} */
    public List<String> validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        HgUrlParserResult result = parseScmUrl( scmSpecificUrl );

        return result.messages;
    }

    /** {@inheritDoc} */
    public String getScmType()
    {
        return "hg";
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgAddCommand command = new HgAddCommand();

        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        HgChangeLogCommand command = new HgChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        HgCheckInCommand command = new HgCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        HgCheckOutCommand command = new HgCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgTagCommand command = new HgTagCommand();

        command.setLogger( getLogger() );

        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgDiffCommand command = new HgDiffCommand();

        command.setLogger( getLogger() );

        return (DiffScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet,
                                   CommandParameters parameters )
        throws ScmException
    {
        HgRemoveCommand command = new HgRemoveCommand();

        command.setLogger( getLogger() );

        return (RemoveScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet,
                                   CommandParameters parameters )
        throws ScmException
    {
        HgStatusCommand command = new HgStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet,
                                   CommandParameters parameters )
        throws ScmException
    {
        HgUpdateCommand command = new HgUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet,
                                    CommandParameters parameters )
        throws ScmException
    {
        HgBlameCommand command = new HgBlameCommand();

        command.setLogger( getLogger() );

        return (BlameScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public BranchScmResult branch( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgBranchCommand command = new HgBranchCommand();

        command.setLogger( getLogger() );

        return (BranchScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @since 1.5
     */
    @Override
    protected ListScmResult list( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgListCommand hgListCommand = new HgListCommand();
        hgListCommand.setLogger( getLogger() );
        return (ListScmResult) hgListCommand.executeCommand( repository, fileSet, parameters );

    }

    /**
     * returns result of hg id -i
     * @since 1.5
     * @see org.apache.maven.scm.provider.AbstractScmProvider#info(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    @Override
    public InfoScmResult info( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgInfoCommand infoCommand = new HgInfoCommand();
        infoCommand.setLogger( getLogger() );
        return (InfoScmResult) infoCommand.execute( repository, fileSet, parameters );
    }
}
