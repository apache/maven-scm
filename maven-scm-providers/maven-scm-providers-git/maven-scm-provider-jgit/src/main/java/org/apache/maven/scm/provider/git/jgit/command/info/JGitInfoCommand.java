package org.apache.maven.scm.provider.git.jgit.command.info;

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
import java.util.Collections;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;

/**
 * @since 1.9.5
 */
public class JGitInfoCommand
    extends AbstractCommand
    implements GitCommand
{
    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        Git git = null;
        try
        {
            File basedir = fileSet.getBasedir();

            git = Git.open( basedir );

            ObjectId objectId = git.getRepository().resolve( "HEAD" );

            InfoItem infoItem = new InfoItem();
            infoItem.setRevision( StringUtils.trim( objectId.name() ) );
            infoItem.setURL( basedir.getPath() );

            return new InfoScmResult( Collections.singletonList( infoItem ),
                                      new ScmResult( "JGit.resolve(HEAD)", "", objectId.toString(), true ) );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit resolve failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }
}
