package org.apache.maven.scm.provider.clearcase.command.checkin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 */
public class ClearCaseCheckInCommandTest extends ScmTestCase
{
    public void testCommand()
        throws ScmException
    {
        File file = new File( "test.java" );
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), file );
        Commandline commandLine = ClearCaseCheckInCommand.createCommandLine( scmFileSet, "done some changes" );

        assertEquals( "cleartool ci -c \"done some changes\" " + file.getAbsolutePath(), commandLine.toString() );
    }
}
