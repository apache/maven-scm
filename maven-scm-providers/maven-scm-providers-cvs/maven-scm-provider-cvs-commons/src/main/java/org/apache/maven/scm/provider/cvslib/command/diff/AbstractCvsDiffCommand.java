package org.apache.maven.scm.provider.cvslib.command.diff;

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
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public abstract class AbstractCvsDiffCommand
    extends AbstractDiffCommand
    implements CvsCommand
{
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, String startRevision,
                                                String endRevision )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "diff", repository, fileSet );

        cl.createArgument().setValue( "-u" );

        cl.createArgument().setValue( "-N" );

        if ( startRevision != null )
        {
            cl.createArgument().setValue( "-r" );
            cl.createArgument().setValue( startRevision );
        }

        if ( endRevision != null )
        {
            cl.createArgument().setValue( "-r" );
            cl.createArgument().setValue( endRevision );
        }

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        return executeCvsCommand( cl );
    }

    protected abstract DiffScmResult executeCvsCommand( Commandline cl )
        throws ScmException;
}
