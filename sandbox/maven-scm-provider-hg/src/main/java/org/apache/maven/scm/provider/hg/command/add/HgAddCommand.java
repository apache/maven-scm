package org.apache.maven.scm.provider.hg.command.add;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommand;

import java.io.File;
import java.util.Iterator;

/**
 * Add no recursive.
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgAddCommand
    extends AbstractAddCommand
    implements HgCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        //String[] addCmd = new String[] { ADD_CMD, NO_RECURSE_OPTION };
        String[] addCmd = new String[] { ADD_CMD, VERBOSE_OPTION };
        addCmd = HgUtils.expandCommandLine( addCmd, fileSet );

        File workingDir = fileSet.getBasedir();
        HgAddConsumer consumer = new HgAddConsumer( getLogger(), workingDir );
        ScmResult result = HgUtils.execute( consumer, getLogger(), workingDir, addCmd );

        AddScmResult addScmResult = new AddScmResult( consumer.getAddedFiles(), result );

        // add in bogus 'added' results for empty directories.  only need to do this because the maven scm unit test
        // seems to think that this is the way we should behave.  it's pretty hacky. -rwd
        for ( Iterator iterator = fileSet.getFileList().iterator(); iterator.hasNext(); )
        {
            File workingFile = (File) iterator.next();
            File file = new File( workingDir, workingFile.getPath() );
            if ( file.isDirectory() && file.listFiles().length == 0 )
            {
                addScmResult.getAddedFiles().add( workingFile );
            }
        }

        return addScmResult;
    }
}
