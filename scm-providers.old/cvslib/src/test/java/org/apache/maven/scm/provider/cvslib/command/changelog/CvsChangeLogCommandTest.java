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

import junit.framework.TestCase;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;
import org.apache.maven.scm.util.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsChangeLogCommandTest extends TestCase
{
    private CvsChangeLogCommand instance;
    private String baseDir;

    public CvsChangeLogCommandTest(String testName)
    {
        super(testName);
    }

    /**
     * Initialize per test data
     * @throws Exception when there is an unexpected problem
     */
    public void setUp() throws Exception
    {
        baseDir = System.getProperty("basedir");
        assertNotNull("The system property basedir was not defined.", baseDir);
        instance = new CvsChangeLogCommand();
    }

    public void testGetCommandWithEndDate()
    {
        try
        {
            CvsRepository repo = new CvsRepository();
            repo.setDelimiter(":");
            repo.setConnection(
                "pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven");
            instance.setRepository(repo);
            instance.setWorkingDirectory(baseDir);
            Calendar cal = Calendar.getInstance();
			cal.set(2003, 8, 10);
            Date startDate = cal.getTime();
            instance.setStartDate(startDate);
			cal.set(2003, 9, 10);
			Date endDate = cal.getTime();
			instance.setEndDate(endDate);
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                cl.getDefaultShell()+"cvs -d :pserver:anoncvs@cvs.apache.org:/home/cvspublic -q log -d2003-09-10<2003-10-10",
                cl.toString());
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
	public void testGetCommandWithoutEndDate()
	{
		try
		{
			CvsRepository repo = new CvsRepository();
			repo.setDelimiter(":");
			repo.setConnection(
				"pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven");
			instance.setRepository(repo);
			Calendar cal = Calendar.getInstance();
			cal.set(2003, 8, 10);
			Date startDate = cal.getTime();
			instance.setStartDate(startDate);
			Commandline cl = instance.getCommandLine();
			System.out.println(cl.toString());
			assertEquals(
				cl.getDefaultShell()+"cvs -d :pserver:anoncvs@cvs.apache.org:/home/cvspublic -q log -d>2003-09-10",
				cl.toString());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testGetCommandWithBranchOrTag()
	{
		try
		{
			CvsRepository repo = new CvsRepository();
			repo.setDelimiter(":");
			repo.setConnection(
				"pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven");
			instance.setRepository(repo);
			instance.setBranch("branchName");
			Commandline cl = instance.getCommandLine();
			System.out.println(cl.toString());
			assertEquals(
				cl.getDefaultShell()+"cvs -d :pserver:anoncvs@cvs.apache.org:/home/cvspublic -q log -rbranchName",
				cl.toString());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
    public void testSetDateRange()
    {
        instance.setRange(30);
        assertNotNull(instance.getStartDate());
        assertNotNull(instance.getEndDate());
    }
    
    public void testSetStartDate()
    {
        Calendar cal = Calendar.getInstance();
		cal.set(2003, 12, 13);
        Date startDate = cal.getTime();
        instance.setStartDate(startDate);
        assertEquals(startDate, instance.getStartDate());
    }
    
    public void testSetEndDate()
    {
        Calendar cal = Calendar.getInstance();
		cal.set(2003, 12, 13);
        Date endDate = cal.getTime();
        instance.setEndDate(endDate);
        assertEquals(endDate, instance.getEndDate());
    }
    
    public void testGetName()
    {
        assertEquals("changelog", instance.getName());
    }
    
    public void testGetDisplayName()
    {
        try
        {
            assertEquals("ChangeLog", instance.getDisplayName());
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testSetValidConsumer()
    {
        try
        {
            CvsChangeLogConsumer cons = new CvsChangeLogConsumer();
            instance.setConsumer(cons);
            assertEquals(cons, instance.getConsumer());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testSetInvalidConsumer()
    {
        try
        {
            instance.setConsumer(null);
            fail();
        }
        catch(ScmException e)
        {
        }
    }
}
