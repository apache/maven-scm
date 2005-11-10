package org.apache.maven.scm.provider.svn.command.update;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.SvnCommandLineUtils;
import org.apache.maven.scm.provider.svn.command.changelog.SvnChangeLogCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnUpdateCommand
    extends AbstractUpdateCommand
    implements SvnCommand
{

    private final static Set REVISION_SPECIFIERS = Collections.unmodifiableSet( new HashSet()
    {
        {
            add( "HEAD" );
            add( "BASE" );
            add( "COMMITTED" );
            add( "PREV" );
        }
    } );

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), tag );

        SvnUpdateConsumer consumer = new SvnUpdateConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new UpdateScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new SvnUpdateScmResult( cl.toString(), consumer.getUpdatedFiles(), consumer.getRevision() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory, String tag )
    {
        if ( tag != null && StringUtils.isEmpty( tag.trim() ) ) 
        {
            tag = null;
        }
        
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        if (tag == null  || isRevisionArgument( tag ) )
        {
            cl.createArgument().setValue( "update" );

            if ( tag != null )
            {
                cl.createArgument().setValue( "-r" );
                cl.createArgument().setValue( tag );
            }
        }
        else
        {
            // The tag specified does not appear to be numeric, so assume it refers 
            // to a branch/tag url and perform a switch operation rather than update
            cl.createArgument().setValue( "switch" );
            cl.createArgument().setValue( resolveTagURL( repository, tag ) );
            cl.createArgument().setValue( workingDirectory.getAbsolutePath() );
        }

        return cl;
    }

    /**
     * @see org.apache.maven.scm.command.update.AbstractUpdateCommand#getChangeLogCommand()
     */
    protected ChangeLogCommand getChangeLogCommand()
    {
        SvnChangeLogCommand command =  new SvnChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }

    private final static String[] SVN_BASE_DIRS = new String[] { "/trunk", "/branches", "/tags" };

    /* Returns the "base" repository url, where "base" is the root of
     * the /trunk, /branches, /tags directories
     *
     * This probably belongs in SvnScmProviderRepository rather than here.
     */
    static String getSVNBaseURL( SvnScmProviderRepository repository )
    {
        String repoPath = repository.getUrl();

        for ( int i = 0; i < SVN_BASE_DIRS.length; i++ )
        {
            String dir = SVN_BASE_DIRS[i];
            if ( repoPath.indexOf( dir ) >= 0 )
            {
                return repoPath.substring( 0, repoPath.indexOf( dir ) );
            }
        }

        // At this point we were unable to locate the "base" of this svn repository
        // so assume that the repository url is the "base"
        if ( repoPath.endsWith( "/" ) )
        {
            return repoPath.substring( 0, repoPath.length() - 1 );
        }
        else
        {
            return repoPath;
        }
    }

    /* Resolves a tag to a repository url. If the tag is relative, the tag is appended
     * to the "base" directory of the repository. 
     */
    static String resolveTagURL( SvnScmProviderRepository repository, String tag )
    {
        if ( tag == null )
        {
            return null;
        }

        if ( tag.indexOf( "://" ) >= 0 )
        {
            // tag is already an absolute url so just return it. 
            return tag;
        }

        // Tag must be relative so append it to the repositories base path
        return getSVNBaseURL( repository ) + "/" + tag;
    }

    /* Returns whether the supplied tag refers to an
     * actual revision or is specifying a tag/branch url 
     * in the repository.  According to the subversion documentation, 
     * the following are valid revision specifiers:
     *  NUMBER       revision number
     *  "{" DATE "}" revision at start of the date
     *  "HEAD"       latest in repository
     *  "BASE"       base rev of item's working copy
     *  "COMMITTED"  last commit at or before BASE
     *  "PREV" 
     */
    private static boolean isRevisionArgument( String tag )
    {
        if ( StringUtils.isEmpty( tag ) || StringUtils.isEmpty( tag.trim() ) )
        {
            return false;
        }

        // attempt the revision NUMBER conversion and specifier lookup        
        if ( StringUtils.isNumeric( tag ) || REVISION_SPECIFIERS.contains( tag ) )
        {
            return true;
        }

        // lastly see if it appears to be a date specifier with 
        // the first char = '{' and last = '}'
        if ( tag.startsWith( "{" ) && tag.endsWith( "}" ) )
        {
            return true;
        }

        return false;
    }

}
