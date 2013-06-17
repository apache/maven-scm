package org.apache.maven.scm.provider.git.jgit.command.diff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class JGitDiffCommand extends AbstractDiffCommand implements GitCommand {

	@Override
	protected DiffScmResult executeDiffCommand(ScmProviderRepository repository, ScmFileSet fileSet, ScmVersion startRevision, ScmVersion endRevision) throws ScmException {

		try {
			Git git = Git.open(fileSet.getBasedir());

			OutputStream out = new ByteArrayOutputStream();
			DiffCommand diff = git.diff().setOutputStream(out).setOldTree(getTreeIterator(git.getRepository(), startRevision.getName())).setNewTree(getTreeIterator(git.getRepository(), endRevision.getName()));
			List<DiffEntry> entries = diff.call();
			List<ScmFile> changedFiles = new ArrayList<ScmFile>();
			
			// TODO get differences 
			Map<String, CharSequence> differences = new HashMap<String, CharSequence>();

			for (DiffEntry diffEntry : entries) {
				changedFiles.add(new ScmFile(diffEntry.getNewPath(), getFileStatusForModificationType(diffEntry.getChangeType())));
			}

			return new DiffScmResult(changedFiles, differences, out.toString(), new ScmResult("JGit diff", "diff", null, true));
		} catch (Exception e) {
			throw new ScmException("JGit diff failure!", e);
		}
	}

	private ScmFileStatus getFileStatusForModificationType(ChangeType changeType) {
		switch (changeType) {
		case ADD:
			return ScmFileStatus.ADDED;
		case MODIFY:
			return ScmFileStatus.MODIFIED;
		case DELETE:
			return ScmFileStatus.DELETED;
		case RENAME:
			return ScmFileStatus.RENAMED;
		case COPY:
			return ScmFileStatus.COPIED;
		default:
			return ScmFileStatus.UNKNOWN;
		}
	}

	private AbstractTreeIterator getTreeIterator(Repository repo, String name) throws IOException {
		final ObjectId id = repo.resolve(name);
		if (id == null)
			throw new IllegalArgumentException(name);
		final CanonicalTreeParser p = new CanonicalTreeParser();
		final ObjectReader or = repo.newObjectReader();
		try {
			p.reset(or, new RevWalk(repo).parseTree(id));
			return p;
		} finally {
			or.release();
		}
	}
}
