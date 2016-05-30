package org.apache.maven.scm.provider.git.jgit.command.blame;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.blame.AbstractBlameCommand;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitBlameCommand
    extends AbstractBlameCommand
    implements GitCommand
{

    @Override
    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet workingDirectory,
                                               String filename )
        throws ScmException
    {

        Git git = null;
        File basedir = workingDirectory.getBasedir();
        try
        {
            git = JGitUtils.openRepo( basedir );
            BlameResult blameResult = git.blame().setFilePath( filename ).call();

            List<BlameLine> lines = new ArrayList<BlameLine>();

            int i = 0;
            while ( ( i = blameResult.computeNext() ) != -1 )
            {
                lines.add( new BlameLine( blameResult.getSourceAuthor( i ).getWhen(),
                                          blameResult.getSourceCommit( i ).getName(),
                                          blameResult.getSourceAuthor( i ).getName(),
                                          blameResult.getSourceCommitter( i ).getName() ) );
            }

            return new BlameScmResult( "JGit blame", lines );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit blame failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

}
