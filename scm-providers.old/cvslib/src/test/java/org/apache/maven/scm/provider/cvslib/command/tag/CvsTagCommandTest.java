package org.apache.maven.scm.provider.cvslib.command.tag;

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
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;
import org.apache.maven.scm.util.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsTagCommandTest extends TestCase
{
    private CvsTagCommand instance;
    private String baseDir;

    /**
     * @param testName
     */
    public CvsTagCommandTest(String testName)
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
        instance = new CvsTagCommand();
    }

    public void testGetCommandWithTag()
    {
        try
        {
            CvsRepository repo = new CvsRepository();
            repo.setDelimiter(":");
            repo.setConnection(
                "pserver:anonymous@cvs.codehaus.org:/scm/cvspublic:test-repo");
            repo.setPassword("anonymous@cvs.codehaus.org");

            instance.setRepository(repo);
            instance.setTagName("my_tag");
            Commandline cl = instance.getCommandLine();
            System.out.println(cl.toString());
            assertEquals(
                cl.getDefaultShell()+"cvs -d :pserver:anonymous@cvs.codehaus.org:/scm/cvspublic -q tag -c my_tag",
                cl.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testGetCommandWithoutTag()
    {
        try
        {
            CvsRepository repo = new CvsRepository();
            repo.setDelimiter(":");
            repo.setConnection(
                "pserver:anonymous@cvs.codehaus.org:/scm/cvspublic:test-repo");
            repo.setPassword("anonymous@cvs.codehaus.org");

            instance.setRepository(repo);
            Commandline cl = instance.getCommandLine();
            fail("an exception must be throw");
        }
        catch(ScmException e)
        {
        }
    }
    
    public void testGetDisplayNameName()
    {
        try
        {
            assertEquals("Tag", instance.getDisplayName());
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
            assertEquals("tag", instance.getName());
        }
        catch(Exception e)
        {
            fail();
        }
    }
}
