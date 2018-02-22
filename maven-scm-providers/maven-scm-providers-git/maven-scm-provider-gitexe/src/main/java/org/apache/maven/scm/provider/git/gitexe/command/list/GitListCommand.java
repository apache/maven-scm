package org.apache.maven.scm.provider.git.gitexe.command.list;

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

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public class GitListCommand
    extends AbstractListCommand
    implements GitCommand
{
    /** {@inheritDoc} */
    protected ListScmResult executeListCommand( ScmProviderRepository repo, ScmFileSet fileSet, boolean recursive,
                                                ScmVersion scmVersion )
        throws ScmException
    {
        return new ListScmResult( null, new ScmResult( null, "This provider does not support list", null, false ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "ls-files" );

        return cl;
    }

}
