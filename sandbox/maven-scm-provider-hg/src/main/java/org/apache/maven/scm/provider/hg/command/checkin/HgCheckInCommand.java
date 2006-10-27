package org.apache.maven.scm.provider.hg.command.checkin;

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
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommand;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.status.HgStatusCommand;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgCheckInCommand
    extends AbstractCheckInCommand
{

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                     String tag )
        throws ScmException
    {

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        // Get files that will be committed (if not specified in fileSet)
        List commitedFiles = new ArrayList();
        File[] files = fileSet.getFiles();
        if ( files.length == 0 )
        { //Either commit all changes
            HgStatusCommand statusCmd = new HgStatusCommand();
            statusCmd.setLogger( getLogger() );
            StatusScmResult status = statusCmd.executeStatusCommand( repo, fileSet );
            List statusFiles = status.getChangedFiles();
            for ( Iterator it = statusFiles.iterator(); it.hasNext(); )
            {
                ScmFile file = (ScmFile) it.next();
                if ( file.getStatus() == ScmFileStatus.ADDED || file.getStatus() == ScmFileStatus.DELETED
                    || file.getStatus() == ScmFileStatus.MODIFIED )
                {
                    commitedFiles.add( new ScmFile( file.getPath(), ScmFileStatus.CHECKED_IN ) );
                }
            }

        }
        else
        { //Or commit spesific files
            for ( int i = 0; i < files.length; i++ )
            {
                commitedFiles.add( new ScmFile( files[i].getPath(), ScmFileStatus.CHECKED_IN ) );
            }
        }

        // Commit to local branch
        String[] commitCmd = new String[] { HgCommand.COMMIT_CMD, HgCommand.MESSAGE_OPTION, message };
        commitCmd = HgUtils.expandCommandLine( commitCmd, fileSet );
        ScmResult result = HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), fileSet.getBasedir(),
                                                commitCmd );

        // Push to parent branch if any
        HgScmProviderRepository repository = (HgScmProviderRepository) repo;
        if ( !repository.getURI().equals( fileSet.getBasedir().getAbsolutePath() ) )
        {
            String[] push_cmd = new String[] { HgCommand.PUSH_CMD, repository.getURI() };
            result = HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), fileSet.getBasedir(),
                                          push_cmd );
        }

        return new CheckInScmResult( commitedFiles, result );
    }
}
