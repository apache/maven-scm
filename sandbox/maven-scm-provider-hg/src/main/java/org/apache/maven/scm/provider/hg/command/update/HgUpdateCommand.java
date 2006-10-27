package org.apache.maven.scm.provider.hg.command.update;

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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommand;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.changelog.HgChangeLogCommand;
import org.apache.maven.scm.provider.hg.command.diff.HgDiffConsumer;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgUpdateCommand
    extends AbstractUpdateCommand
    implements HgCommand
{

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        File workingDir = fileSet.getBasedir();

        // Update branch
        String[] update_cmd = new String[] { HgCommand.PULL_CMD };
        ScmResult updateResult = HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), workingDir,
                                                      update_cmd );

        if ( !updateResult.isSuccess() )
        {
            return new UpdateScmResult( null, null, updateResult );
        }

        // Find changes from last revision
        int currentRevision = HgUtils.getCurrentRevisionNumber( getLogger(), workingDir );
        int previousRevision = currentRevision - 1;
        String[] diffCmd = new String[] { DIFF_CMD, REVISION_OPTION, "" + previousRevision };
        HgDiffConsumer diffConsumer = new HgDiffConsumer( getLogger(), workingDir );
        ScmResult diffResult = HgUtils.execute( diffConsumer, getLogger(), workingDir, diffCmd );

        // Now translate between diff and update file status
        List updatedFiles = new ArrayList();
        List changes = new ArrayList();
        List diffFiles = diffConsumer.getChangedFiles();
        Map diffChanges = diffConsumer.getDifferences();
        for ( Iterator it = diffFiles.iterator(); it.hasNext(); )
        {
            ScmFile file = (ScmFile) it.next();
            changes.add( diffChanges.get( file.getPath() ) );
            if ( file.getStatus() == ScmFileStatus.MODIFIED )
            {
                updatedFiles.add( new ScmFile( file.getPath(), ScmFileStatus.PATCHED ) );
            }
            else
            {
                updatedFiles.add( file );
            }
        }

        return new UpdateScmResultWithRevision( updatedFiles, changes, String.valueOf( currentRevision ), diffResult );
    }

    protected ChangeLogCommand getChangeLogCommand()
    {
        HgChangeLogCommand command = new HgChangeLogCommand();
        command.setLogger( getLogger() );
        return command;
    }
}
