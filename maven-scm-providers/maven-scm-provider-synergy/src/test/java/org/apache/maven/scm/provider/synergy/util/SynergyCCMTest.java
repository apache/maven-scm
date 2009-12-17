package org.apache.maven.scm.provider.synergy.util;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyCCMTest
    extends ScmTestCase
{

    public void testShowTaskObjects()
        throws Exception
    {
        Commandline cl = SynergyCCM.showTaskObjects( 45, "my format", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm task -show objects -f \"my format\" 45", null, cl );
        cl = SynergyCCM.showTaskObjects( 45, null, "CCM_ADDR" );
        assertCommandLine( "ccm task -show objects 45", null, cl );
    }

    public void testQuery()
        throws Exception
    {
        Commandline cl = SynergyCCM.query( "my query", "my format", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm query -u -f \"my format\" \"my query\"", null, cl );
        cl = SynergyCCM.query( "my query", null, "CCM_ADDR" );
        assertCommandLine( "ccm query -u \"my query\"", null, cl );
    }

    public void testCreateBaseline()
        throws Exception
    {
        Commandline cl =
            SynergyCCM.createBaseline( "myProject~1", "theBaseline", "my_release", "my_purpose", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm baseline -create theBaseline -p myProject~1 -release my_release -purpose my_purpose",
                           null, cl );
    }

    public void testCreate()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        f.deleteOnExit();
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.create( list, "test creation", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertCommandLine( "ccm create -c \"test creation\" \"" + f.getCanonicalPath() + "\"", null, cl );
        }
        else
        {
            assertCommandLine( "ccm create -c \"test creation\" " + f.getCanonicalPath(), null, cl );
        }
        File f2 = File.createTempFile( "test", null );
        f2.deleteOnExit();
        list.add( f2 );
        cl = SynergyCCM.create( list, "test creation", "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine( "ccm create -c \"test creation\" \"" + f.getCanonicalPath() + "\" \"" +
                    f2.getCanonicalPath() + "\"", null, cl );
            }
            else
            {
                assertCommandLine(
                    "ccm create -c \"test creation\" \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "",
                    null, cl );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine(
                    "ccm create -c \"test creation\" " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"",
                    null, cl );
            }
            else
            {
                assertCommandLine(
                    "ccm create -c \"test creation\" " + f.getCanonicalPath() + " " + f2.getCanonicalPath(), null, cl );
            }
        }
    }

    public void testCreateTask()
        throws Exception
    {
        /*
         * NOTE: Quoting of arguments can differ for Windows/Unix, hence we normalize to single quotes for the purpose
         * of testing.
         */

        Commandline cl = SynergyCCM.createTask( "the synopsis", "release", true, "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        String actual = cl.toString().replace( '\"', '\'' );
        String expected = "ccm task -create -synopsis 'the synopsis' -release release";
        assertTrue( "[" + actual + "] does not contain [" + expected + "]",
                    actual.indexOf( expected ) > -1 );

        cl = SynergyCCM.createTask( "the synopsis", null, true, "CCM_ADDR" );
        actual = cl.toString().replace( '\"', '\'' );
        expected = "ccm task -create -synopsis 'the synopsis'";
        assertTrue( "[" + actual + "] does not contain [" + expected + "]",
                    actual.indexOf( expected ) > -1 );
    }

    public void testCheckinTask()
        throws Exception
    {
        Commandline cl = SynergyCCM.checkinTask( "truc", "a comment", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm task -checkin truc -comment \"a comment\"", null, cl );
    }

    public void testDelete()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        f.deleteOnExit();
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.delete( list, "CCM_ADDR", true );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertCommandLine( "ccm delete -replace \"" + f.getCanonicalPath() + "\"", null, cl );
        }
        else
        {
            assertCommandLine( "ccm delete -replace " + f.getCanonicalPath(), null, cl );
        }
        File f2 = File.createTempFile( "test", null );
        f2.deleteOnExit();
        list.add( f2 );
        cl = SynergyCCM.delete( list, "CCM_ADDR", false );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine( "ccm delete \"" + f.getCanonicalPath() + "\" \"" + f2.getCanonicalPath() + "\"",
                                   null, cl );
            }
            else
            {
                assertCommandLine( "ccm delete \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "", null,
                                   cl );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine( "ccm delete " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"", null,
                                   cl );
            }
            else
            {
                assertCommandLine( "ccm delete " + f.getCanonicalPath() + " " + f2.getCanonicalPath(), null, cl );
            }
        }
    }

    public void testReconfigure()
        throws Exception
    {
        Commandline cl = SynergyCCM.reconfigure( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm reconfigure -recurse -p project~1", null, cl );
    }

    public void testReconfigureProperties()
        throws Exception
    {
        Commandline cl = SynergyCCM.reconfigureProperties( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm reconfigure_properties -refresh project~1", null, cl );
    }

    public void testReconcileUwa()
        throws Exception
    {
        Commandline cl = SynergyCCM.reconcileUwa( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm rwa -r -uwa -p project~1", null, cl );
        cl = SynergyCCM.reconcileUwa( null, "CCM_ADDR" );
        assertCommandLine( "ccm rwa -r -uwa", null, cl );
    }

    public void testReconcileUdb()
        throws Exception
    {
        Commandline cl = SynergyCCM.reconcileUdb( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm rwa -r -udb -p project~1", null, cl );
        cl = SynergyCCM.reconcileUdb( null, "CCM_ADDR" );
        assertCommandLine( "ccm rwa -r -udb", null, cl );
    }

    public void testDir()
        throws Exception
    {
        File f = File.createTempFile( "foo", null );
        f.deleteOnExit();
        Commandline cl = SynergyCCM.dir( f.getParentFile(), "format", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( f.getParentFile().getCanonicalFile(), cl.getWorkingDirectory().getCanonicalFile() );
        assertCommandLine( "ccm dir -m -f format", f.getParentFile().getCanonicalFile(), cl );
    }

    public void testCheckoutFiles()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        f.deleteOnExit();
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.checkoutFiles( list, "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertCommandLine( "ccm co \"" + f.getCanonicalPath() + "\"", null, cl );
        }
        else
        {
            assertCommandLine( "ccm co " + f.getCanonicalPath(), null, cl );
        }
        File f2 = File.createTempFile( "test", null );
        f2.deleteOnExit();
        list.add( f2 );
        cl = SynergyCCM.checkoutFiles( list, "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine( "ccm co \"" + f.getCanonicalPath() + "\" \"" + f2.getCanonicalPath() + "\"", null,
                                   cl );
            }
            else
            {
                assertCommandLine( "ccm co \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "", null, cl );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine( "ccm co " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"", null, cl );
            }
            else
            {
                assertCommandLine( "ccm co " + f.getCanonicalPath() + " " + f2.getCanonicalPath(), null, cl );
            }
        }
    }

    public void testCheckoutProject()
        throws Exception
    {
        Commandline cl = SynergyCCM.checkoutProject( null, "MyProject", new ScmTag( "MyVersion" ), "MyPurpose",
                                                     "MyRelease", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm co -subprojects -rel -t MyVersion -purpose MyPurpose -release MyRelease -p MyProject",
                           null, cl );
        File f = File.createTempFile( "test", null );
        f.deleteOnExit();
        cl = SynergyCCM.checkoutProject( f.getParentFile(), "MyProject", new ScmTag( "MyVersion" ), "MyPurpose",
                                         "MyRelease", "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertCommandLine( "ccm co -subprojects -rel -t MyVersion -purpose MyPurpose -release MyRelease -path \"" +
                f.getParentFile().getCanonicalPath() + "\" -p MyProject", null, cl );
        }
        else
        {
            assertCommandLine( "ccm co -subprojects -rel -t MyVersion -purpose MyPurpose -release MyRelease -path " +
                f.getParentFile().getCanonicalPath() + " -p MyProject", null, cl );
        }
    }

    public void testCheckinProject()
        throws Exception
    {
        Commandline cl = SynergyCCM.checkinProject( "MyProject", "a comment", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm ci -c \"a comment\" -p MyProject", null, cl );
    }

    public void testCheckinFiles()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        f.deleteOnExit();
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.checkinFiles( list, "a comment", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertCommandLine( "ccm ci -c \"a comment\" \"" + f.getCanonicalPath() + "\"", null, cl );
        }
        else
        {
            assertCommandLine( "ccm ci -c \"a comment\" " + f.getCanonicalPath(), null, cl );
        }
        File f2 = File.createTempFile( "test", null );
        f2.deleteOnExit();
        list.add( f2 );
        cl = SynergyCCM.checkinFiles( list, "a comment", "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine(
                    "ccm ci -c \"a comment\" \"" + f.getCanonicalPath() + "\" \"" + f2.getCanonicalPath() + "\"", null,
                    cl );
            }
            else
            {
                assertCommandLine(
                    "ccm ci -c \"a comment\" \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "", null,
                    cl );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertCommandLine(
                    "ccm ci -c \"a comment\" " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"", null,
                    cl );
            }
            else
            {
                assertCommandLine( "ccm ci -c \"a comment\" " + f.getCanonicalPath() + " " + f2.getCanonicalPath(),
                                   null, cl );
            }
        }
    }

    public void testSync()
        throws Exception
    {
        Commandline cl = SynergyCCM.synchronize( "myProject", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm sync -r -p myProject", null, cl );
    }

    public void testShowWorkArea()
        throws Exception
    {
        Commandline cl = SynergyCCM.showWorkArea( "MyProject~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm wa -show MyProject~1", null, cl );
    }

    public void testStart()
        throws ScmException
    {
        Commandline cl = SynergyCCM.start( "user", "pass", SynergyRole.BUILD_MGR );
        assertCommandLine( "ccm start -nogui -m -q -n user -pw pass -r build_mgr", null, cl );
    }
	
    public void testStartRemote()
        throws ScmException
    {
        Commandline cl = SynergyCCM.startRemote( "user", "pass", SynergyRole.BUILD_MGR );
        assertCommandLine( "ccm start -nogui -m -q -rc -n user -pw pass -r build_mgr", null, cl );
    }

    public void testStop()
        throws Exception
    {
        Commandline cl = SynergyCCM.stop( "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm stop", null, cl );
    }

    public void testDelimiter()
        throws Exception
    {
        Commandline cl = SynergyCCM.delimiter( "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironmentVariables(), "CCM_ADDR=CCM_ADDR" ) );
        assertCommandLine( "ccm delimiter", null, cl );
    }

    public boolean assertContains( String[] array, String value )
    {
        for ( int i = 0; i < array.length; i++ )
        {
            if ( array[i].equals( value ) )
            {
                return true;
            }
        }
        return false;
    }
}
