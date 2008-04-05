package org.apache.maven.scm.provider.git;

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
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.command.info.GitInfoScmResult;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * SCM Provider for git
 * 
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="git"
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractGitScmProvider
    extends AbstractScmProvider
{

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static class ScmUrlParserResult
    {
        List messages = new ArrayList();

        ScmProviderRepository repository;
    }

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public String getScmSpecificFilename()
    {
        return ".git";
    }

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        ScmUrlParserResult result = parseScmUrl( scmSpecificUrl );

        if ( result.messages.size() > 0 )
        {
            throw new ScmRepositoryException( "The scm url is invalid.", result.messages );
        }

        return result.repository;
    }

    /**
     * This creates a local ScmProviderRepository for the given path
     * @see org.apache.maven.scm.provider.AbstractScmProvider#makeProviderScmRepository(java.io.File)
     */
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

        if ( !new File( path, ".git" ).exists() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a git checkout directory." );
        }

        try
        {
            return makeProviderScmRepository( getRepositoryURL( path ), ':' );
        }
        catch ( ScmException e )
        {
            // XXX We should allow throwing of SCMException.
            throw new ScmRepositoryException( "Error executing info command", e );
        }
    }

    protected abstract String getRepositoryURL( File path )
        throws ScmException;

    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        List messages = new ArrayList();
        try
        {
            makeProviderScmRepository( scmSpecificUrl, delimiter );
        }
        catch ( ScmRepositoryException e )
        {
            messages = e.getValidationMessages();
        }
        return messages;
    }

    public String getScmType()
    {
        return "git";
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * The git-submodule(1) command is available since Git 1.5.3, so modules will
     * be activated in a later stage
     */
    private ScmUrlParserResult parseScmUrl( String scmSpecificUrl )
    {
        ScmUrlParserResult result = new ScmUrlParserResult();

        String url = scmSpecificUrl;

        // ----------------------------------------------------------------------
        // Do some sanity checking of the git url
        // ----------------------------------------------------------------------

        if ( url.startsWith( GitScmProviderRepository.PROTOCOL_FILE ) )
        {
            if ( !url.startsWith( GitScmProviderRepository.PROTOCOL_FILE + "://" ) )
            {
                result.messages.add( "A git 'file' url must be on the form 'file://[hostname]/'." );

                return result;
            }
        }
        else if ( url.startsWith( GitScmProviderRepository.PROTOCOL_HTTPS ) )
        {
            if ( !url.startsWith( GitScmProviderRepository.PROTOCOL_HTTPS + "://" ) )
            {
                result.messages.add( "A git 'http' url must be on the form 'https://'." );

                return result;
            }
        }
        else if ( url.startsWith( GitScmProviderRepository.PROTOCOL_HTTP ) )
        {
            if ( !url.startsWith( GitScmProviderRepository.PROTOCOL_HTTP + "://" ) )
            {
                result.messages.add( "A git 'http' url must be on the form 'http://'." );

                return result;
            }
        }
        else if ( url.startsWith( GitScmProviderRepository.PROTOCOL_SSH ) )
        {
            if ( !url.startsWith( GitScmProviderRepository.PROTOCOL_SSH + "://" ) )
            {
                result.messages.add( "A git 'ssh' url must be on the form 'ssh://'." );

                return result;
            }
        }
        else if ( url.startsWith( GitScmProviderRepository.PROTOCOL_GIT ) )
        {
            if ( !url.startsWith( GitScmProviderRepository.PROTOCOL_GIT + "://" ) )
            {
                result.messages.add( "A git 'git' url must be on the form 'git://'." );

                return result;
            }
        }
        else
        {
            result.messages.add( url + " url isn't a valid git URL." );

            return result;
        }

        result.repository = new GitScmProviderRepository( url );

        return result;
    }

    protected abstract GitCommand getAddCommand();

    /**
     * @see AbstractScmProvider#add(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (AddScmResult) executeCommand( getAddCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getBranchCommand();

    /**
     * @see AbstractScmProvider#branch(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    protected BranchScmResult branch( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        return (BranchScmResult) executeCommand( getBranchCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getChangeLogCommand();

    /**
     * @see AbstractScmProvider#changelog(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        return (ChangeLogScmResult) executeCommand( getChangeLogCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getCheckInCommand();

    /**
     * @see AbstractScmProvider#checkin(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        return (CheckInScmResult) executeCommand( getCheckInCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getCheckOutCommand();

    /**
     * @see AbstractScmProvider#checkout(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        return (CheckOutScmResult) executeCommand( getCheckOutCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getDiffCommand();

    /**
     * @see AbstractScmProvider#diff(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (DiffScmResult) executeCommand( getDiffCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getExportCommand();

    protected ExportScmResult export( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        return (ExportScmResult) executeCommand( getExportCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getRemoveCommand();

    /**
     * @see AbstractScmProvider#remove(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (RemoveScmResult) executeCommand( getRemoveCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getStatusCommand();

    /**
     * @see AbstractScmProvider#status(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) executeCommand( getStatusCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getTagCommand();

    /**
     * @see AbstractScmProvider#tag(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (TagScmResult) executeCommand( getTagCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getUpdateCommand();

    /**
     * @see AbstractScmProvider#update(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (UpdateScmResult) executeCommand( getUpdateCommand(), repository, fileSet, parameters );
    }

    protected ScmResult executeCommand( GitCommand command, ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        command.setLogger( getLogger() );

        return command.execute( repository, fileSet, parameters );
    }

    protected abstract GitCommand getListCommand();

    /**
     * @see AbstractScmProvider#list(ScmProviderRepository,ScmFileSet,CommandParameters)
     */
    public ListScmResult list( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        GitCommand cmd = getListCommand();

        return (ListScmResult) executeCommand( cmd, repository, fileSet, parameters );
    }

    protected abstract GitCommand getInfoCommand();

    public GitInfoScmResult info( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        GitCommand cmd = getInfoCommand();

        return (GitInfoScmResult) executeCommand( cmd, repository, fileSet, parameters );
    }

}
