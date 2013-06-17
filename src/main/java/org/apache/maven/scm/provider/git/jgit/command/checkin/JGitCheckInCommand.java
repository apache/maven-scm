package org.apache.maven.scm.provider.git.jgit.command.checkin;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id: JGitCheckInCommand.java 894145 2009-12-28 10:13:39Z struberg $
 */
public class JGitCheckInCommand extends AbstractCheckInCommand implements GitCommand {
	/** {@inheritDoc} */
	protected CheckInScmResult executeCheckInCommand(ScmProviderRepository repo, ScmFileSet fileSet, String message, ScmVersion version) throws ScmException {

		try {
			Git git = Git.open(fileSet.getBasedir());

			if (!fileSet.getFileList().isEmpty()) {
				AddCommand add = git.add();
				for (File file : fileSet.getFileList()) {
					add.addFilepattern(file.getPath());
				}
				add.call();
			} else {
				// add all tracked files which are modified manually
				Set<String> changeds = git.status().call().getModified();
				if (changeds.isEmpty()) {
					// warn there is nothing to add
				} else {
					AddCommand add = git.add();
					for (String changed : changeds) {
						add.addFilepattern(changed);
					}
					add.call();
				}
			}

			git.commit().setMessage(message).call();

			if (repo.isPushChanges()) {
				String branch = version != null ? version.getName() : null;
				if (StringUtils.isBlank(branch)) {
					branch = git.getRepository().getBranch();
				}
				RefSpec refSpec = new RefSpec("refs/heads/" + branch + ":" + "refs/heads/" + branch);
				getLogger().info("push changes to remote... " + refSpec.toString());
				JGitUtils.push(getLogger(), git, (GitScmProviderRepository) repo, refSpec);
			}

			List<ScmFile> checkedInFiles = new ArrayList<ScmFile>();
			// TODO get a list of the commited files

			return new CheckInScmResult("JGit checkin", checkedInFiles);
		} catch (Exception e) {
			throw new ScmException("JGit checkin failure!", e);
		}
	}

}
