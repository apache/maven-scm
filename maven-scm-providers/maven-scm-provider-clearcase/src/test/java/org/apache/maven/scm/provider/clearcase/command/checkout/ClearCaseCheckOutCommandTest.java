package org.apache.maven.scm.provider.clearcase.command.checkout;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.clearcase.repository.ClearCaseScmProviderRepository;
import org.apache.maven.scm.providers.clearcase.settings.Settings;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @author <a href="mailto:frederic.mura@laposte.net">Frederic Mura</a>
 */
public class ClearCaseCheckOutCommandTest
    extends ScmTestCase
{
    private Settings settings = null;
    private ClearCaseCheckOutCommand checkOutCommand = null;
    
    public void setUp() throws Exception 
    {
        super.setUp();
        checkOutCommand = new ClearCaseCheckOutCommand();
        checkOutCommand.setLogger(new DefaultLog());
        settings = new Settings();
        checkOutCommand.setSettings(settings);
    }
    
    public void testCreateViewCommandLine()
        throws IOException
    {
        String viewName = "testView";
        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_DEFAULT);
        
        Commandline commandLine =
            checkOutCommand.createCreateViewCommandLine( getWorkingDirectory(), viewName, null );
        assertEquals( "cleartool mkview -snapshot -tag testView -vws " + checkOutCommand.getViewStore() +
            "testView.vws " + getWorkingDirectory().getCanonicalPath(), commandLine.toString() );

        settings.setUseVWSParameter(false);
        commandLine = checkOutCommand.createCreateViewCommandLine( getWorkingDirectory(), viewName, null );
        assertEquals( "cleartool mkview -snapshot -tag testView " + getWorkingDirectory().getCanonicalPath(),
                      commandLine.toString() );

        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_LT);
        settings.setUseVWSParameter(true);
        commandLine = checkOutCommand.createCreateViewCommandLine( getWorkingDirectory(), viewName, null );
        assertEquals( "cleartool mkview -snapshot -tag testView " + getWorkingDirectory().getCanonicalPath(),
                      commandLine.toString() );
        
        settings.setUseVWSParameter(false);
        commandLine = checkOutCommand.createCreateViewCommandLine( getWorkingDirectory(), viewName, null );
        assertEquals( "cleartool mkview -snapshot -tag testView " + getWorkingDirectory().getCanonicalPath(),
                      commandLine.toString() );
        
        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_UCM);
        String streamId = "streamIdentifier";
        commandLine = checkOutCommand.createCreateViewCommandLine( getWorkingDirectory(), viewName, streamId );
        assertEquals( "cleartool mkview -snapshot -tag testView -stream " + streamId + " " + 
            getWorkingDirectory().getCanonicalPath(), commandLine.toString() );
        
        settings.setUseVWSParameter(true);
        commandLine = checkOutCommand.createCreateViewCommandLine( getWorkingDirectory(), viewName, streamId );
        assertEquals( "cleartool mkview -snapshot -tag testView -stream " + streamId + " -vws " + checkOutCommand.getViewStore() +
            "testView.vws " + getWorkingDirectory().getCanonicalPath(), commandLine.toString() );
    }

    public void testUpdateConfigSpec()
    {
        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_DEFAULT);

        File configSpecLocation;
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) >= 0 )
        {
            configSpecLocation = new File( "\\\\myserver\\configspecs\\testconfigspec.txt" );
        }
        else
        {
            configSpecLocation = new File( "/clearcase/configspecs/testconfigspec.txt" );
        }

        Commandline commandLine = checkOutCommand.createUpdateConfigSpecCommandLine( getWorkingDirectory(),
                                                                                              configSpecLocation,
                                                                                              "testView" );
        assertEquals( "cleartool setcs -tag testView " + configSpecLocation, commandLine.toString() );

        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_LT);
        commandLine = checkOutCommand.createUpdateConfigSpecCommandLine( getWorkingDirectory(),
                                                                                  configSpecLocation, "testView" );
        assertEquals( "cleartool setcs -tag testView " + configSpecLocation, commandLine.toString() );
    }

    public void testCreateConfigSpec()
    {
        assertEquals( "element * CHECKEDOUT\n" + "element * /main/LATEST\n" + "load MYVOB/my/dir\n",
                checkOutCommand.createConfigSpec( "MYVOB/my/dir", null ) );
        assertEquals( "element * CHECKEDOUT\n" + "element * MYTAG\n" + "element -directory * /main/LATEST\n" +
            "load MYVOB/my/dir\n", checkOutCommand
            .createConfigSpec( "MYVOB/my/dir", new ScmBranch( "MYTAG" ) ) );
    }
    
    public void testGetStreamIdentifier()
    {
        String streamName = "stream35_v1.0";
        String vobName = "pVob_35";
        String streamIdentifier = checkOutCommand.getStreamIdentifier(streamName, vobName);
        assertEquals("stream:" + streamName + "@" + vobName, streamIdentifier);
        
        streamIdentifier = checkOutCommand.getStreamIdentifier(streamName, null);
        assertNull(streamIdentifier);
        
        streamIdentifier = checkOutCommand.getStreamIdentifier(null, vobName);
        assertNull(streamIdentifier);
    }
}
