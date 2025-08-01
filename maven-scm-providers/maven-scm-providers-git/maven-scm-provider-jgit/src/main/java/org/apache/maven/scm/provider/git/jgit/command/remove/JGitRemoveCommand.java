/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.provider.git.jgit.command.remove;

import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.eclipse.jgit.api.Git;

/**
 * @author Georg Tsakumagos
 * @since 2.0.0-M2
 */
public class JGitRemoveCommand extends AbstractRemoveCommand implements GitCommand<RemoveScmResult> {

    @Override
    protected RemoveScmResult executeRemoveCommand(ScmProviderRepository repository, ScmFileSet fileSet, String message)
            throws ScmException {

        if (fileSet.getFileList().isEmpty()) {
            throw new ScmException("You must provide at least one file/directory to remove");
        }
        Git git = null;
        try {
            git = JGitUtils.openRepo(fileSet.getBasedir());

            List<ScmFile> removedFiles = JGitUtils.removeAllFiles(git, fileSet);

            if (logger.isDebugEnabled()) {
                for (ScmFile scmFile : removedFiles) {
                    logger.info("Removed file: " + scmFile);
                }
            }

            return new RemoveScmResult("JGit remove", removedFiles);

        } catch (Exception e) {
            throw new ScmException("JGit remove failure!", e);
        } finally {
            JGitUtils.closeRepo(git);
        }
    }
}
