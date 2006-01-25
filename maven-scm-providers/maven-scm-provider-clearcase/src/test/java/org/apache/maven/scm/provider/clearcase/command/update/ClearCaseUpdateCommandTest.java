package org.apache.maven.scm.provider.clearcase.command.update;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 */
public class ClearCaseUpdateCommandTest
    extends ScmTestCase
{
    public void testCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory() );
        Commandline commandLine = ClearCaseUpdateCommand.createCommandLine( scmFileSet );
        assertEquals( "cleartool update", commandLine.toString() );
    }
}
