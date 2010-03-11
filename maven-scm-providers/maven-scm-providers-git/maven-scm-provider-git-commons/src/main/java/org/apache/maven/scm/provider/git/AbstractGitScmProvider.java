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
import org.apache.maven.scm.command.blame.BlameScmResult;
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

    /**
     * Internal class
     */
    private static class ScmUrlParserResult
    {
        private List messages = new ArrayList();

        private ScmProviderRepository repository;
    }

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public String getScmSpecificFilename()
    {
        return ".git";
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        try
        {
            ScmUrlParserResult result = parseScmUrl( scmSpecificUrl, delimiter );
    
            if ( result.messages.size() > 0 )
            {
                throw new ScmRepositoryException( "The scm url is invalid.", result.messages );
            }
    
            return result.repository;
        }
        catch ( ScmException e )
        {
            // XXX We should allow throwing of SCMException.
            throw new ScmRepositoryException( "Error creating the scm repository", e );
        }
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
            throw new ScmRepositoryException( "Error creating the scm repository", e );
        }
    }

    protected abstract String getRepositoryURL( File path )
        throws ScmException;

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
    private ScmUrlParserResult parseScmUrl( String scmSpecificUrl, char delimiter )
        throws ScmException
    {
        ScmUrlParserResult result = new ScmUrlParserResult();

        result.repository = new GitScmProviderRepository( scmSpecificUrl );

        return result;
    }

    protected abstract GitCommand getAddCommand();

    /** {@inheritDoc} */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (AddScmResult) executeCommand( getAddCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getBranchCommand();

    /** {@inheritDoc} */
    protected BranchScmResult branch( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        return (BranchScmResult) executeCommand( getBranchCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getChangeLogCommand();

    /** {@inheritDoc} */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        return (ChangeLogScmResult) executeCommand( getChangeLogCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getCheckInCommand();

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        return (CheckInScmResult) executeCommand( getCheckInCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getCheckOutCommand();

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        return (CheckOutScmResult) executeCommand( getCheckOutCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getDiffCommand();

    /** {@inheritDoc} */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (DiffScmResult) executeCommand( getDiffCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getExportCommand();

    /** {@inheritDoc} */
    protected ExportScmResult export( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        return (ExportScmResult) executeCommand( getExportCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getRemoveCommand();

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (RemoveScmResult) executeCommand( getRemoveCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getStatusCommand();

    /** {@inheritDoc} */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) executeCommand( getStatusCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getTagCommand();

    /** {@inheritDoc} */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (TagScmResult) executeCommand( getTagCommand(), repository, fileSet, parameters );
    }

    protected abstract GitCommand getUpdateCommand();

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        GitCommand cmd = getBlameCommand();

        return (BlameScmResult) executeCommand( cmd, repository, fileSet, parameters );
    }

    protected abstract GitCommand getBlameCommand();

}
