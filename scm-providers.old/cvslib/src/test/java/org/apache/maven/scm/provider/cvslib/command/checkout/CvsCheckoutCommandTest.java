package org.apache.maven.scm.provider.cvslib.command.checkout;

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

import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsCheckoutCommandTest extends AbstractCvsScmTest
{
    public void testGetCommand()
        throws Exception
    {
        CvsCheckOutCommand instance = (CvsCheckOutCommand)createCommand( "checkout", CvsCheckOutCommand.class );
        Commandline cl = instance.getCommandLine();
        System.out.println(cl.toString());
        assertEquals(
            "cvs -d " + getRepositoryPath() + " -q checkout test-repo",
            cl.toString());

        // test if checkout works fine
        instance.execute();
        File f1 = new File(getWorkingDirectory() + "/test-repo/checkout/Readme.txt");
        assertTrue(
            f1.getAbsolutePath() + " file doesn't exist",
            f1.exists());
        File f2 = new File(getWorkingDirectory() + "/test-repo/checkout/Foo.java");
        assertTrue(
            f2.getAbsolutePath() + " file doesn't exist",
            f2.exists());
    }

    public void testGetCommandWithTag()
        throws Exception
    {
        CvsCheckOutCommand instance = (CvsCheckOutCommand)createCommand( "checkout", CvsCheckOutCommand.class );
        instance.setTag("myTag");
        Commandline cl = instance.getCommandLine();
        System.out.println(cl.toString());
        assertEquals(
            "cvs -d " + getRepositoryPath() + " -q checkout -rmyTag test-repo",
            cl.toString());
    }
    
    public void testGetDisplayNameName()
    {
        assertEquals("Check out", new CvsCheckOutCommand().getDisplayName());
    }
    
    public void testGetName()
    {
        assertEquals("checkout", new CvsCheckOutCommand().getName());
    }
}
