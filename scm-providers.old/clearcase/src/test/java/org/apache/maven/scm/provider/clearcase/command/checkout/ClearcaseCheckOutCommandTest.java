package org.apache.maven.scm.provider.clearcase.command.checkout;

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
import org.apache.maven.scm.provider.clearcase.repository.ClearcaseRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ClearcaseCheckOutCommandTest extends TestCase
{
    private String baseDir;
    private ClearcaseRepository repo;
    private ClearcaseCheckOutCommand cmd;
    
    /**
     * @param testName
     */
    public ClearcaseCheckOutCommandTest(String testName)
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
        cmd = new ClearcaseCheckOutCommand();
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
            cmd.setBranch("myBranch");
            Commandline cl = cmd.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                "cleartool co -branch myBranch",
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
                "cleartool co",
                cl.toString());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
}
