package org.apache.maven.scm.provider.svn.svnjava.util;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.tmatesoft.svn.core.wc.ISVNStatusHandler} implementation for most
 * scm commands.  Creates a list of {@link org.apache.maven.scm.ScmFile} objects and determines
 * the {@link org.apache.maven.scm.ScmFileStatus} from the
 * {@link org.tmatesoft.svn.core.wc.SVNEvent#getExpectedAction()}.
 *
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 */
public class ScmFileEventHandler
    implements ISVNEventHandler
{
    private final ScmLogger logger;

    private final List files = new ArrayList();

    private final File baseDirectory;

    /**
     * The logger is used in alerting the user to unknown file statuses.
     */
    public ScmFileEventHandler( ScmLogger logger, File baseDirectory )
    {
        this.logger = logger;

        this.baseDirectory = baseDirectory;
    }

    /**
     * Creates a {@link ScmFile} for each event with the exception of directories.
     * Directory events are ignored.
     */
    public void handleEvent( SVNEvent event, double progress )
    {
        ScmFileStatus status = SvnJavaUtil.getScmFileStatus( event.getExpectedAction() );

        // Do nothing for events without files
        if ( event.getFile() == null || event.getExpectedAction() == SVNEventAction.COMMIT_DELTA_SENT ||
            event.getExpectedAction() == SVNEventAction.COMMIT_COMPLETED || event.getNodeKind() != SVNNodeKind.FILE )
        {
            return;
        }

        if ( status == null )
        {
            logger.info( "Unknown SVN file status: '" + event.getExpectedAction() + "' for file: " +
                event.getFile().getAbsolutePath() );

            status = ScmFileStatus.UNKNOWN;
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( StringUtils.defaultString( status, event.getContentsStatus().toString() ) + " - " +
                event.getFile().getAbsolutePath() );
        }

        String currentFile = event.getFile().getAbsolutePath();
        if ( currentFile.startsWith( baseDirectory.getAbsolutePath() ) )
        {
            currentFile = currentFile.substring( baseDirectory.getAbsolutePath().length() + 1 );
        }

        files.add( new ScmFile( currentFile, status ) );
    }

    public void checkCancelled()
        throws SVNCancelException
    {
        // null
    }

    /**
     * Returns the list of files collected from handling events.
     *
     * @return a list of {@link ScmFile} objects
     */
    public List getFiles()
    {
        return files;
    }
}