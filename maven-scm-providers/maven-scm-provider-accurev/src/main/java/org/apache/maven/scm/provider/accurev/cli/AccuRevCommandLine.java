package org.apache.maven.scm.provider.accurev.cli;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevStat;
import org.apache.maven.scm.provider.accurev.CategorisedElements;
import org.apache.maven.scm.provider.accurev.FileDifference;
import org.apache.maven.scm.provider.accurev.Stream;
import org.apache.maven.scm.provider.accurev.Transaction;
import org.apache.maven.scm.provider.accurev.WorkSpace;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class AccuRevCommandLine
    implements AccuRev
{

    private static final String[] EMPTY_STRING_ARRAY = new String[] {};

    private static final File CURRENT_DIR = new File( "." );

    private ScmLogger logger;

    private Commandline cl = new Commandline();

    private StreamConsumer systemErr;

    private StringBuffer commandLines = new StringBuffer();

    private StringBuffer errorOutput = new StringBuffer();

    private String[] hostArgs = EMPTY_STRING_ARRAY;

    private String[] authArgs = EMPTY_STRING_ARRAY;

    private String executable = "accurev";

    private long executableModTime;

    private String clientVersion;

    public AccuRevCommandLine()
    {

        super();
        reset();

    }

    public AccuRevCommandLine( String host, int port )
    {

        this();
        setServer( host, port );
    }

    public void setServer( String host, int port )
    {

        if ( host != null )
        {
            hostArgs = new String[] { "-H", host + ":" + port };
        }
        else
        {
            hostArgs = EMPTY_STRING_ARRAY;
        }

    }

    public void setExecutable( String accuRevExe )
    {

        executable = accuRevExe;
        reset();
    }

    private boolean executeCommandLine( File basedir, String[] args, Iterable<File> elements, Pattern matchPattern,
                                        List<File> matchedFiles )
        throws AccuRevException
    {

        FileConsumer stdoutConsumer = new FileConsumer( matchedFiles, matchPattern );

        return executeCommandLine( basedir, args, elements, stdoutConsumer );
    }

    private boolean executeCommandLine( File basedir, String[] args, Iterable<File> elements,
                                        StreamConsumer stdoutConsumer )
        throws AccuRevException
    {

        setWorkingDirectory( basedir );
        setCommandLineArgs( args );

        if ( elements != null )
        {
            for ( File file : elements )
            {
                String path = file.getPath();
                // Hack for Windows "/./". TODO find a nicer way to handle this.
                if ( "\\.".equals( path ) )
                {
                    path = "\\.\\";
                }
                cl.createArg().setValue( path );
            }
        }
        return executeCommandLine( null, stdoutConsumer ) == 0;
    }

    private void setCommandLineArgs( String[] args )
    {

        cl.clearArgs();

        if ( args.length > 0 )
        {
            // First arg is the accurev command
            cl.createArg().setValue( args[0] );

            // Inject -H <host:port> and -A <token> here
            cl.addArguments( hostArgs );
            cl.addArguments( authArgs );
        }

        for ( int i = 1; i < args.length; i++ )
        {
            cl.createArg().setValue( args[i] );
        }

    }

    private boolean executeCommandLine( String[] args )
        throws AccuRevException
    {

        return executeCommandLine( args, null, null ) == 0;
    }

    private int executeCommandLine( String[] args, InputStream stdin, StreamConsumer stdout )
        throws AccuRevException
    {

        setCommandLineArgs( args );

        return executeCommandLine( stdin, stdout );

    }

    private int executeCommandLine( InputStream stdin, StreamConsumer stdout )
        throws AccuRevException
    {

        commandLines.append( cl.toString() );
        commandLines.append( ';' );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( cl.toString() );
        }
        try
        {

            int result = executeCommandLine( cl, stdin, new CommandOutputConsumer( getLogger(), stdout ), systemErr );
            if ( result != 0 )
            {
                getLogger().debug( "Non zero result - " + result );
            }
            return result;
        }
        catch ( CommandLineException ex )
        {
            throw new AccuRevException( "Error executing command " + cl.toString(), ex );
        }

    }

    /**
     * Extracted so test class can override
     * 
     * @param stdin
     * @param stdout
     * @param stderr
     * @return
     * @throws CommandLineException
     */
    protected int executeCommandLine( Commandline cl, InputStream stdin, CommandOutputConsumer stdout,
                                      StreamConsumer stderr )
        throws CommandLineException
    {

        int result = CommandLineUtils.executeCommandLine( cl, stdin, stdout, stderr );
        stdout.waitComplete();

        return result;
    }

    protected Commandline getCommandline()
    {

        return cl;
    }

    public void reset()
    {

        // TODO find out why Commandline allows executable, args etc to be initialised to
        // null, but not allowing them to be reset to null. This results is weird "clear"
        // behaviour. It is just safer to start again.

        cl = new Commandline();
        commandLines = new StringBuffer();
        errorOutput = new StringBuffer();
        cl.getShell().setQuotedArgumentsEnabled( true );
        cl.setExecutable( executable );
        try
        {
            cl.addSystemEnvironment();
        }
        catch ( Exception e )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().warn( "Unable to obtain system environment", e );
            }
            else
            {
                getLogger().warn( "Unable to obtain system environment" );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean mkws( String basisStream, String workspaceName, File basedir )
        throws AccuRevException
    {

        setWorkingDirectory( basedir );
        String[] mkws = { "mkws", "-b", basisStream, "-w", workspaceName, "-l", basedir.getAbsolutePath() };

        return executeCommandLine( mkws );

    }

    /**
     * {@inheritDoc}
     */
    public List<File> update( File baseDir, String transactionId )
        throws AccuRevException
    {

        if ( transactionId == null )
        {
            transactionId = "highest";
        }
        String[] update = { "update", "-t", transactionId };
        setWorkingDirectory( baseDir );

        List<File> updatedFiles = new ArrayList<File>();
        return executeCommandLine( update, null, new FileConsumer( updatedFiles, FileConsumer.UPDATE_PATTERN ) ) == 0 ? updatedFiles
                        : null;

    }

    /**
     * {@inheritDoc}
     */
    public List<File> add( File basedir, List<File> elements, String message )
        throws AccuRevException
    {

        if ( StringUtils.isBlank( message ) )
        {
            message = AccuRev.DEFAULT_ADD_MESSAGE;
        }

        boolean recursive = false;

        if ( elements == null || elements.isEmpty() )
        {
            elements = Collections.singletonList( CURRENT_DIR );
            recursive = true;
        }
        else if ( elements.size() == 1 && elements.toArray()[0].equals( CURRENT_DIR ) )
        {
            recursive = true;
        }

        List<File> addedFiles = new ArrayList<File>();
        return executeCommandLine( basedir, new String[] { "add", "-c", message, recursive ? "-R" : null }, elements,
                                   FileConsumer.ADD_PATTERN, addedFiles ) ? addedFiles : null;

    }

    public List<File> defunct( File basedir, List<File> files, String message )
        throws AccuRevException
    {

        if ( StringUtils.isBlank( message ) )
        {
            message = AccuRev.DEFAULT_REMOVE_MESSAGE;
        }

        if ( files == null || files.isEmpty() )
        {
            files = Collections.singletonList( CURRENT_DIR );
        }

        ArrayList<File> defunctFiles = new ArrayList<File>();
        return executeCommandLine( basedir, new String[] { "defunct", "-c", message }, files,
                                   FileConsumer.DEFUNCT_PATTERN, defunctFiles ) ? defunctFiles : null;
    }

    public List<File> promote( File basedir, List<File> files, String message )
        throws AccuRevException
    {

        if ( StringUtils.isBlank( message ) )
        {
            message = AccuRev.DEFAULT_PROMOTE_MESSAGE;
        }
        List<File> promotedFiles = new ArrayList<File>();
        return executeCommandLine( basedir, new String[] { "promote", "-K", "-c", message }, files,
                                   FileConsumer.PROMOTE_PATTERN, promotedFiles ) ? promotedFiles : null;

    }

    public String getCommandLines()
    {

        return commandLines.toString();
    }

    public String getErrorOutput()
    {

        return errorOutput.toString();
    }

    public void setLogger( ScmLogger logger )
    {

        this.logger = logger;
        this.systemErr = new ErrorConsumer( logger, errorOutput );
    }

    public ScmLogger getLogger()
    {

        return logger;
    }

    public boolean mkdepot( String depotName )
        throws AccuRevException
    {

        String[] mkdepot = { "mkdepot", "-p", depotName };

        return executeCommandLine( mkdepot );

    }

    public boolean mkstream( String backingStream, String newStreamName )
        throws AccuRevException
    {
        String[] mkstream = { "mkstream", "-b", backingStream, "-s", newStreamName };
        return executeCommandLine( mkstream );

    }

    public boolean promoteStream( String subStream, String commitMessage, List<File> promotedFiles )
        throws AccuRevException
    {
        String[] promote = { "promote", "-s", subStream, "-d" };
        return executeCommandLine( promote );

    }

    /**
     * {@inheritDoc}
     */
    public List<File> promoteAll( File baseDir, String commitMessage )
        throws AccuRevException
    {

        setWorkingDirectory( baseDir );
        String[] promote = { "promote", "-p", "-K", "-c", commitMessage };

        List<File> promotedFiles = new ArrayList<File>();
        return executeCommandLine( promote, null, new FileConsumer( promotedFiles, FileConsumer.PROMOTE_PATTERN ) ) == 0 ? promotedFiles
                        : null;

    }

    public AccuRevInfo info( File basedir )
        throws AccuRevException
    {

        setWorkingDirectory( basedir );
        String[] info = { "info" };
        AccuRevInfo result = new AccuRevInfo( basedir );

        executeCommandLine( info, null, new InfoConsumer( result ) );
        return result;
    }

    private void setWorkingDirectory( File basedir )
    {

        // TODO raise bug against plexus. Null is OK for working directory
        // but once set to not-null cannot be set back to null!
        // this is a problem if the old workingdir has been deleted
        // probably safer to use a new commandline

        if ( basedir == null )
        {
            cl.setWorkingDirectory( "." );
        }
        cl.setWorkingDirectory( basedir );
    }

    public boolean reactivate( String workSpaceName )
        throws AccuRevException
    {

        String[] reactivate = { "reactivate", "wspace", workSpaceName };

        return executeCommandLine( reactivate, null, new CommandOutputConsumer( getLogger(), null ) ) == 0;

    }

    public boolean rmws( String workSpaceName )
        throws AccuRevException
    {

        String[] rmws = { "rmws", "-s", workSpaceName };

        return executeCommandLine( rmws );

    }

    public String stat( File element )
        throws AccuRevException
    {

        String[] stat = { "stat", "-fx", element.getAbsolutePath() };

        StatConsumer statConsumer = new StatConsumer( getLogger() );
        executeCommandLine( stat, null, statConsumer );
        return statConsumer.getStatus();

    }

    public boolean chws( File basedir, String workSpaceName, String newBasisStream )
        throws AccuRevException
    {

        setWorkingDirectory( basedir );
        return executeCommandLine( new String[] { "chws", "-s", workSpaceName, "-b", newBasisStream, "-l", "." } );

    }

    public boolean mksnap( String snapShotName, String basisStream )
        throws AccuRevException
    {

        return executeCommandLine( new String[] { "mksnap", "-s", snapShotName, "-b", basisStream, "-t", "now" } );
    }

    public List<File> statTag( String streamName )
        throws AccuRevException
    {

        List<File> taggedFiles = new ArrayList<File>();
        String[] stat = new String[] { "stat", "-a", "-ffl", "-s", streamName };
        return executeCommandLine( null, stat, null, FileConsumer.STAT_PATTERN, taggedFiles ) ? taggedFiles : null;
    }

    public List<File> stat( File basedir, Collection<File> elements, AccuRevStat statType )
        throws AccuRevException
    {

        boolean recursive = false;

        if ( elements == null || elements.isEmpty() )
        {
            elements = Collections.singletonList( CURRENT_DIR );
            recursive = true;
        }
        else if ( elements.size() == 1 && elements.toArray()[0].equals( CURRENT_DIR ) )
        {
            recursive = true;
        }

        String[] args = { "stat", "-ffr", statType.getStatArg(), recursive ? "-R" : null };

        List<File> matchingElements = new ArrayList<File>();
        return executeCommandLine( basedir, args, elements, statType.getMatchPattern(), matchingElements ) ? matchingElements
                        : null;
    }

    public List<File> pop( File basedir, Collection<File> elements )
        throws AccuRevException
    {

        if ( elements == null || elements.isEmpty() )
        {
            elements = Collections.singletonList( CURRENT_DIR );
        }

        String[] popws = { "pop", "-R" };

        List<File> poppedFiles = new ArrayList<File>();
        return executeCommandLine( basedir, popws, elements, FileConsumer.POPULATE_PATTERN, poppedFiles ) ? poppedFiles
                        : null;
    }

    public List<File> pop( File basedir, String versionSpec, Collection<File> elements )
        throws AccuRevException
    {

        if ( elements == null || elements.isEmpty() )
        {
            elements = Collections.singletonList( new File( "/./" ) );
        }

        String[] pop = { "pop", "-v", versionSpec, "-L", basedir.getAbsolutePath(), "-R" };

        List<File> poppedFiles = new ArrayList<File>();
        return executeCommandLine( basedir, pop, elements, FileConsumer.POPULATE_PATTERN, poppedFiles ) ? poppedFiles
                        : null;
    }

    public CategorisedElements statBackingStream( File basedir, Collection<File> elements )
        throws AccuRevException
    {

        CategorisedElements catElems = new CategorisedElements();

        if ( elements.isEmpty() )
        {
            return catElems;
        }
        String[] args = { "stat", "-b", "-ffr" };

        return executeCommandLine( basedir, args, elements, new StatBackingConsumer( catElems.getMemberElements(),
                                                                                     catElems.getNonMemberElements() ) ) ? catElems
                        : null;

    }

    public List<Transaction> history( String baseStream, String fromTimeSpec, String toTimeSpec, int count,
                                      boolean depotHistory, boolean transactionsOnly )
        throws AccuRevException
    {

        String timeSpec = fromTimeSpec;

        if ( toTimeSpec != null )
        {
            timeSpec = timeSpec + "-" + toTimeSpec;
        }

        if ( count > 0 )
        {
            timeSpec = timeSpec + "." + count;
        }

        String[] hist =
            { "hist", transactionsOnly ? "-ftx" : "-fx", depotHistory ? "-p" : "-s", baseStream, "-t", timeSpec };

        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        HistoryConsumer stdout = new HistoryConsumer( getLogger(), transactions );
        return executeCommandLine( hist, null, stdout ) == 0 ? transactions : null;
    }

    public List<FileDifference> diff( String baseStream, String fromTimeSpec, String toTimeSpec )
        throws AccuRevException
    {
        String timeSpec = fromTimeSpec + "-" + toTimeSpec;
        String[] diff = { "diff", "-fx", "-a", "-i", "-v", baseStream, "-V", baseStream, "-t", timeSpec };

        List<FileDifference> results = new ArrayList<FileDifference>();
        DiffConsumer stdout = new DiffConsumer( getLogger(), results );
        return executeCommandLine( diff, null, stdout ) < 2 ? results : null;
    }

    public boolean login( String user, String password )
        throws AccuRevException
    {

        // TODO Raise bug against plexus commandline - can't set workingdir to null
        // and will get an error if the working directory is deleted.
        cl.setWorkingDirectory( "." );
        authArgs = EMPTY_STRING_ARRAY;
        AuthTokenConsumer stdout = new AuthTokenConsumer();

        boolean result;
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            if ( StringUtils.isBlank( password ) )
            {
                // Ensure blank is passed in.
                password = "\"\"";
            }
            String[] login = { "login", "-A", user, password };
            result = executeCommandLine( login, null, stdout ) == 0;
        }
        else
        {
            String[] login = { "login", "-A", user };
            password = StringUtils.clean( password ) + "\n";
            byte[] bytes = password.getBytes();
            ByteArrayInputStream stdin = new ByteArrayInputStream( bytes );
            result = executeCommandLine( login, stdin, stdout ) == 0;

        }

        authArgs = new String[] { "-A", stdout.getAuthToken() };
        return result;
    }

    public boolean logout()
        throws AccuRevException
    {

        String[] logout = { "logout" };
        return executeCommandLine( logout );

    }

    public List<BlameLine> annotate( File basedir, File file )
        throws AccuRevException
    {

        String[] annotate = { "annotate", "-ftud" };
        List<BlameLine> lines = new ArrayList<BlameLine>();
        AnnotateConsumer stdout = new AnnotateConsumer( lines, getLogger() );

        return executeCommandLine( basedir, annotate, Collections.singletonList( file ), stdout ) ? lines : null;
    }

    public Map<String, WorkSpace> showRefTrees()
        throws AccuRevException
    {

        String[] show = { "show", "-fx", "refs" };
        Map<String, WorkSpace> refTrees = new HashMap<String, WorkSpace>();
        WorkSpaceConsumer stdout = new WorkSpaceConsumer( getLogger(), refTrees );
        return executeCommandLine( show, null, stdout ) == 0 ? refTrees : null;
    }

    public Map<String, WorkSpace> showWorkSpaces()
        throws AccuRevException
    {

        String[] show = { "show", "-a", "-fx", "wspaces" };
        Map<String, WorkSpace> workSpaces = new HashMap<String, WorkSpace>();
        WorkSpaceConsumer stdout = new WorkSpaceConsumer( getLogger(), workSpaces );
        return executeCommandLine( show, null, stdout ) == 0 ? workSpaces : null;
    }

    public Stream showStream( String stream )
        throws AccuRevException
    {
        String[] show = { "show", "-s", stream, "-fx", "streams" };
        List<Stream> streams = new ArrayList<Stream>();
        StreamsConsumer stdout = new StreamsConsumer( getLogger(), streams );

        return executeCommandLine( show, null, stdout ) == 0 && streams.size() == 1 ? streams.get( 0 ) : null;
    }

    public String getExecutable()
    {

        return executable;
    }

    public String getClientVersion()
        throws AccuRevException
    {

        long lastModified = new File( getExecutable() ).lastModified();
        if ( clientVersion == null || executableModTime != lastModified )
        {
            executableModTime = lastModified;

            ClientVersionConsumer stdout = new ClientVersionConsumer();
            executeCommandLine( new String[] {}, null, stdout );
            clientVersion = stdout.getClientVersion();
        }
        return clientVersion;

    }

}
