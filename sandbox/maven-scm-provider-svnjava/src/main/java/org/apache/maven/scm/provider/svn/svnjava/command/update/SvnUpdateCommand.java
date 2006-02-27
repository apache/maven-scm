package org.apache.maven.scm.provider.svn.svnjava.command.update;

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
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.command.changelog.SvnChangeLogCommand;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.ScmFileEventHandler;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnUpdateCommand
    extends AbstractUpdateCommand
    implements SvnCommand
{

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        getLogger().info( "SVN update directory: " + fileSet.getBasedir().getAbsolutePath() );

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        try
        {
            ScmFileEventHandler handler = new ScmFileEventHandler( getLogger(), fileSet.getBasedir() );

            javaRepo.getClientManager().getUpdateClient().setEventHandler( handler );

            if ( tag == null || SvnTagBranchUtils.isRevisionSpecifier( tag ) )
            {
                SvnJavaUtil.update( javaRepo.getClientManager(), fileSet.getBasedir(), getSVNRevision( tag ), true );
            }
            else
            {
                // The tag specified does not appear to be numeric, so assume it refers 
                // to a branch/tag url and perform a switch operation rather than update
                SvnJavaUtil.switchToURL( javaRepo.getClientManager(), fileSet.getBasedir(),
                                         SVNURL.parseURIEncoded( SvnTagBranchUtils.resolveTagUrl( repository, tag ) ),
                                         SVNRevision.HEAD, true );
            }

            return new UpdateScmResult( SvnJavaScmProvider.COMMAND_LINE, handler.getFiles() );
        }
        catch ( SVNException e )
        {
            return new UpdateScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN update failed.", e.getMessage(), false );
        }
        finally
        {
            javaRepo.getClientManager().getUpdateClient().setEventHandler( null );
        }
    }

    private static SVNRevision getSVNRevision( String revision )
    {
        if ( revision == null || !SvnTagBranchUtils.isRevisionSpecifier( revision ) )
        {
            return SVNRevision.HEAD;
        }
        else
        {
            return SVNRevision.parse( revision );
        }
    }

    /**
     * @see org.apache.maven.scm.command.update.AbstractUpdateCommand#getChangeLogCommand()
     */
    protected ChangeLogCommand getChangeLogCommand()
    {
        SvnChangeLogCommand command = new SvnChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }


}
