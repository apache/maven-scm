package org.apache.maven.scm.provider.svn.svnjava.command.checkin;

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
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnCheckInCommand
    extends AbstractCheckInCommand
    implements SvnCommand
{
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      String tag )
        throws ScmException
    {
        if ( StringUtils.isNotEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        getLogger().info( "SVN commit directory: " + fileSet.getBasedir().getAbsolutePath() );

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        SvnJavaUtil.GenericEventHandler handler = new SvnJavaUtil.GenericEventHandler();

        javaRepo.getClientManager().getCommitClient().setEventHandler( handler );

        try
        {
            File[] tmpPaths = fileSet.getFiles();
            File[] paths;
            if ( tmpPaths == null || tmpPaths.length == 0 )
            {
                paths = new File[]{fileSet.getBasedir()};
            }
            else
            {
                paths = new File[tmpPaths.length];
                for ( int i = 0; i < tmpPaths.length; i++ )
                {
                    if ( tmpPaths[i].isAbsolute() )
                    {
                        paths[i] = tmpPaths[i];
                    }
                    else
                    {
                        paths[i] = new File( fileSet.getBasedir(), tmpPaths[i].toString() );
                    }
                }
            }

            SvnJavaUtil.commit( javaRepo.getClientManager(), paths, false, message, true );

            List files = new ArrayList();
            for ( Iterator iter = handler.getEvents().iterator(); iter.hasNext(); )
            {
                SVNEvent event = (SVNEvent) iter.next();
                if ( event.getExpectedAction() != SVNEventAction.COMMIT_COMPLETED &&
                    event.getExpectedAction() != SVNEventAction.COMMIT_DELTA_SENT &&
                    event.getNodeKind() == SVNNodeKind.FILE )
                {
                    files.add( new ScmFile( event.getFile().toString(), ScmFileStatus.CHECKED_IN ) );
                }
            }

            return new CheckInScmResult( SvnJavaScmProvider.COMMAND_LINE, files );
        }
        catch ( SVNException e )
        {
            return new CheckInScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN commit failed.", e.getMessage(), false );
        }
        finally
        {
            javaRepo.getClientManager().getCommitClient().setEventHandler( null );
        }
    }
}
