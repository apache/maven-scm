package org.apache.maven.scm.provider.git.jgit.command.branch;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;

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
			Ref branchRef = git.branchCreate().setName(branch).call();

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
