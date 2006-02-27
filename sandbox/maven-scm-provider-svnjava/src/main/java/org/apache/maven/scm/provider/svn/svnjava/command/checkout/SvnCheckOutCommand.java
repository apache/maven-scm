package org.apache.maven.scm.provider.svn.svnjava.command.checkout;

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
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
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
public class SvnCheckOutCommand
    extends AbstractCheckOutCommand
    implements SvnCommand
{
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        getLogger().info( "SVN checkout directory: " + fileSet.getBasedir().getAbsolutePath() );

        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        String url = repository.getUrl();
        if ( tag != null )
        {
            url = SvnTagBranchUtils.resolveTagUrl( repository, tag );
        }

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        ScmFileEventHandler handler = new ScmFileEventHandler( getLogger(), fileSet.getBasedir() );

        javaRepo.getClientManager().getUpdateClient().setEventHandler( handler );

        try
        {
            SvnJavaUtil.checkout( javaRepo.getClientManager(), SVNURL.parseURIEncoded( url ), SVNRevision.HEAD, fileSet
                .getBasedir(), true );

            return new CheckOutScmResult( SvnJavaScmProvider.COMMAND_LINE, handler.getFiles() );
        }
        catch ( SVNException e )
        {
            return new CheckOutScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN checkout failed.", e.getMessage(),
                                          false );
        }
        finally
        {
            javaRepo.getClientManager().getUpdateClient().setEventHandler( null );
        }
    }


}
