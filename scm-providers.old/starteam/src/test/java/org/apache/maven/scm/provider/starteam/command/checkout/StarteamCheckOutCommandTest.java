package org.apache.maven.scm.provider.starteam.command.checkout;

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

import junit.framework.TestCase;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.starteam.repository.StarteamRepository;
import org.apache.maven.scm.provider.starteam.command.changelog.StarteamChangeLogConsumer;
import org.apache.maven.scm.util.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamCheckOutCommandTest extends TestCase
{
    private String baseDir;
    private StarteamRepository repo;
    private StarteamCheckOutCommand cmd;
    
    /**
     * @param testName
     */
    public StarteamCheckOutCommandTest(String testName)
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
        cmd = new StarteamCheckOutCommand();
        cmd.setRepository(repo);
    }
    
    public void testGetDisplayNameName()
    {
        try
        {
            assertEquals("Check out", cmd.getDisplayName());
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
            assertEquals("checkout", cmd.getName());
        }
        catch(Exception e)
        {
            fail();
        }
    }
    
    public void testGetCommandLineWithWorkingDirectory()
    {
        try
        {
            cmd.setWorkingDirectory(baseDir);
            cmd.setTag("myTag");
            Commandline cl = cmd.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                cl.getDefaultShell()+"stcmd co -x -nologo -is -p myusername:mypassword@myhost:1234/projecturl -vl myTag",
                cl.toString());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testGetCommandLineWithoutWorkingDirectory()
    {
        try
        {
            Commandline cl = cmd.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                cl.getDefaultShell()+"stcmd co -x -nologo -is -p myusername:mypassword@myhost:1234/projecturl",
                cl.toString());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
}
