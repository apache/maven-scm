package org.apache.maven.scm.provider.svn.command.checkout;

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

import org.apache.maven.scm.provider.svn.repository.SvnRepository;
import org.codehaus.plexus.util.cli.Commandline;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnCheckOutCommandTest extends TestCase
{
    private SvnCheckOutCommand instance;
    private String baseDir;

    /**
      * @param testName
      */
    public SvnCheckOutCommandTest(String testName)
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
        instance = new SvnCheckOutCommand();
    }

    public void testGetCommand()
    {
        try
        {
            SvnRepository repo = new SvnRepository();
            repo.setDelimiter(":");
            repo.setConnection("anonymous@http://foo.com/svn/trunk");
            repo.setPassword("passwd");
            instance.setRepository(repo);
            instance.setTag("10");
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "svn checkout --non-interactive -v -r 10 --username anonymous --password passwd http://foo.com/svn/trunk",
                cl.toString());
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testGetCommand2()
    {
        try
        {
            SvnRepository repo = new SvnRepository();
            repo.setDelimiter(":");
            repo.setConnection("http://foo.com/svn/trunk");
            instance.setRepository(repo);
            instance.setWorkingDirectory(baseDir);
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "svn checkout --non-interactive -v http://foo.com/svn/trunk",
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
            assertEquals("Check out", instance.getDisplayName());
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
            assertEquals("checkout", instance.getName());
        }
        catch(Exception e)
        {
            fail();
        }
    }
}
