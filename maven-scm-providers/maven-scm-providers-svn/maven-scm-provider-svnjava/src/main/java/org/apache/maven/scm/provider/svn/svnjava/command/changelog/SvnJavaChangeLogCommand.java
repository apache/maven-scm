package org.apache.maven.scm.provider.svn.svnjava.command.changelog;

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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnChangeSet;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnJavaChangeLogCommand
    extends AbstractChangeLogCommand
    implements SvnCommand
{
    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "executeChangeLogCommand( ScmProviderRepository repository, "
            + "ScmFileSet fileSet, ScmVersion startVersion, ScmVersion endVersion, String datePattern ) is not "
            + "implemented" );
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        SVNRevision startRevision =
            ( startDate != null ) ? SVNRevision.create( startDate ) : SVNRevision.UNDEFINED;
        SVNRevision endRevision = ( endDate != null ) ? SVNRevision.create( endDate ) : SVNRevision.HEAD;

        try
        {
            SVNURL url = javaRepo.getSvnUrl();

            if ( branch != null )
            {
                url = SVNURL.parseURIEncoded( SvnTagBranchUtils.resolveBranchUrl( javaRepo, branch ) );
            }

            ChangeLogHandler handler = new ChangeLogHandler( startDate, endDate );

            SvnJavaUtil.changelog( javaRepo.getClientManager(), url, startRevision, endRevision, true, // stopOnCopy
                                   true, // reportPaths
                                   handler );

            return new ChangeLogScmResult( SvnJavaScmProvider.COMMAND_LINE, handler.getChangeSets() );
        }
        catch ( SVNException e )
        {
            return new ChangeLogScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN Changelog failed.",
                                           e.getMessage(), false );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected static class ChangeLogHandler
        implements ISVNLogEntryHandler
    {
        private ChangeLogSet changeLogSet;

        private List changeSets = new ArrayList();

        public ChangeLogHandler( Date startDate, Date endDate )
        {
            changeLogSet = new ChangeLogSet( startDate, endDate );
        }

        public void handleLogEntry( SVNLogEntry logEntry )
            throws SVNException
        {
            List changedFiles = new ArrayList();

            for ( Iterator i = logEntry.getChangedPaths().keySet().iterator(); i.hasNext(); )
            {
                changedFiles.add( new ChangeFile( (String) i.next() ) );
            }

            changeSets.add( new SvnChangeSet( logEntry.getDate(), logEntry.getMessage(), logEntry.getAuthor(),
                                              changedFiles ) );
        }

        public ChangeLogSet getChangeSets()
        {
            changeLogSet.setChangeSets( changeSets );

            return changeLogSet;
        }
    }
}
