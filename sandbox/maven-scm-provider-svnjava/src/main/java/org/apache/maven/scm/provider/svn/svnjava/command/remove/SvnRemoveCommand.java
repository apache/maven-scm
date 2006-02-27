package org.apache.maven.scm.provider.svn.svnjava.command.remove;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.ScmFileEventHandler;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.tmatesoft.svn.core.SVNException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnRemoveCommand
    extends AbstractRemoveCommand
    implements SvnCommand
{
    protected ScmResult executeRemoveCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message )
        throws ScmException
    {
        if ( fileSet.getFiles().length == 0 )
        {
            throw new ScmException( "You must provide at least one file/directory to remove" );
        }

        getLogger().info( "SVN remove working directory: " + fileSet.getBasedir().getAbsolutePath() );

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        ScmFileEventHandler handler = new ScmFileEventHandler( getLogger(), fileSet.getBasedir() );

        javaRepo.getClientManager().getWCClient().setEventHandler( handler );

        try
        {
            SvnJavaUtil.delete( javaRepo.getClientManager(), fileSet.getFiles(), true );

            return new RemoveScmResult( SvnJavaScmProvider.COMMAND_LINE, handler.getFiles() );
        }
        catch ( SVNException e )
        {
            return new RemoveScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN remove failed.", e.getMessage(), false );
        }
        finally
        {
            javaRepo.getClientManager().getWCClient().setEventHandler( null );
        }
    }
}
