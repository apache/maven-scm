package org.apache.maven.scm.command.diff;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * TODO: Better support for entire filesets.
 * TODO: Better support for entire changesets (ie consistency between revision handling in CVS and SVN).
 * TODO: Consistent handling of revisions, tags and dates - currently only revisions supported, though tags will work for CVS
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public abstract class AbstractDiffCommand
    extends AbstractCommand
{
    protected abstract DiffScmResult executeDiffCommand( ScmProviderRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException;

    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        String startRevision = parameters.getString( CommandParameter.START_REVISION, null );

        String endRevision = parameters.getString( CommandParameter.END_REVISION, null );

        return executeDiffCommand( repository, fileSet, startRevision, endRevision );
    }
}
