package org.apache.maven.scm.provider.cvslib.command.update;

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

import java.io.File;

import org.apache.maven.scm.provider.cvslib.command.checkout.CvsCheckOutCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;
import org.apache.maven.scm.util.Commandline;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsUpdateCommandTest extends TestCase
{
    private CvsUpdateCommand instance;
    private String baseDir;

    /**
     * @param testName
     */
    public CvsUpdateCommandTest(String testName)
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
        instance = new CvsUpdateCommand();
    }

    public void testCommand()
    {
        try
        {
            CvsRepository repo = new CvsRepository();
            repo.setDelimiter(":");
            repo.setConnection(
                "pserver:anonymous@cvs.codehaus.org:/scm/cvspublic:test-repo");
            repo.setPassword("anonymous@cvs.codehaus.org");

            // check out before run update command 
            CvsCheckOutCommand coCmd = new CvsCheckOutCommand();
            coCmd.setRepository(repo);
            Commandline cocl = coCmd.getCommandLine();
            String workingDir = baseDir + "/target/testrepo/cvslib/update/";
            coCmd.setWorkingDirectory(workingDir);
            coCmd.execute();
            File fooFile = new File(workingDir + "test-repo/checkout/Foo.java");
            assertTrue(
                fooFile.getAbsolutePath() + " file doesn't exist",
                fooFile.exists());

            // we delete a file for test the update command execution
            fooFile.delete();
            assertTrue(
                fooFile.getAbsolutePath() + " file exists",
                !fooFile.exists());

            // Run the update command
            instance.setRepository(repo);
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                cl.getDefaultShell()+"cvs -d :pserver:anonymous@cvs.codehaus.org:/scm/cvspublic -q update test-repo",
                cl.toString());

            instance.setWorkingDirectory(workingDir);
            instance.execute();

            // test if update works fine
            File f1 = new File(workingDir + "test-repo/checkout/Foo.java");
            assertTrue(
                f1.getAbsolutePath() + " file doesn't exist",
                f1.exists());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testGetCommandWithTag()
    {
        try
        {
            CvsRepository repo = new CvsRepository();
            repo.setDelimiter(":");
            repo.setConnection(
                "pserver:anonymous@cvs.codehaus.org:/scm/cvspublic:test-repo");
            instance.setRepository(repo);
            instance.setTag("myTag");
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                cl.getDefaultShell()+"cvs -d :pserver:anonymous@cvs.codehaus.org:/scm/cvspublic -q update -rmyTag test-repo",
                cl.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testGetDisplayNameName()
    {
        try
        {
            assertEquals("Update", instance.getDisplayName());
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
            assertEquals("update", instance.getName());
        }
        catch(Exception e)
        {
            fail();
        }
    }
}
