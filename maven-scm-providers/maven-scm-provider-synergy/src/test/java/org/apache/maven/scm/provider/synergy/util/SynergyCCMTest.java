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

import junit.framework.TestCase;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmTag;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyCCMTest
    extends TestCase
{

    public void testShowTaskObjects()
        throws ScmException
    {
        Commandline cl = SynergyCCM.showTaskObjects( 45, "my format", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm task -show objects -f \"my format\" 45", cl.toString() );
        cl = SynergyCCM.showTaskObjects( 45, null, "CCM_ADDR" );
        assertEquals( "ccm task -show objects 45", cl.toString() );
    }

    public void testQuery()
        throws ScmException
    {
        Commandline cl = SynergyCCM.query( "my query", "my format", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm query -u -f \"my format\" \"my query\"", cl.toString() );
        cl = SynergyCCM.query( "my query", null, "CCM_ADDR" );
        assertEquals( "ccm query -u \"my query\"", cl.toString() );
    }

    public void testCreateBaseline()
        throws ScmException
    {
        Commandline cl =
            SynergyCCM.createBaseline( "myProject~1", "theBaseline", "my_release", "my_purpose", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm baseline -create theBaseline -p myProject~1 -release my_release -purpose my_purpose",
                      cl.toString() );
    }

    public void testCreate()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.create( list, "test creation", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertEquals( "ccm create -c \"test creation\" \"" + f.getCanonicalPath() + "\"", cl.toString() );
        }
        else
        {
            assertEquals( "ccm create -c \"test creation\" " + f.getCanonicalPath(), cl.toString() );
        }
        File f2 = File.createTempFile( "test", null );
        list.add( f2 );
        cl = SynergyCCM.create( list, "test creation", "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals( "ccm create -c \"test creation\" \"" + f.getCanonicalPath() + "\" \"" +
                    f2.getCanonicalPath() + "\"", cl.toString() );
            }
            else
            {
                assertEquals(
                    "ccm create -c \"test creation\" \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "",
                    cl.toString() );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals(
                    "ccm create -c \"test creation\" " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"",
                    cl.toString() );
            }
            else
            {
                assertEquals( "ccm create -c \"test creation\" " + f.getCanonicalPath() + " " + f2.getCanonicalPath(),
                              cl.toString() );
            }
        }
    }

    public void testCreateTask()
        throws ScmException
    {
        Commandline cl = SynergyCCM.createTask( "the synopsis", "release", true, "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        String expected = "ccm task -create -synopsis \"the synopsis\" -release release";
        assertTrue( "[" + cl.toString() + "] do not contain [" + expected + "]",
                    cl.toString().indexOf( expected ) > -1 );
        cl = SynergyCCM.createTask( "the synopsis", null, true, "CCM_ADDR" );
        expected = "ccm task -create -synopsis \"the synopsis\"";
        assertTrue( "[" + cl.toString() + "] do not contain [" + expected + "]",
                    cl.toString().indexOf( expected ) > -1 );
    }

    public void testCheckinTask()
        throws ScmException
    {
        Commandline cl = SynergyCCM.checkinTask( "truc", "a comment", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm task -checkin truc -comment \"a comment\"", cl.toString() );
    }

    public void testDelete()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.delete( list, "CCM_ADDR", true );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertEquals( "ccm delete -replace \"" + f.getCanonicalPath() + "\"", cl.toString() );
        }
        else
        {
            assertEquals( "ccm delete -replace " + f.getCanonicalPath(), cl.toString() );
        }
        File f2 = File.createTempFile( "test", null );
        list.add( f2 );
        cl = SynergyCCM.delete( list, "CCM_ADDR", false );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals( "ccm delete \"" + f.getCanonicalPath() + "\" \"" + f2.getCanonicalPath() + "\"", cl
                    .toString() );
            }
            else
            {
                assertEquals( "ccm delete \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "", cl
                    .toString() );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals( "ccm delete " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"", cl
                    .toString() );
            }
            else
            {
                assertEquals( "ccm delete " + f.getCanonicalPath() + " " + f2.getCanonicalPath(), cl.toString() );
            }
        }
    }

    public void testReconfigure()
        throws ScmException
    {
        Commandline cl = SynergyCCM.reconfigure( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm reconfigure -recurse -p project~1", cl.toString() );
    }

    public void testReconfigureProperties()
        throws ScmException
    {
        Commandline cl = SynergyCCM.reconfigureProperties( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm reconfigure_properties -refresh project~1", cl.toString() );
    }

    public void testReconcileUwa()
        throws ScmException
    {
        Commandline cl = SynergyCCM.reconcileUwa( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm rwa -r -uwa -p project~1", cl.toString() );
        cl = SynergyCCM.reconcileUwa( null, "CCM_ADDR" );
        assertEquals( "ccm rwa -r -uwa", cl.toString() );
    }

    public void testReconcileUdb()
        throws ScmException
    {
        Commandline cl = SynergyCCM.reconcileUdb( "project~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm rwa -r -udb -p project~1", cl.toString() );
        cl = SynergyCCM.reconcileUdb( null, "CCM_ADDR" );
        assertEquals( "ccm rwa -r -udb", cl.toString() );
    }

    public void testDir()
        throws Exception
    {
        File f = File.createTempFile( "foo", null );
        Commandline cl = SynergyCCM.dir( f.getParentFile(), "format", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( f.getParentFile().getCanonicalFile(), cl.getWorkingDirectory().getCanonicalFile() );
        assertEquals( "ccm dir -m -f format", cl.toString() );
    }

    public void testCheckoutFiles()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.checkoutFiles( list, "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertEquals( "ccm co \"" + f.getCanonicalPath() + "\"", cl.toString() );
        }
        else
        {
            assertEquals( "ccm co " + f.getCanonicalPath(), cl.toString() );
        }
        File f2 = File.createTempFile( "test", null );
        list.add( f2 );
        cl = SynergyCCM.checkoutFiles( list, "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals( "ccm co \"" + f.getCanonicalPath() + "\" \"" + f2.getCanonicalPath() + "\"", cl
                    .toString() );
            }
            else
            {
                assertEquals( "ccm co \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "", cl.toString() );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals( "ccm co " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"", cl.toString() );
            }
            else
            {
                assertEquals( "ccm co " + f.getCanonicalPath() + " " + f2.getCanonicalPath(), cl.toString() );
            }
        }
    }

    public void testCheckoutProject()
        throws Exception
    {
        Commandline cl = SynergyCCM.checkoutProject( null, "MyProject", new ScmTag( "MyVersion" ), "MyPurpose",
                                                     "MyRelease", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm co -subprojects -rel -t MyVersion -purpose MyPurpose -release MyRelease -p MyProject", cl
            .toString() );
        File f = File.createTempFile( "test", null );
        cl = SynergyCCM.checkoutProject( f.getParentFile(), "MyProject", new ScmTag( "MyVersion" ), "MyPurpose",
                                         "MyRelease", "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertEquals( "ccm co -subprojects -rel -t MyVersion -purpose MyPurpose -release MyRelease -path \"" +
                f.getParentFile().getCanonicalPath() + "\" -p MyProject", cl.toString() );
        }
        else
        {
            assertEquals( "ccm co -subprojects -rel -t MyVersion -purpose MyPurpose -release MyRelease -path " +
                f.getParentFile().getCanonicalPath() + " -p MyProject", cl.toString() );
        }
    }

    public void testCheckinProject()
        throws ScmException
    {
        Commandline cl = SynergyCCM.checkinProject( "MyProject", "a comment", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm ci -c \"a comment\" -p MyProject", cl.toString() );
    }

    public void testCheckinFiles()
        throws Exception
    {
        File f = File.createTempFile( "test", null );
        List list = new LinkedList();
        list.add( f );
        Commandline cl = SynergyCCM.checkinFiles( list, "a comment", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            assertEquals( "ccm ci -c \"a comment\" \"" + f.getCanonicalPath() + "\"", cl.toString() );
        }
        else
        {
            assertEquals( "ccm ci -c \"a comment\" " + f.getCanonicalPath(), cl.toString() );
        }
        File f2 = File.createTempFile( "test", null );
        list.add( f2 );
        cl = SynergyCCM.checkinFiles( list, "a comment", "CCM_ADDR" );
        if ( f.getCanonicalPath().indexOf( " " ) > -1 )
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals(
                    "ccm ci -c \"a comment\" \"" + f.getCanonicalPath() + "\" \"" + f2.getCanonicalPath() + "\"",
                    cl.toString() );
            }
            else
            {
                assertEquals( "ccm ci -c \"a comment\" \"" + f.getCanonicalPath() + "\" " + f2.getCanonicalPath() + "",
                              cl.toString() );
            }
        }
        else
        {
            if ( f2.getCanonicalPath().indexOf( " " ) > -1 )
            {
                assertEquals( "ccm ci -c \"a comment\" " + f.getCanonicalPath() + " \"" + f2.getCanonicalPath() + "\"",
                              cl.toString() );
            }
            else
            {
                assertEquals( "ccm ci -c \"a comment\" " + f.getCanonicalPath() + " " + f2.getCanonicalPath(), cl
                    .toString() );
            }
        }
    }

    public void testSync()
        throws ScmException
    {
        Commandline cl = SynergyCCM.synchronize( "myProject", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm sync -r -p myProject", cl.toString() );
    }

    public void testShowWorkArea()
        throws ScmException
    {
        Commandline cl = SynergyCCM.showWorkArea( "MyProject~1", "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm wa -show MyProject~1", cl.toString() );
    }

    public void testStart()
        throws ScmException
    {
        Commandline cl = SynergyCCM.start( "user", "pass", SynergyRole.BUILD_MGR );
        assertEquals( "ccm start -nogui -m -q -n user -pw pass -r build_mgr", cl.toString() );
    }

    public void testStop()
        throws ScmException
    {
        Commandline cl = SynergyCCM.stop( "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm stop", cl.toString() );
    }

    public void testDelimiter()
        throws ScmException
    {
        Commandline cl = SynergyCCM.delimiter( "CCM_ADDR" );
        assertTrue( "CCM_ADDR is not set.", assertContains( cl.getEnvironments(), "CCM_ADDR=CCM_ADDR" ) );
        assertEquals( "ccm delimiter", cl.toString() );
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
