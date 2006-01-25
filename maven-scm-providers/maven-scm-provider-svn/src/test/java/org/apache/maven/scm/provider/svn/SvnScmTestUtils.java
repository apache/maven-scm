package org.apache.maven.scm.provider.svn;

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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public final class SvnScmTestUtils
{
    private SvnScmTestUtils()
    {
    }

    public static void initializeRepository( File repositoryRoot, File dump )
        throws Exception
    {
        if ( repositoryRoot.exists() )
        {
            FileUtils.deleteDirectory( repositoryRoot );
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

    public static String getScmUrl( File repositoryRootFile )
        throws CommandLineException
    {
        String repositoryRoot = repositoryRootFile.getAbsolutePath();

        // TODO: it'd be great to build this into CommandLineUtils somehow
        // TODO: some way without a custom cygwin sys property?
        if ( "true".equals( System.getProperty( "cygwin" ) ) )
        {
            Commandline cl = new Commandline();

            cl.setExecutable( "cygpath" );

            cl.createArgument().setValue( "--unix" );

            cl.createArgument().setValue( repositoryRoot );

            CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

            int exitValue = CommandLineUtils.executeCommandLine( cl, stdout, null );

            if ( exitValue != 0 )
            {
                throw new CommandLineException( "Unable to convert cygwin path, exit code = " + exitValue );
            }

            repositoryRoot = stdout.getOutput().trim();
        }
        else if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
        {
            repositoryRoot = "/" + StringUtils.replace( repositoryRoot, "\\", "/" );
        }

        return "scm:svn:file://" + repositoryRoot;
    }
}
