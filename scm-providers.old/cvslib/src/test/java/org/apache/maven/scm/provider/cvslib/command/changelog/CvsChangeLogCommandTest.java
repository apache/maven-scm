package org.apache.maven.scm.provider.cvslib.command.changelog;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import java.util.Calendar;
import java.util.Date;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @version $Id: CvsChangeLogCommandTest.java,v 1.4 2004/05/02 22:10:58 jvanzyl
 *          Exp $
 */
public class CvsChangeLogCommandTest
    extends AbstractCvsScmTest
{
    public void testGetCommandWithEndDate()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        CvsRepository repo = new CvsRepository();

        repo.setDelimiter( ":" );

        repo.setConnection( "pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven" );

        instance.setRepository( repo );

        instance.setWorkingDirectory( getBasedir() );

        Calendar cal = Calendar.getInstance();
        cal.set( 2003, 8, 10 );
        Date startDate = cal.getTime();

        cal.set( 2003, 9, 10 );
        Date endDate = cal.getTime();

        instance.setStartDate( startDate );
        instance.setEndDate( endDate );

        Commandline cl = instance.getCommandLine();

        assertEquals(
            "cvs -d :pserver:anoncvs@cvs.apache.org:/home/cvspublic -q log -d2003-09-10<2003-10-10",
            cl.toString() );
    }

    public void testGetCommandWithoutEndDate()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        CvsRepository repo = new CvsRepository();

        repo.setDelimiter( ":" );
        repo.setConnection( "pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven" );
        instance.setRepository( repo );
        Calendar cal = Calendar.getInstance();
        cal.set( 2003, 8, 10 );
        Date startDate = cal.getTime();
        instance.setStartDate( startDate );

        Commandline cl = instance.getCommandLine();

        assertEquals(
            "cvs -d :pserver:anoncvs@cvs.apache.org:/home/cvspublic -q log -d>2003-09-10", cl
                .toString() );
    }

    public void testGetCommandWithBranchOrTag()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        CvsRepository repo = new CvsRepository();

        repo.setDelimiter( ":" );

        repo.setConnection( "pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven" );

        instance.setRepository( repo );

        instance.setBranch( "branchName" );

        Commandline cl = instance.getCommandLine();

        assertEquals( "cvs -d :pserver:anoncvs@cvs.apache.org:/home/cvspublic -q log -rbranchName",
            cl.toString() );
    }

    public void testSetDateRange()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        instance.setRange( 30 );

        assertNotNull( instance.getStartDate() );

        assertNotNull( instance.getEndDate() );
    }

    public void testSetStartDate()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        Calendar cal = Calendar.getInstance();

        cal.set( 2003, 12, 13 );

        Date startDate = cal.getTime();

        instance.setStartDate( startDate );

        assertEquals( startDate, instance.getStartDate() );
    }

    public void testSetEndDate()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        Calendar cal = Calendar.getInstance();

        cal.set( 2003, 12, 13 );

        Date endDate = cal.getTime();

        instance.setEndDate( endDate );

        assertEquals( endDate, instance.getEndDate() );
    }

    public void testGetName()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        assertEquals( "changelog", instance.getName() );
    }

    public void testGetDisplayName()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        assertEquals( "ChangeLog", instance.getDisplayName() );
    }

    public void testSetValidConsumer()
        throws Exception
    {
        CvsChangeLogCommand instance = new CvsChangeLogCommand();

        CvsChangeLogConsumer cons = new CvsChangeLogConsumer();

        instance.setConsumer( cons );

        assertEquals( cons, instance.getConsumer() );
    }

    public void testSetInvalidConsumer()
    {
        try
        {
            CvsChangeLogCommand instance = new CvsChangeLogCommand();

            instance.setConsumer( null );

            fail( "Expected exception." );
        }
        catch ( ScmException e )
        {
            // expected
        }
    }
}