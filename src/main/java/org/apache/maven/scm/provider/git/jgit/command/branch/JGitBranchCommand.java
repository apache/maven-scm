package org.apache.maven.scm.provider.git.jgit.command.branch;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;

/**
 * 
 * @author Dominik Bartholdi (imod)
 */
public class JGitBranchCommand extends AbstractBranchCommand implements GitCommand {

	/** {@inheritDoc} */
	@Override
	protected ScmResult executeBranchCommand(ScmProviderRepository repo, ScmFileSet fileSet, String branch, String message) throws ScmException {
		if (branch == null || StringUtils.isEmpty(branch.trim())) {
			throw new ScmException("branch name must be specified");
		}

		if (!fileSet.getFileList().isEmpty()) {
			throw new ScmException("This provider doesn't support branching subsets of a directory");
		}

		try {
			Git git = Git.open(fileSet.getBasedir());
			git.branchCreate().setName(branch).call();

			if (repo.isPushChanges()) {
				getLogger().info("push branch [" + branch + "] to remote...");
				JGitUtils.push(getLogger(), git, (GitScmProviderRepository) repo, new RefSpec("refs/heads/" + branch));
			}

			List<ScmFile> taggedFiles = new ArrayList<ScmFile>();
			// TODO list all branched files

			return new BranchScmResult("JGit branch", taggedFiles);

		} catch (Exception e) {
			throw new ScmException("JGit branch failed!", e);
		}
	}

}
