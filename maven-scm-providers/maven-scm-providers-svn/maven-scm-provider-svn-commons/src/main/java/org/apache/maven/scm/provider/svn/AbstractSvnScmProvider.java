package org.apache.maven.scm.provider.svn;

/*
 * Copyright 2003-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AddScmResult;
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
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.util.EntriesReader;
import org.apache.maven.scm.provider.svn.util.Entry;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

/**
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
        List messages = new ArrayList();

        ScmProviderRepository repository;
    }

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

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
     * @see org.apache.maven.scm.provider.AbstractScmProvider#makeProviderScmRepository(java.io.File)
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        if ( path == null || !path.isDirectory() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a valid directory." );
        }

        File svnDirectory = new File( path, ".svn" );

        if ( !svnDirectory.exists() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a svn checkout directory." );
        }

        File svnEntriesFile = new File( svnDirectory, "entries" );

        String svnUrl = null;

        try
        {
            FileReader reader = new FileReader( svnEntriesFile );

            EntriesReader entriesReader = new EntriesReader();

            List entries = entriesReader.read( reader );

            for ( Iterator i = entries.iterator(); i.hasNext(); )
            {
                Entry svnEntry = (Entry) i.next();

                if ( "".equals( svnEntry.getName() ) )
                {
                    svnUrl = svnEntry.getUrl();
                }
            }
        }
        catch ( Exception e )
        {
            ScmRepositoryException ex = new ScmRepositoryException( "Can't read " + svnEntriesFile.getAbsolutePath() );

            ex.setStackTrace( e.getStackTrace() );

            throw ex;
        }

        return makeProviderScmRepository( svnUrl, ':' );
    }


    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        List messages = new ArrayList();
        try
        {
            makeProviderScmRepository( scmSpecificUrl, delimiter );
        }
        catch( ScmRepositoryException e )
        {
            messages = e.getValidationMessages();
        }
        return messages;
    }

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

        String user = null;

        String password = null;

        String protocol = null;

        // ----------------------------------------------------------------------
        // Do some sanity checking of the SVN url
        // ----------------------------------------------------------------------

        if ( url.startsWith( "file" ) )
        {
            if ( !url.startsWith( "file:///" ) && !url.startsWith( "file://localhost/" ) )
            {
                result.messages.add( "A svn 'file' url must be on the form 'file:///' or 'file://localhost/'." );

                return result;
            }

            protocol = "file://";
        }
        else if ( url.startsWith( "https" ) )
        {
            if ( !url.startsWith( "https://" ) )
            {
                result.messages.add( "A svn 'http' url must be on the form 'https://'." );

                return result;
            }

            protocol = "https://";
        }
        else if ( url.startsWith( "http" ) )
        {
            if ( !url.startsWith( "http://" ) )
            {
                result.messages.add( "A svn 'http' url must be on the form 'http://'." );

                return result;
            }

            protocol = "http://";
        }
        else if ( url.startsWith( "svn+ssh" ) )
        {
            if ( !url.startsWith( "svn+ssh://" ) )
            {
                result.messages.add( "A svn 'svn+ssh' url must be on the form 'svn+ssh://'." );

                return result;
            }

            protocol = "svn+ssh://";
        }
        else if ( url.startsWith( "svn" ) )
        {
            if ( !url.startsWith( "svn://" ) )
            {
                result.messages.add( "A svn 'svn' url must be on the form 'svn://'." );

                return result;
            }

            protocol = "svn://";
        }
        else
        {
            result.messages.add( url + " url isn't a valid svn URL." );

            return result;
        }

        String urlPath = url.substring( protocol.length() );

        int indexAt = urlPath.indexOf( "@" );

        if ( indexAt > 0 && !"svn+ssh://".equals( protocol ) )
        {
            user = urlPath.substring( 0, indexAt );

            url = protocol + urlPath.substring( indexAt + 1 );
        }
        else
        {
            url = protocol + urlPath;
        }

        result.repository = new SvnScmProviderRepository( url, user, password );

        return result;
    }

    protected abstract SvnCommand getAddCommand();
    
    /**
     * @see AbstractScmProvider#add(ScmRepository, ScmFileSet, CommandParameters)
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (AddScmResult) executeCommand( getAddCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getChangeLogCommand();
    
    /**
     * @see AbstractScmProvider#changelog(ScmRepository, ScmFileSet, CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (ChangeLogScmResult) executeCommand( getChangeLogCommand(), repository, fileSet, parameters );
    }
    
    protected abstract SvnCommand getCheckInCommand();

    /**
     * @see AbstractScmProvider#checkin(ScmRepository, ScmFileSet, CommandParameters)
     */
    public CheckInScmResult checkin( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (CheckInScmResult) executeCommand( getCheckInCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getCheckOutCommand();
    
    /**
     * @see AbstractScmProvider#checkout(ScmRepository, ScmFileSet, CommandParameters)
     */
    public CheckOutScmResult checkout( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (CheckOutScmResult) executeCommand( getCheckOutCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getDiffCommand();
    
    /**
     * @see AbstractScmProvider#diff(ScmRepository, ScmFileSet, CommandParameters)
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (DiffScmResult) executeCommand( getDiffCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getRemoveCommand();
    
    /**
     * @see AbstractScmProvider#remove(ScmRepository, ScmFileSet, CommandParameters)
     */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (RemoveScmResult) executeCommand( getRemoveCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getStatusCommand();
    
    /**
     * @see AbstractScmProvider#status(ScmRepository, ScmFileSet, CommandParameters)
     */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) executeCommand( getStatusCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getTagCommand();
    
    /**
     * @see AbstractScmProvider#tag(ScmRepository, ScmFileSet, CommandParameters)
     */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (TagScmResult) executeCommand( getTagCommand(), repository, fileSet, parameters );
    }

    protected abstract SvnCommand getUpdateCommand();
    
    /**
     * @see AbstractScmProvider#update(ScmRepository, ScmFileSet, CommandParameters)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (UpdateScmResult) executeCommand( getUpdateCommand(), repository, fileSet, parameters );
    }
    
    protected ScmResult executeCommand( SvnCommand command, ScmRepository repository, ScmFileSet fileSet,
                                    CommandParameters parameters )
        throws ScmException
    {
        command.setLogger( getLogger() );

        return command.execute( repository.getProviderRepository(), fileSet, parameters );
    }
}
