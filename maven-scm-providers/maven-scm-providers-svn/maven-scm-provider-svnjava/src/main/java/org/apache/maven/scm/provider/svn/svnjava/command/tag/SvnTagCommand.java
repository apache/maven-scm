package org.apache.maven.scm.provider.svn.svnjava.command.tag;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.codehaus.plexus.util.FileUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @todo since this is just a copy, use that instead.
 */
public class SvnTagCommand
    extends AbstractTagCommand
    implements SvnCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag,
                                           String message )
        throws ScmException
    {
        return executeTagCommand( repo, fileSet, tag, new ScmTagParameters(message) );
    }

    protected ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag,
                                           ScmTagParameters scmTagParameters )
        throws ScmException
    {
        if ( tag == null )
        {
            throw new ScmException( "tag must be specified" );
        }

        if ( fileSet.getFiles().length != 0 )
        {
            throw new ScmException( "This provider doesn't support tagging subsets of a directory" );
        }

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "SVN checkout directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        try
        {
            SVNURL destURL = SVNURL.parseURIEncoded( SvnTagBranchUtils.resolveTagUrl( repository, new ScmTag( tag ) ) );
            
            SVNCommitInfo info = SvnJavaUtil
                .copy( javaRepo.getClientManager(), javaRepo.getSvnUrl(), destURL, false, "[maven-scm] copy for tag "
                    + tag, scmTagParameters.getScmRevision() );

            if ( info.getError() != null )
            {
                return new TagScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN tag failed.", info.getError()
                    .getMessage(), false );
            }

            // The copy command doesn't return a list of files that were tagged,
            // so manually build the list from the contents of the fileSet.getBaseDir.
            List fileList = new ArrayList();
            List files = null;
            try
            {
                files = FileUtils.getFiles( fileSet.getBasedir(), "**", "**/.svn/**", false );
            }
            catch ( IOException e )
            {
                throw new ScmException( "Error while building list of tagged files.", e );
            }

            for ( Iterator i = files.iterator(); i.hasNext(); )
            {
                File f = (File) i.next();
                fileList.add( new ScmFile( f.getPath(), ScmFileStatus.TAGGED ) );
            }

            return new TagScmResult( SvnJavaScmProvider.COMMAND_LINE, fileList );
        }
        catch ( SVNException e )
        {
            return new TagScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN tag failed.", e.getMessage(), false );
        }
    }
   
}
