package org.apache.maven.scm.provider.svn.svnjava.command.export;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.export.AbstractExportCommand;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.ScmFileEventHandler;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 */
public class SvnJavaExportCommand
    extends AbstractExportCommand
    implements SvnCommand
{

    /** 
     * @see org.apache.maven.scm.command.export.AbstractExportCommand#executeExportCommand(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.ScmVersion, java.lang.String)
     */
    protected ExportScmResult executeExportCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version,
                                                    String outputDirectory )
        throws ScmException
    {
        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        ScmFileEventHandler handler = new ScmFileEventHandler( getLogger(), fileSet.getBasedir() );

        javaRepo.getClientManager().getUpdateClient().setEventHandler( handler );
        String url = javaRepo.getUrl();

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmTag )
            {
                url = SvnTagBranchUtils.resolveTagUrl( javaRepo, (ScmTag) version );
            }
            else if ( version instanceof ScmBranch )
            {
                url = SvnTagBranchUtils.resolveBranchUrl( javaRepo, (ScmBranch) version );
            }
        }

        url = SvnCommandUtils.fixUrl( url, javaRepo.getUser() );
        try
        {
            SvnJavaUtil.export( javaRepo.getClientManager(), SVNURL.parseURIEncoded( url ), SVNRevision.HEAD, fileSet
                .getBasedir(), true );

            return new ExportScmResult( SvnJavaScmProvider.COMMAND_LINE, handler.getFiles() );
        }
        catch ( SVNException e )
        {
            return new ExportScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN checkout failed.", e.getMessage(), false );
        }
        finally
        {
            javaRepo.getClientManager().getUpdateClient().setEventHandler( null );
        }

    }

}
