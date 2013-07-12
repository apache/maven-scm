package org.apache.maven.scm.provider.git.jgit.command.add;

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

import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.eclipse.jgit.api.Git;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @version $Id: JGitAddCommand.java 894145 2009-12-28 10:13:39Z struberg $
 */
public class JGitAddCommand
    extends AbstractAddCommand
    implements GitCommand
{
    /**
     * {@inheritDoc}
     */
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {

        if ( fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "You must provide at least one file/directory to add (e.g. -Dincludes=...)" );
        }
        try
        {
            Git git = Git.open( fileSet.getBasedir() );

            List<ScmFile> addedFiles = JGitUtils.addAllFiles( git, fileSet );

            if ( getLogger().isDebugEnabled() )
            {
                for ( ScmFile scmFile : addedFiles )
                {
                    getLogger().info( "added file: " + scmFile );
                }
            }

            return new AddScmResult( "JGit add", addedFiles );

        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit add failure!", e );
        }

    }

}
