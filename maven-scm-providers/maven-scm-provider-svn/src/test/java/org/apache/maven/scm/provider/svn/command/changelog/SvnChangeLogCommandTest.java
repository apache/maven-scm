package org.apache.maven.scm.provider.svn.command.changelog;

/*
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
 */

import java.util.Calendar;
import java.util.Date;

import org.apache.maven.scm.provider.svn.repository.SvnRepository;
import org.codehaus.plexus.util.cli.Commandline;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogCommandTest extends TestCase
{
    private SvnChangeLogCommand instance;
    private String baseDir;

    /**
     * @param testName
     */
    public SvnChangeLogCommandTest(String testName)
    {
        super(testName);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        baseDir = System.getProperty("basedir");
        assertNotNull("The system property basedir was not defined.", baseDir);
        instance = new SvnChangeLogCommand();
    }

    public void testGetCommandWithEndDate()
    {
        try
        {
            SvnRepository repo = new SvnRepository();
            repo.setDelimiter(":");
            repo.setConnection("http://foo.com/svn/trunk");
            instance.setRepository(repo);
            instance.setRange(30);
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
                "svn log --non-interactive -v -r \"{2003/09/10 GMT}:{2003/10/10 GMT}\" http://foo.com/svn/trunk",
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
            SvnRepository repo = new SvnRepository();
            repo.setDelimiter(":");
            repo.setConnection("http://foo.com/svn/trunk");
            instance.setRepository(repo);
            Calendar cal = Calendar.getInstance();
            cal.set(2003, 8, 10);
            Date startDate = cal.getTime();
            instance.setStartDate(startDate);
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "svn log --non-interactive -v -r \"{2003/09/10 GMT}:HEAD\" http://foo.com/svn/trunk",
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
            SvnRepository repo = new SvnRepository();
            repo.setDelimiter(":");
            repo.setConnection("anonymous@http://foo.com/svn/trunk");
            repo.setPassword("passwd");
            instance.setRepository(repo);
            instance.setBranch("3");
            instance.setWorkingDirectory(baseDir);
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "svn log --non-interactive -v -r 3 --username anonymous --password passwd http://foo.com/svn/trunk",
                cl.toString());
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testGetCommandWithoutBranchOrTag()
    {
        try
        {
            SvnRepository repo = new SvnRepository();
            repo.setDelimiter(":");
            repo.setConnection("anonymous@http://foo.com/svn/trunk");
            repo.setPassword("passwd");
            instance.setRepository(repo);
            instance.setWorkingDirectory(baseDir);
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "svn log --non-interactive -v --username anonymous --password passwd http://foo.com/svn/trunk",
                cl.toString());
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testGetDisplayNameName()
    {
        try
        {
            assertEquals("Changelog", instance.getDisplayName());
        }
        catch(Exception e)
        {
            fail();
        }
    }

    public void testGetName()
    {
        try
        {
            assertEquals("changelog", instance.getName());
        }
        catch(Exception e)
        {
            fail();
        }
    }

    public void testConsumer()
    {
        try
        {
            SvnChangeLogConsumer cons = new SvnChangeLogConsumer();
            instance.setConsumer(cons);
            assertEquals(cons, instance.getConsumer());
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testWrongConsumer()
    {
        try
        {
            instance.setConsumer(null);
            fail();
        }
        catch(Exception e)
        {
        }
    }
}
