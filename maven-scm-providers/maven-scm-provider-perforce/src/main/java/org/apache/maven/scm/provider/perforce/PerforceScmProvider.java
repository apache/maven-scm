package org.apache.maven.scm.provider.perforce;

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
import org.apache.maven.scm.NoSuchCommandScmException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.command.PerforceInfoCommand;
import org.apache.maven.scm.provider.perforce.command.PerforceWhereCommand;
import org.apache.maven.scm.provider.perforce.command.add.PerforceAddCommand;
import org.apache.maven.scm.provider.perforce.command.blame.PerforceBlameCommand;
import org.apache.maven.scm.provider.perforce.command.changelog.PerforceChangeLogCommand;
import org.apache.maven.scm.provider.perforce.command.checkin.PerforceCheckInCommand;
import org.apache.maven.scm.provider.perforce.command.checkout.PerforceCheckOutCommand;
import org.apache.maven.scm.provider.perforce.command.diff.PerforceDiffCommand;
import org.apache.maven.scm.provider.perforce.command.edit.PerforceEditCommand;
import org.apache.maven.scm.provider.perforce.command.login.PerforceLoginCommand;
import org.apache.maven.scm.provider.perforce.command.remove.PerforceRemoveCommand;
import org.apache.maven.scm.provider.perforce.command.status.PerforceStatusCommand;
import org.apache.maven.scm.provider.perforce.command.tag.PerforceTagCommand;
import org.apache.maven.scm.provider.perforce.command.unedit.PerforceUnEditCommand;
import org.apache.maven.scm.provider.perforce.command.update.PerforceUpdateCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @author mperham
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="perforce"
 */
