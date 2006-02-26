package org.apache.maven.scm.provider.svn.svnjava;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import junit.framework.Assert;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Uses svn-test.properties to configure how to execute the test cases.  At this time,
 * the tmate.org java library does not support file:// urls.  To test the javasvn scm provider,
 * you must create an apache instance which serves subversion. The repository that apache points
 * to must also be a local repository which can be accessed via svnadmin.<br>
 * Here is an example windows configuration: <br>
 * - Create a folder C:\svn which will contain test repositories.
 * - add a SVNParentPath which points to C:\svn. This will allow the test cases to create new repositories
 * under the parent path and reference them via the url "/svn/repository-name".
 * <pre>
 * &lt;Location /svn&gt;
 *   DAV svn
 *   SVNParentPath C:\svn
 * &lt;/Location&gt;
 * </pre>
 * <br>
 * Now edit the svn-test.properties and set the following:
 * <pre>
 * base-url=http://localhost/svn
 * svn-root=C:\\svn
 * </pre>
 * <p/>
 * Once this is configured, remove the line in the pom.xml which excludes all of the Tck Tests
 * during the build.  The test cases will now create test repositories in the svn-root folder
 * and have access to them via the base-url.
 *
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 */
public final class SvnJavaTestUtils
{
    private static final int MAX_DELETE_ATTEMPTS = 10;

    private static final int SLEEP_RETRY_PERIOD = 5000; // 5 seconds

    private static Properties props = new Properties();

    static
    {
        InputStream in = SvnJavaTestUtils.class.getClassLoader().getResourceAsStream( "svn-test.properties" );

        if ( in != null )
        {
            try
            {
                props.load( in );
            }
            catch ( IOException e )
            {
                // ignore it
                props.clear();
            }
        }
    }

    private SvnJavaTestUtils()
    {
    }

    public static String getBaseURL()
    {
        return props.getProperty( "base-url" );
    }

    public static File getSvnRoot()
    {
        if ( props.getProperty( "svn-root" ) != null )
        {
            return new File( props.getProperty( "svn-root" ) );
        }
        else
        {
            return null;
        }
    }

    /**
     * Its possible that a server process still has the directory locked.
     * Attempt to delete it a few times before giving up.
     *
     * @param directory
     * @throws Exception
     */
    private static void deleteDirectory( File directory )
        throws Exception
    {
        for ( int nAttempt = 0; nAttempt < MAX_DELETE_ATTEMPTS; nAttempt++ )
        {
            try
            {
                FileUtils.deleteDirectory( directory );
            }
            catch ( IOException e )
            {
                if ( nAttempt >= MAX_DELETE_ATTEMPTS - 1 )
                {
                    throw e;
                }
                else
                {
                    System.out.println( "Attempt[" + ( nAttempt + 1 ) + " of " + MAX_DELETE_ATTEMPTS +
                        "] - Error deleting directory: " + directory.getAbsolutePath() );
                    Thread.sleep( SLEEP_RETRY_PERIOD );
                }
            }
        }
    }

    public static void initializeRepository( File repositoryRoot, File dump )
        throws Exception
    {
        if ( repositoryRoot.exists() )
        {
            deleteDirectory( repositoryRoot );
        }

        Assert.assertTrue( "Could not make repository root directory: " + repositoryRoot.getAbsolutePath(),
                           repositoryRoot.mkdirs() );

        ScmTestCase.execute( repositoryRoot.getParentFile(), "svnadmin", "create " + repositoryRoot.getName() );

        loadSvnDump( repositoryRoot, dump );
    }

    private static void loadSvnDump( File repositoryRoot, File dump )
        throws Exception
    {
        Assert.assertTrue( "The dump file doesn't exist: " + dump.getAbsolutePath(), dump.exists() );

        Commandline cl = new Commandline();

        cl.setExecutable( "svnadmin" );

        cl.setWorkingDirectory( repositoryRoot.getParentFile().getAbsolutePath() );

        cl.createArgument().setValue( "load" );

        cl.createArgument().setValue( repositoryRoot.getAbsolutePath() );

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitValue = CommandLineUtils.executeCommandLine( cl, new FileInputStream( dump ), stdout, stderr );

        if ( exitValue != 0 )
        {
            System.err.println( "-----------------------------------------" );
            System.err.println( "Command line: " + cl );
            System.err.println( "Working directory: " + cl.getWorkingDirectory() );
            System.err.println( "-----------------------------------------" );
            System.err.println( "Standard output: " );
            System.err.println( "-----------------------------------------" );
            System.err.println( stdout.getOutput() );
            System.err.println( "-----------------------------------------" );

            System.err.println( "Standard error: " );
            System.err.println( "-----------------------------------------" );
            System.err.println( stderr.getOutput() );
            System.err.println( "-----------------------------------------" );
        }

        if ( exitValue != 0 )
        {
            Assert.fail( "Exit value wasn't 0, was:" + exitValue );
        }
    }
}
