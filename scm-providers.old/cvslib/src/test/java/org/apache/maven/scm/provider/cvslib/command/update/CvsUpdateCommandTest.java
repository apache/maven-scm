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

import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.command.checkout.CvsCheckOutCommand;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsUpdateCommandTest
    extends AbstractCvsScmTest
{
    public void testCommand()
        throws Exception
    {
        CvsCheckOutCommand coCmd = (CvsCheckOutCommand)createCommand( "checkout", CvsCheckOutCommand.class );
        coCmd.setWorkingDirectory(getWorkingDirectory());
        coCmd.execute();

        File fooFile = new File(getWorkingDirectory() + "/test-repo/checkout/Foo.java");
        assertTrue(
            fooFile.getAbsolutePath() + " file doesn't exist",
            fooFile.exists());

        // we delete a file for test the update command execution
        fooFile.delete();
        assertTrue(
            fooFile.getAbsolutePath() + " file exists",
            !fooFile.exists());

        // Run the update command
        CvsUpdateCommand instance = (CvsUpdateCommand)createCommand( "update", CvsUpdateCommand.class );

        Commandline cl = instance.getCommandLine();
        System.out.println(cl.toString());
        assertEquals(
            "cvs -d " + getRepositoryPath() + " -q update test-repo",
            cl.toString());

        instance.setWorkingDirectory(getWorkingDirectory());
        instance.execute();

        // test if update works fine
        File f1 = new File(getWorkingDirectory() + "/test-repo/checkout/Foo.java");
        assertTrue(
            f1.getAbsolutePath() + " file doesn't exist",
            f1.exists());
    }

    public void testGetCommandWithTag()
        throws Exception
    {
        CvsUpdateCommand instance = (CvsUpdateCommand)createCommand( "update", CvsUpdateCommand.class );

        instance.setTag("myTag");
        Commandline cl = instance.getCommandLine();
        System.out.println(cl.toString());
        assertEquals(
            "cvs -d " + getRepositoryPath() + " -q update -rmyTag test-repo",
            cl.toString());
    }

    public void testGetDisplayNameName()
    {
        assertEquals("Update", new CvsUpdateCommand().getDisplayName());
    }

    public void testGetName()
    {
        assertEquals("update", new CvsUpdateCommand().getName());
    }
}