public class PerforceScmProvider
    extends AbstractScmProvider
{
    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public boolean requiresEditMode()
    {
        return true;
    }

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        String path;
        int port = 0;
        String host = null;

        int i1 = scmSpecificUrl.indexOf( delimiter );
        int i2 = scmSpecificUrl.indexOf( delimiter, i1 + 1 );

        if ( i1 > 0 )
        {
            int lastDelimiter = scmSpecificUrl.lastIndexOf( delimiter );
            path = scmSpecificUrl.substring( lastDelimiter + 1 );
            host = scmSpecificUrl.substring( 0, i1 );

            // If there is tree parts in the scm url, the second is the port
            if ( i2 >= 0 )
            {
                try
                {
                    String tmp = scmSpecificUrl.substring( i1 + 1, lastDelimiter );
                    port = Integer.parseInt( tmp );
                }
                catch ( NumberFormatException ex )
                {
                    throw new ScmRepositoryException( "The port has to be a number." );
                }
            }
        }
        else
        {
            path = scmSpecificUrl;
        }

        String user = null;
        String password = null;
        if ( host != null && host.indexOf( "@" ) > 1 )
        {
            user = host.substring( 0, host.indexOf( "@" ) );
            host = host.substring( host.indexOf( "@" ) + 1 );
        }

        if ( path.indexOf( "@" ) > 1 )
        {
            if ( host != null )
            {
                if ( getLogger().isWarnEnabled() )
                {
                    getLogger().warn(
                                      "Username as part of path is deprecated, the new format is "
                                          + "scm:perforce:[username@]host:port:path_to_repository" );
                }
            }

            user = path.substring( 0, path.indexOf( "@" ) );
            path = path.substring( path.indexOf( "@" ) + 1 );
        }

        return new PerforceScmProviderRepository( host, port, path, user, password );
    }

    public String getScmType()
    {
        return "perforce";
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                            CommandParameters parameters )
        throws ScmException
    {
        PerforceChangeLogCommand command = new PerforceChangeLogCommand();
        command.setLogger( getLogger() );
        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    protected AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceAddCommand command = new PerforceAddCommand();
        command.setLogger( getLogger() );
        return (AddScmResult) command.execute( repository, fileSet, params );
    }

    protected RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceRemoveCommand command = new PerforceRemoveCommand();
        command.setLogger( getLogger() );
        return (RemoveScmResult) command.execute( repository, fileSet, params );
    }

    protected CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceCheckInCommand command = new PerforceCheckInCommand();
        command.setLogger( getLogger() );
        return (CheckInScmResult) command.execute( repository, fileSet, params );
    }

    protected CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                          CommandParameters params )
        throws ScmException
    {
        PerforceCheckOutCommand command = new PerforceCheckOutCommand();
        command.setLogger( getLogger() );
        return (CheckOutScmResult) command.execute( repository, fileSet, params );
    }

    protected DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceDiffCommand command = new PerforceDiffCommand();
        command.setLogger( getLogger() );
        return (DiffScmResult) command.execute( repository, fileSet, params );
    }

    protected EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceEditCommand command = new PerforceEditCommand();
        command.setLogger( getLogger() );
        return (EditScmResult) command.execute( repository, fileSet, params );
    }

    protected LoginScmResult login( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceLoginCommand command = new PerforceLoginCommand();
        command.setLogger( getLogger() );
        return (LoginScmResult) command.execute( repository, fileSet, params );
    }

    protected StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceStatusCommand command = new PerforceStatusCommand();
        command.setLogger( getLogger() );
        return (StatusScmResult) command.execute( repository, fileSet, params );
    }

    protected TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceTagCommand command = new PerforceTagCommand();
        command.setLogger( getLogger() );
        return (TagScmResult) command.execute( repository, fileSet, params );
    }

    protected UnEditScmResult unedit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceUnEditCommand command = new PerforceUnEditCommand();
        command.setLogger( getLogger() );
        return (UnEditScmResult) command.execute( repository, fileSet, params );
    }

    protected UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceUpdateCommand command = new PerforceUpdateCommand();
        command.setLogger( getLogger() );
        return (UpdateScmResult) command.execute( repository, fileSet, params );
    }

    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceBlameCommand command = new PerforceBlameCommand();
        command.setLogger( getLogger() );
        return (BlameScmResult) command.execute( repository, fileSet, params );
    }

    public static Commandline createP4Command( PerforceScmProviderRepository repo, File workingDir )
    {
        Commandline command = new Commandline();
        command.setExecutable( "p4" );
        if ( workingDir != null )
        {
            // SCM-209
            command.createArg().setValue( "-d" );
            command.createArg().setValue( workingDir.getAbsolutePath() );
        }

        if ( repo.getHost() != null )
        {
            command.createArg().setValue( "-p" );
            String value = repo.getHost();
            if ( repo.getPort() != 0 )
            {
                value += ":" + Integer.toString( repo.getPort() );
            }
            command.createArg().setValue( value );
        }

        if ( StringUtils.isNotEmpty( repo.getUser() ) )
        {
            command.createArg().setValue( "-u" );
            command.createArg().setValue( repo.getUser() );
        }

        if ( StringUtils.isNotEmpty( repo.getPassword() ) )
        {
            command.createArg().setValue( "-P" );
            command.createArg().setValue( repo.getPassword() );
        }
        return command;
    }

    public static String clean( String string )
    {
        if ( string.indexOf( " -P " ) == -1 )
        {
            return string;
        }
        int idx = string.indexOf( " -P " ) + 4;
        int end = string.indexOf( ' ', idx );
        return string.substring( 0, idx ) + StringUtils.repeat( "*", end - idx ) + string.substring( end );
    }

    /**
     * Given a path like "//depot/foo/bar", returns the
     * proper path to include everything beneath it.
     * <p/>
     * //depot/foo/bar -> //depot/foo/bar/...
     * //depot/foo/bar/ -> //depot/foo/bar/...
     * //depot/foo/bar/... -> //depot/foo/bar/...
     *
     * @param repoPath
     * @return
     */
    public static String getCanonicalRepoPath( String repoPath )
    {
        if ( repoPath.endsWith( "/..." ) )
        {
            return repoPath;
        }
        else if ( repoPath.endsWith( "/" ) )
        {
            return repoPath + "...";
        }
        else
        {
            return repoPath + "/...";
        }
    }

    private static final String NEWLINE = "\r\n";

    /*
     * Clientspec name can be overridden with the system property below.  I don't
     * know of any way for this code to get access to maven's settings.xml so this
     * is the best I can do.
     *
     * Sample clientspec:

     Client: mperham-mikeperham-dt-maven
     Root: d:\temp\target
     Owner: mperham
     View:
     //depot/sandbox/mperham/tsa/tsa-domain/... //mperham-mikeperham-dt-maven/...
     Description:
     Created by maven-scm-provider-perforce

     */
    public static String createClientspec( ScmLogger logger, PerforceScmProviderRepository repo, File workDir,
                                           String repoPath )
    {
        String clientspecName = getClientspecName( logger, repo, workDir );
        String userName = getUsername( logger, repo );

        String rootDir;
        try
        {
            // SCM-184
            rootDir = workDir.getCanonicalPath();
        }
        catch ( IOException ex )
        {
            //getLogger().error("Error getting canonical path for working directory: " + workDir, ex);
            rootDir = workDir.getAbsolutePath();
        }

        StringBuffer buf = new StringBuffer();
        buf.append( "Client: " ).append( clientspecName ).append( NEWLINE );
        buf.append( "Root: " ).append( rootDir ).append( NEWLINE );
        buf.append( "Owner: " ).append( userName ).append( NEWLINE );
        buf.append( "View:" ).append( NEWLINE );
        buf.append( "\t" ).append( PerforceScmProvider.getCanonicalRepoPath( repoPath ) );
        buf.append( " //" ).append( clientspecName ).append( "/..." ).append( NEWLINE );
        buf.append( "Description:" ).append( NEWLINE );
        buf.append( "\t" ).append( "Created by maven-scm-provider-perforce" ).append( NEWLINE );
        return buf.toString();
    }

    public static final String DEFAULT_CLIENTSPEC_PROPERTY = "maven.scm.perforce.clientspec.name";

    public static String getClientspecName( ScmLogger logger, PerforceScmProviderRepository repo, File workDir )
    {
        String def = generateDefaultClientspecName( logger, repo, workDir );
        // until someone put clearProperty in DefaultContinuumScm.getScmRepository( Project , boolean  )
        String l = System.getProperty( DEFAULT_CLIENTSPEC_PROPERTY, def );
        if ( l == null || "".equals( l.trim() ) )
        {
            return def;
        }
        return l;
    }

    private static String generateDefaultClientspecName( ScmLogger logger, PerforceScmProviderRepository repo,
                                                         File workDir )
    {
        String username = getUsername( logger, repo );
        String hostname;
        String path;
        try
        {
            hostname = InetAddress.getLocalHost().getHostName();
            // [SCM-370][SCM-351] client specs cannot contain forward slashes, spaces and ~; "-" is okay
            path = workDir.getCanonicalPath().replaceAll( "[/ ~]", "-" );
        }
        catch ( UnknownHostException e )
        {
            // Should never happen
            throw new RuntimeException( e );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
        return username + "-" + hostname + "-MavenSCM-" + path;
    }

    private static String getUsername( ScmLogger logger, PerforceScmProviderRepository repo )
    {
        String username = PerforceInfoCommand.getInfo( logger, repo ).getEntry( "User name" );
        if ( username == null )
        {
            // os user != perforce user
            username = repo.getUser();
            if ( username == null )
            {
                username = System.getProperty( "user.name", "nouser" );
            }
        }
        return username;
    }

    /**
     * This is a "safe" method which handles cases where repo.getPath() is
     * not actually a valid Perforce depot location.  This is a frequent error
     * due to branches and directory naming where dir name != artifactId.
     *
     * @param log     the logging object to use
     * @param repo    the Perforce repo
     * @param basedir the base directory we are operating in.  If pom.xml exists in this directory,
     *                this method will verify <pre>repo.getPath()/pom.xml</pre> == <pre>p4 where basedir/pom.xml</pre>
     * @return repo.getPath if it is determined to be accurate.  The p4 where location otherwise.
     */
    public static String getRepoPath( ScmLogger log, PerforceScmProviderRepository repo, File basedir )
    {
        PerforceWhereCommand where = new PerforceWhereCommand( log, repo );

        // Handle an edge case where we release:prepare'd a module with an invalid SCM location.
        // In this case, the release.properties will contain the invalid URL for checkout purposes
        // during release:perform.  In this case, the basedir is not the module root so we detect that
        // and remove the trailing target/checkout directory.
        if ( basedir.toString().replace( '\\', '/' ).endsWith( "/target/checkout" ) )
        {
            String dir = basedir.toString();
            basedir = new File( dir.substring( 0, dir.length() - "/target/checkout".length() ) );
            log.debug( "Fixing checkout URL: " + basedir );
        }
        File pom = new File( basedir, "pom.xml" );
        String loc = repo.getPath();
        log.debug( "SCM path in pom: " + loc );
        if ( pom.exists() )
        {
            loc = where.getDepotLocation( pom );
            if ( loc == null )
            {
                loc = repo.getPath();
                log.debug( "cannot find depot => using " + loc );
            }
            else if ( loc.endsWith( "/pom.xml" ) )
            {
                loc = loc.substring( 0, loc.length() - "/pom.xml".length() );
                log.debug( "Actual POM location: " + loc );
                if ( !repo.getPath().equals( loc ) )
                {
                    log.info( "The SCM location in your pom.xml (" + repo.getPath()
                        + ") is not equal to the depot location (" + loc
                        + ").  This happens frequently with branches.  " + "Ignoring the SCM location." );
                }
            }
        }
        return loc;
    }


    private static Boolean live = null;

    public static boolean isLive()
    {
        if ( live == null )
        {
            if ( !Boolean.getBoolean( "maven.scm.testing" ) )
            {
                // We are not executing in the tests so we are live.
                live = Boolean.TRUE;
            }
            else
            {
                // During unit tests, we need to check the local system
                // to see if the user has Perforce installed.  If not, we mark
                // the provider as "not live" (or dead, I suppose!) and skip
                // anything that requires an active server connection.
                try
                {
                    Commandline command = new Commandline();
                    command.setExecutable( "p4" );
                    Process proc = command.execute();
                    BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
                    String line;
                    while ( ( line = br.readLine() ) != null )
                    {
                        //System.out.println(line);
                    }
                    int rc = proc.exitValue();
                    live = ( rc == 0 ? Boolean.TRUE : Boolean.FALSE );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    live = Boolean.FALSE;
                }
            }
        }

        return live.booleanValue();
    }
}
