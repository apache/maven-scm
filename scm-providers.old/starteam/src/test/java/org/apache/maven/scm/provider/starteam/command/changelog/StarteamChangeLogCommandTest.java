package org.apache.maven.scm.provider.starteam.command.changelog;

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
import org.apache.maven.scm.provider.starteam.repository.StarteamRepository;
import org.apache.maven.scm.util.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamChangeLogCommandTest extends TestCase
{
    private StarteamChangeLogCommand instance;
    private StarteamRepository repo;
    private String baseDir;

    public StarteamChangeLogCommandTest(String testName)
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
        repo = new StarteamRepository();
        repo.setDelimiter(":");
        repo.setConnection("myusername:mypassword@myhost:1234/projecturl");
        instance = new StarteamChangeLogCommand();
        instance.setRepository(repo);
    }
    
    public void testGetCommandLine()
    {
        try
        {
            instance.setWorkingDirectory(baseDir);
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "stcmd hist -x -nologo -is -p myusername:mypassword@myhost:1234/projecturl",
                cl.toString());
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testGetCommandLineWithTag()
    {
        try
        {
            instance.setTag("myTag");
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "stcmd hist -x -nologo -is -p myusername:mypassword@myhost:1234/projecturl -vl myTag",
                cl.toString());
        }
        catch(Exception e)
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
            StarteamChangeLogConsumer cons = new StarteamChangeLogConsumer();
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
