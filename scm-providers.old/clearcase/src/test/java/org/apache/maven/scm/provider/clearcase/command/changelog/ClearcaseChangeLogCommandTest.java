package org.apache.maven.scm.provider.clearcase.command.changelog;

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
import org.apache.maven.scm.provider.clearcase.repository.ClearcaseRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ClearcaseChangeLogCommandTest extends TestCase
{
    private ClearcaseChangeLogCommand instance;
    private ClearcaseRepository repo;
    private String baseDir;

    public ClearcaseChangeLogCommandTest(String testName)
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
        repo = new ClearcaseRepository();
        repo.setDelimiter(":");
        repo.setConnection("");
        instance = new ClearcaseChangeLogCommand();
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
                "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n\" -recurse -nco",
                cl.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testGetCommandLineWithTag()
    {
        try
        {
            instance.setBranch("myBranch");
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n\" -recurse -nco -branch myBranch",
                cl.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testSetDateRange()
    {
        instance.setRange(30);
        assertNotNull(instance.getStartDate());
        assertNull(instance.getEndDate());
    }
    
    public void testSetStartDate()
    {
        Calendar cal = Calendar.getInstance();
		cal.set(2003, 11, 13);
        Date startDate = cal.getTime();
        instance.setStartDate(startDate);
        assertEquals(startDate, instance.getStartDate());
    }
    
    public void testSetEndDate()
    {
        Calendar cal = Calendar.getInstance();
		cal.set(2003, 11, 13);
        Date endDate = cal.getTime();
        instance.setEndDate(endDate);
        assertNull(instance.getEndDate());
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
            ClearcaseChangeLogConsumer cons = new ClearcaseChangeLogConsumer();
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