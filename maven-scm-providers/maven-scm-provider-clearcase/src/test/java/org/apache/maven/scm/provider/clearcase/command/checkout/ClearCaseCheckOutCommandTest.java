package org.apache.maven.scm.provider.clearcase.command.checkout;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @author <a href="mailto:frederic.mura@laposte.net">Frederic Mura</a>
 */
public class ClearCaseCheckOutCommandTest
    extends ScmTestCase
{
    public void testCreateViewCommandLine()
        throws IOException
    {
        ClearCaseCheckOutCommand.setIsClearCaseLT( false );
        Commandline commandLine =
            ClearCaseCheckOutCommand.createCreateViewCommandLine( getWorkingDirectory(), "testView" );
        assertEquals( "cleartool mkview -snapshot -tag testView -vws " + ClearCaseCheckOutCommand.getViewStore() +
            "testView.vws " + getWorkingDirectory(), commandLine.toString() );

        ClearCaseCheckOutCommand.setUseVWS( false );
        commandLine = ClearCaseCheckOutCommand.createCreateViewCommandLine( getWorkingDirectory(), "testView" );
        assertEquals( "cleartool mkview -snapshot -tag testView " + getWorkingDirectory(), commandLine.toString() );

        ClearCaseCheckOutCommand.setIsClearCaseLT( true );
        ClearCaseCheckOutCommand.setUseVWS( true );
        commandLine = ClearCaseCheckOutCommand.createCreateViewCommandLine( getWorkingDirectory(), "testView" );
        assertEquals( "cleartool mkview -snapshot -tag testView " + getWorkingDirectory(), commandLine.toString() );
    }

    public void testUpdateConfigSpec()
    {
        ClearCaseCheckOutCommand.setIsClearCaseLT( false );
        File configSpecLocation = new File( "\\\\myserver\\configspecs\\testconfigspec.txt" );
        Commandline commandLine = ClearCaseCheckOutCommand.createUpdateConfigSpecCommandLine( getWorkingDirectory(),
                                                                                              configSpecLocation,
                                                                                              "testView" );
        assertEquals( "cleartool setcs -tag testView " + configSpecLocation, commandLine.toString() );

        ClearCaseCheckOutCommand.setIsClearCaseLT( true );
        commandLine = ClearCaseCheckOutCommand.createUpdateConfigSpecCommandLine( getWorkingDirectory(),
                                                                                  configSpecLocation, "testView" );
        assertEquals( "cleartool setcs -tag testView " + configSpecLocation, commandLine.toString() );
    }
}
