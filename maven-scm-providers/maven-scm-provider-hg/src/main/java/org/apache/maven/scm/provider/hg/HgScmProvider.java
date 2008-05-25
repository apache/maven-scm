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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.command.tag.HgTagCommand;
import org.apache.maven.scm.provider.hg.command.add.HgAddCommand;
import org.apache.maven.scm.provider.hg.command.changelog.HgChangeLogCommand;
import org.apache.maven.scm.provider.hg.command.checkin.HgCheckInCommand;
import org.apache.maven.scm.provider.hg.command.checkout.HgCheckOutCommand;
import org.apache.maven.scm.provider.hg.command.diff.HgDiffCommand;
import org.apache.maven.scm.provider.hg.command.remove.HgRemoveCommand;
import org.apache.maven.scm.provider.hg.command.status.HgStatusCommand;
import org.apache.maven.scm.provider.hg.command.update.HgUpdateCommand;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mercurial (HG) is a decentralized revision control system.
 * <a href="http://www.selenic.com/mercurial">http://www.selenic.com/mercurial</a>
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider"
 * role-hint="hg"
 */
public class HgScmProvider
    extends AbstractScmProvider
{
    public String getScmSpecificFilename()
    {
        return ".hg";
    }

    private static class HgUrlParserResult
    {
        List messages = new ArrayList();

        ScmProviderRepository repository;
    }


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
        } else {
            try {

                File file = new File(url);

            } catch (Throwable e) {
                result.messages.add( "The filename provided is not valid" );

                return result;
            }

        }

        result.repository = new HgScmProviderRepository( url );

        return result;
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#makeProviderScmRepository(java.io.File)
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        if ( path == null || !path.isDirectory() )
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

    
    /**
     * Validate the scm url.
     *
     * @param scmSpecificUrl The SCM url
     * @param delimiter      The delimiter used in the SCM url
     * @return Returns a list of messages if the validation failed
     */ 
    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        HgUrlParserResult result = parseScmUrl( scmSpecificUrl );

        return result.messages;
    }

    public String getScmType()
    {
        return "hg";
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#add(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgAddCommand command = new HgAddCommand();

        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        HgChangeLogCommand command = new HgChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkin(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        HgCheckInCommand command = new HgCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkout(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        HgCheckOutCommand command = new HgCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkout(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        HgTagCommand command = new HgTagCommand();

        command.setLogger( getLogger() );

        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }


    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#diff(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgDiffCommand command = new HgDiffCommand();

        command.setLogger( getLogger() );

        return (DiffScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#remove(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgRemoveCommand command = new HgRemoveCommand();

        command.setLogger( getLogger() );

        return (RemoveScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#status(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgStatusCommand command = new HgStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#update(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgUpdateCommand command = new HgUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }
}
