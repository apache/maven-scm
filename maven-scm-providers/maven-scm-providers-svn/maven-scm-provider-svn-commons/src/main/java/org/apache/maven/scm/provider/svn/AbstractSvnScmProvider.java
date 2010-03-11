package org.apache.maven.scm.provider.svn;

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
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.info.SvnInfoScmResult;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.util.SvnUtil;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * SCM Provider for Subversion
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractSvnScmProvider
    extends AbstractScmProvider
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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
        return ".svn";
    }

    /** {@inheritDoc} */
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

        if ( !new File( path, ".svn" ).exists() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a svn checkout directory." );
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
        return "svn";
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ScmUrlParserResult parseScmUrl( String scmSpecificUrl )
    {
        ScmUrlParserResult result = new ScmUrlParserResult();

        String url = scmSpecificUrl;

        // ----------------------------------------------------------------------
        // Do some sanity checking of the SVN url
        // ----------------------------------------------------------------------

        if ( url.startsWith( "file" ) )
        {
            if ( !url.startsWith( "file://" ) )
            {
                result.messages.add( "A svn 'file' url must be on the form 'file://[hostname]/'." );

                return result;
            }
        }
        else if ( url.startsWith( "https" ) )
        {
            if ( !url.startsWith( "https://" ) )
            {
                result.messages.add( "A svn 'http' url must be on the form 'https://'." );

                return result;
            }
        }
        else if ( url.startsWith( "http" ) )
        {
            if ( !url.startsWith( "http://" ) )
            {
                result.messages.add( "A svn 'http' url must be on the form 'http://'." );

                return result;
            }
        }
        // Support of tunnels: svn+xxx with xxx defined in subversion conf file
        else if ( url.startsWith( "svn+" ) )
        {
            if ( url.indexOf( "://" ) < 0 )
            {
                result.messages.add( "A svn 'svn+xxx' url must be on the form 'svn+xxx://'." );

                return result;
            }
            else
            {
                String tunnel = url.substring( "svn+".length(), url.indexOf( "://" ) );

                //ssh is always an allowed tunnel
                if ( !"ssh".equals( tunnel ) )
                {
                    SvnConfigFileReader reader = new SvnConfigFileReader();
                    if ( SvnUtil.getSettings().getConfigDirectory() != null )
                    {
                        reader.setConfigDirectory( new File( SvnUtil.getSettings().getConfigDirectory() ) );
                    }

                    if ( StringUtils.isEmpty( reader.getProperty( "tunnels", tunnel ) ) )
                    {
                        result.messages.add(
                            "The tunnel '" + tunnel + "' isn't defined in your subversion configuration file." );

                        return result;
                    }
                }
            }
        }
        else if ( url.startsWith( "svn" ) )
        {
            if ( !url.startsWith( "svn://" ) )
            {
                result.messages.add( "A svn 'svn' url must be on the form 'svn://'." );

                return result;
            }
        }
        else
        {
            result.messages.add( url + " url isn't a valid svn URL." );

            return result;
        }

        result.repository = new SvnScmProviderRepository( url );

        return result;
    }

    protected abstract SvnCommand getAddCommand();

    /** {@inheritDoc} */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (AddScmResult) executeCommand( getAddCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getBranchCommand();

    /** {@inheritDoc} */
    protected BranchScmResult branch( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        return (BranchScmResult) executeCommand( getBranchCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getChangeLogCommand();

    /** {@inheritDoc} */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        return (ChangeLogScmResult) executeCommand( getChangeLogCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getCheckInCommand();

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        return (CheckInScmResult) executeCommand( getCheckInCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getCheckOutCommand();

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        return (CheckOutScmResult) executeCommand( getCheckOutCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getDiffCommand();

    /** {@inheritDoc} */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (DiffScmResult) executeCommand( getDiffCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getExportCommand();

    /** {@inheritDoc} */
    protected ExportScmResult export( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        return (ExportScmResult) executeCommand( getExportCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getRemoveCommand();

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (RemoveScmResult) executeCommand( getRemoveCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getStatusCommand();

    /** {@inheritDoc} */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) executeCommand( getStatusCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getTagCommand();

    /** {@inheritDoc} */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (TagScmResult) executeCommand( getTagCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getUpdateCommand();

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (UpdateScmResult) executeCommand( getUpdateCommand(), repository, fileSet, parameters );
    }

    protected ScmResult executeCommand( SvnCommand command, ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        command.setLogger( getLogger() );

        return command.execute( repository, fileSet, parameters );
    }

    protected abstract SvnCommand getListCommand();

    /** {@inheritDoc} */
    public ListScmResult list( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SvnCommand cmd = getListCommand();

        return (ListScmResult) executeCommand( cmd, repository, fileSet, parameters );
    }

    protected abstract SvnCommand getInfoCommand();

    public SvnInfoScmResult info( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SvnCommand cmd = getInfoCommand();

        return (SvnInfoScmResult) executeCommand( cmd, repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SvnCommand cmd = getBlameCommand();

        return (BlameScmResult) executeCommand( cmd, repository, fileSet, parameters );
    }

    protected abstract SvnCommand getBlameCommand();
}
