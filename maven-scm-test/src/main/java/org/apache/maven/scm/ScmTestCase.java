package org.apache.maven.scm;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import junit.framework.TestCase;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class ScmTestCase
    extends PlexusTestCase
{
    protected static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone( "GMT" );

    private static boolean debugExecute;

    public ScmTestCase()
    {
    }

    protected String getModule()
    {
        fail( "getModule() must be overridden." );

        return null;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( getWorkingDirectory() );
    }

    protected ScmManager getScmManager()
        throws Exception
    {
        return (ScmManager) lookup( ScmManager.ROLE );
    }

    protected ScmRepository makeScmRepository( String scmUrl )
    	throws Exception
    {
        return getScmManager().makeScmRepository( scmUrl );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertPath( String expectedPath, String actualPath )
    	throws Exception
    {
        assertEquals( StringUtils.replace( expectedPath, "\\", "/" ), StringUtils.replace( expectedPath, "\\", "/" ) );
    }

    public void assertResultIsSuccess( ScmResult result )
    {
        if ( result.isSuccess() )
        {
            return;
        }

        System.err.println( "----------------------------------------------------------------------" );
        System.err.println( "Provider message" );
        System.err.println( "----------------------------------------------------------------------" );
        System.err.println( result.getProviderMessage() );
        System.err.println( "----------------------------------------------------------------------" );

        System.err.println( "----------------------------------------------------------------------" );
        System.err.println( "Command output" );
        System.err.println( "----------------------------------------------------------------------" );
        System.err.println( result.getCommandOutput() );
        System.err.println( "----------------------------------------------------------------------" );

        fail( "The check out result success flag was false." );
    }

    // ----------------------------------------------------------------------
    // Utility Methods
    // ----------------------------------------------------------------------

    protected File getRepository()
    {
        return getTestFile( "/src/test/repository" );
    }

    protected ScmFileSet getScmFileSet()
    {
        return new ScmFileSet( getWorkingDirectory() );
    }

    protected File getWorkingDirectory()
    {
        String testName = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( ".") + 1 );

        String caseName = ((TestCase)this).getName();

        return getTestFile( "target/workingDirectory/" + testName + "/" + caseName );
    }

    protected static void setDebugExecute( boolean debugExecute )
    {
        ScmTestCase.debugExecute = debugExecute;
    }

    public static void execute( File workingDirectory, String executable, String arguments )
        throws Exception
    {
        Commandline cl = new Commandline();

        cl.setExecutable( executable );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        cl.addArguments( Commandline.translateCommandline( arguments ) );

        StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitValue = CommandLineUtils.executeCommandLine( cl, stdout, stderr );

        if ( debugExecute || exitValue != 0 )
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
            fail( "Exit value wasn't 0, was:" + exitValue );
        }
    }

    public static void makeDirectory( File basedir, String fileName )
    {
        File dir = new File( basedir, fileName );

        if ( !dir.exists() )
        {
            assertTrue( dir.mkdirs() );
        }
    }

    public static void makeFile( File basedir, String fileName )
    	throws IOException
    {
        makeFile( basedir, fileName, fileName );
    }

    public static void makeFile( File basedir, String fileName, String contents )
    	throws IOException
    {
        File file = new File( basedir, fileName );

        File parent = file.getParentFile();

        if ( !parent.exists() )
        {
            assertTrue( parent.mkdirs() );
        }

        FileWriter writer = new FileWriter( file );

        writer.write( contents );

        writer.close();
    }

    public static Date getDate( int year, int month, int day )
    {
        return getDate(year, month, day, 0, 0, 0, null);
    }

    protected static Date getDate( int year, int month, int day, TimeZone tz )
    {
        return getDate(year, month, day, 0, 0, 0, tz);
    }
    
    protected static Date getDate( int year, int month, int day, int hourOfDay, int minute, int second, TimeZone tz )
    {
        Calendar cal = Calendar.getInstance();
        
        if ( tz != null )
        {
            cal.setTimeZone( tz );
        }
        cal.set( year, month, day, hourOfDay, minute, second );
        cal.set( Calendar.MILLISECOND, 0 );
        
        return cal.getTime();
    }
}
