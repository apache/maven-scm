package org.apache.maven.scm.provider.svn.svnjava.command.status;

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
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnStatusHandler;
import org.tmatesoft.svn.core.SVNException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnStatusCommand
    extends AbstractStatusCommand
    implements SvnCommand
{
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        getLogger().info( "SVN status directory: " + fileSet.getBasedir().getAbsolutePath() );

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        SvnStatusHandler handler = new SvnStatusHandler();

        try
        {
            SvnJavaUtil.status( javaRepo.getClientManager(), fileSet.getBasedir(), true, // isRecursive
                                true, // isRemote                                 
                                handler );

            return new StatusScmResult( SvnJavaScmProvider.COMMAND_LINE, handler.getFiles() );
        }
        catch ( SVNException e )
        {
            return new StatusScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN status failed.", e.getMessage(), false );
        }
    }
}
