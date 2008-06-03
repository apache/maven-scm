package org.apache.maven.scm.provider.accurev.commands.checkout;

/*
 * Copyright 2008 AccuRev Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccuRevScmTckTestCase extends CheckOutCommandTckTest
{
    private String username;
    private String password;
    private String host;
    private Integer port;
    private String depot;
    private String stream;

    private String initWorkspaceName;
    private String workspaceName;

    private MyAccuRevScmProvider scmProvider = new MyAccuRevScmProvider();

    private BasicScmManager scmManager;

    protected ScmManager getScmManager() throws Exception
    {
        if ( null == scmManager )
        {
            scmManager = new BasicScmManager();
            scmManager.setScmProvider( "accurev", scmProvider );
        }
        return scmManager;
    }

    protected void setUp() throws Exception
    {
        username = System.getProperty( "test.accurev.username", "test" );
        password = System.getProperty( "test.accurev.password", "" );
        host = System.getProperty( "test.accurev.host", "localhost" );
        port = Integer.getInteger( "test.accurev.port", 5050 );
        depot = System.getProperty( "test.accurev.depot", "Test" );
        stream = System.getProperty( "test.accurev.stream", "Test" );

        initWorkspaceName = "workspace." + System.currentTimeMillis() + "_" + username;
        workspaceName = "workspace." + ( System.currentTimeMillis() + 1 ) + "_" + username;

        super.setUp();
    }

    public String getScmUrl( String workspaceName, String checkoutMethod ) throws Exception
    {
        return StringUtils.join( new String[]{
            "scm", "accurev", username, password + "@" + host, String.valueOf( port ), depot, stream, workspaceName
        }, ':' ) + "?checkoutMethod=" + checkoutMethod;
    }

    public String getScmUrl() throws Exception
    {
        return getScmUrl( this.workspaceName, "pop" );
    }

    public void initRepo() throws Exception
    {
        String scmUrl = getScmUrl( initWorkspaceName, "mkws" );
        //Login
        ScmRepository scmRepository = getScmManager().makeScmRepository( scmUrl );
        scmProvider.login( scmRepository.getProviderRepository(), getScmFileSet(), new CommandParameters() );

        File basedir = getScmFileSet().getBasedir();
        if ( !basedir.exists() )
        {
            basedir.mkdir();
            basedir.mkdirs();
        }
        else
        {
            cleanDir( basedir );
        }
        //Check if the base folder is not assosiated with workspace
        Commandline cl = makeCommandLine( basedir, "info" );

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        InfoCommandStreamConsumer infoCommandStreamConsumer = new InfoCommandStreamConsumer();
        executeCommandLine( cl, infoCommandStreamConsumer, stdout );

        String workspace = infoCommandStreamConsumer.getWorkspace();
        if ( null != workspace )
        {
            //Pop everything
            cl = makeCommandLine( basedir, "pop" );
            cl.addArguments( new String[]{"-R", "."} );
            executeCommandLine( cl, stdout, stdout );
        }
        else
        {
            workspace = initWorkspaceName;
            //Checkout the test stream
            CheckOutScmResult result = getScmManager().checkOut( scmRepository, getScmFileSet() );
            if ( !result.isSuccess() )
            {
                throw new IllegalStateException( "Cannot checkout the stream" );
            }
        }
        File[] files = basedir.listFiles();
        List filesToDefunct = new ArrayList( files.length );
        for ( int i = 0; i < files.length; i++ )
        {
            File file = files[i];
            filesToDefunct.add( file.getAbsolutePath() );
        }
        //Clean the test stream
        if ( filesToDefunct.size() > 0 )
        {
            //Defunct all elements in stream
            cl = makeCommandLine( basedir, "defunct" );
            cl.addArguments( (String[]) filesToDefunct.toArray( new String[filesToDefunct.size()] ) );
            stdout = new CommandLineUtils.StringStreamConsumer();
            executeCommandLine( cl, stdout, stdout );
            //Promote defuncted files
            promote( filesToDefunct, basedir );
        }
        //Add files expected by the tests to the repository
        String[] filesToCreate = {
            "/pom.xml", "/readme.txt", "/src/main/java/Application.java", "/src/test/java/Test.java"
        };
        List filesToPromote = new ArrayList();
        for ( int i = 0; i < filesToCreate.length; i++ )
        {
            String fileToCreate = filesToCreate[i];
            File file = new File( basedir.getAbsolutePath() + fileToCreate.replace( '/', File.separatorChar ) );
            file.getParentFile().mkdirs();

            writeContentsToFile( file, fileToCreate );
            addToWorkingTree( basedir, file, scmRepository );
            filesToPromote.add( file.getAbsolutePath() );
        }
        promote( filesToPromote, basedir );

        //remove the workspace
        removeWorkspace( basedir, workspace );
        cleanDir( basedir );
    }

    private void promote( List files, File basedir ) throws CommandLineException
    {
        Commandline cl = makeCommandLine( basedir, "promote" );
        cl.addArguments( (String[]) files.toArray( new String[files.size()] ) );
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        executeCommandLine( cl, stdout, stdout );
    }

    private void writeContentsToFile( File file, String contents ) throws IOException
    {
        FileWriter fileWriter = new FileWriter( file, false );
        fileWriter.write( contents );
        fileWriter.close();
    }

    private void removeWorkspace( File commandBasedir, String workspace ) throws CommandLineException
    {
        CommandLineUtils.StringStreamConsumer stdout;
        Commandline cl;
        stdout = new CommandLineUtils.StringStreamConsumer();

        cl = makeCommandLine( commandBasedir, "rmws" );
        cl.addArguments( new String[]{"-s", workspace} );

        executeCommandLine( cl, stdout, stdout );
    }

    private void cleanDir( File basedir )
    {
        if ( !removeFilesInDir( basedir ) )
        {
            throw new IllegalStateException( "Files in checkout directory cannot be deleted" );
        }
    }

    private boolean removeFilesInDir( File dir )
    {
        if ( !dir.isDirectory() )
        {
            throw new IllegalArgumentException( "\"" + dir + "\" not a directory" );
        }
        boolean res = true;
        File[] files = dir.listFiles();
        for ( int i = 0; i < files.length; i++ )
        {
            File file = files[i];
            if ( file.isDirectory() )
            {
                res &= removeFilesInDir( file );
                continue;
            }
            res &= file.delete();
        }
        return res;
    }

    private Commandline makeCommandLine( File basedir, String command )
    {
        Commandline cl;
        cl = new Commandline();
        cl.setWorkingDirectory( basedir.getAbsolutePath() );
        cl.setExecutable( "accurev" );
        cl.addArguments( new String[]{command, "-H", host + ":" + port} );
        return cl;
    }

    private static void executeCommandLine( Commandline cl, CommandLineUtils.StringStreamConsumer stdOutConsumer,
                                            CommandLineUtils.StringStreamConsumer errOutConsumer )
        throws CommandLineException
    {
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdOutConsumer, errOutConsumer );
        if ( exitCode != 0 )
        {
            throw new IllegalStateException( "Cannot execute command: \"" + cl.toString() + "\"." +
                "\nOutput: \n" + stdOutConsumer.getOutput() +
                "\nError output: \n" + errOutConsumer.getOutput() +
                "" );
        }
    }

    public class MyAccuRevScmProvider extends AccuRevScmProvider
    {
        public LoginScmResult login( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters ) throws ScmException
        {
            return super.login( repository, fileSet, parameters );
        }
    }

    private class InfoCommandStreamConsumer extends CommandLineUtils.StringStreamConsumer
    {
        private String workspace;
        private final Pattern pattern = Pattern.compile( "Workspace/ref:\\s*(.*)" );

        public InfoCommandStreamConsumer()
        {
        }

        public void consumeLine( String line )
        {
            super.consumeLine( line );
            Matcher m = pattern.matcher( line );

            if ( m.matches() )
            {
                workspace = m.group( 1 );
            }
        }

        public String getWorkspace()
        {
            return workspace;
        }
    }
}
