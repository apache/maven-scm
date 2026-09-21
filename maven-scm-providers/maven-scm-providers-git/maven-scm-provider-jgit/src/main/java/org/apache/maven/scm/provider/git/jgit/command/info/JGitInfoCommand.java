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
package org.apache.maven.scm.provider.git.jgit.command.info;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.CommandParameter;
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * @since 1.9.5
 */
public class JGitInfoCommand extends AbstractCommand implements GitCommand {
    @Override
    protected ScmResult executeCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
        File basedir = fileSet.getBasedir();
        boolean skipMergeCommits = isSkipMergeCommits(parameters);
        Git git = null;
        try {
            git = JGitUtils.openRepo(basedir);
            ObjectId objectId = git.getRepository().resolve(Constants.HEAD);
            if (objectId == null) {
                throw new ScmException("Cannot resolve HEAD in git repository at " + basedir);
            }

            List<InfoItem> infoItems = new LinkedList<>();
            if (fileSet.getFileList().isEmpty()) {
                RevCommit commit = getMostRecentCommit(git.getRepository(), objectId, skipMergeCommits);
                infoItems.add(getInfoItem(commit, fileSet.getBasedir()));
            } else {
                // iterate over all files
                for (File file : JGitUtils.getWorkingCopyRelativePaths(
                        git.getRepository().getWorkTree(), fileSet)) {
                    infoItems.add(getInfoItem(git.getRepository(), objectId, file, skipMergeCommits));
                }
            }
            return new InfoScmResult(infoItems, new ScmResult("JGit.resolve(HEAD)", "", objectId.toString(), true));
        } catch (IOException e) {
            throw new ScmException("JGit resolve failure!", e);
        } finally {
            JGitUtils.closeRepo(git);
        }
    }

    protected InfoItem getInfoItem(Repository repository, ObjectId headObjectId, File file, boolean skipMergeCommits)
            throws IOException {
        RevCommit commit = getMostRecentCommitForPath(
                repository, headObjectId, JGitUtils.toNormalizedFilePath(file), skipMergeCommits);
        return getInfoItem(commit, file);
    }

    /**
     * Returns the most recent commit reachable from {@code headObjectId}, optionally ignoring merge commits
     * (mimics {@code git log -1 --no-merges} when {@code skipMergeCommits} is {@code true}).
     * May return {@code null} when no non-merge commit is reachable (e.g. shallow history of merge commits only).
     */
    private RevCommit getMostRecentCommit(Repository repository, ObjectId headObjectId, boolean skipMergeCommits)
            throws IOException {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit headCommit = revWalk.parseCommit(headObjectId);
            if (!skipMergeCommits) {
                return headCommit;
            }
            revWalk.markStart(headCommit);
            revWalk.sort(RevSort.COMMIT_TIME_DESC);
            revWalk.setRevFilter(RevFilter.NO_MERGES);
            return revWalk.next();
        }
    }

    protected InfoItem getInfoItem(RevCommit fileCommit, File file) {
        InfoItem infoItem = new InfoItem();
        infoItem.setPath(file.getPath());
        infoItem.setURL(file.toPath().toUri().toASCIIString());
        if (fileCommit == null) {
            // no matching commit (e.g. path without history, or only merge commits while skipping them):
            // like gitexe on empty "git log" output, report the item without revision metadata
            return infoItem;
        }
        infoItem.setRevision(StringUtils.trim(fileCommit.name()));
        PersonIdent authorIdent = fileCommit.getAuthorIdent();
        infoItem.setLastChangedDateTime(authorIdent
                .getWhen()
                .toInstant()
                .atZone(authorIdent.getTimeZone().toZoneId()));
        infoItem.setLastChangedAuthor(authorIdent.getName() + " <" + authorIdent.getEmailAddress() + ">");
        return infoItem;
    }

    private RevCommit getMostRecentCommitForPath(
            Repository repository, ObjectId headObjectId, String path, boolean skipMergeCommits) throws IOException {
        RevCommit latestCommit = null;
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit headCommit = revWalk.parseCommit(headObjectId);
            revWalk.markStart(headCommit);
            revWalk.sort(RevSort.COMMIT_TIME_DESC);
            if (skipMergeCommits) {
                revWalk.setRevFilter(RevFilter.NO_MERGES);
            }
            revWalk.setTreeFilter(AndTreeFilter.create(PathFilter.create(path), TreeFilter.ANY_DIFF));
            latestCommit = revWalk.next();
        }
        return latestCommit;
    }

    /**
     * Whether merge commits should be skipped for the {@code info} command.
     *
     * @param parameters the command parameters (may be {@code null})
     * @return {@code true} if parameter {@link CommandParameter#SCM_SKIP_MERGE_COMMITS} (or the whole
     *         {@code parameters}) is absent, and otherwise the requested value
     * @throws ScmException if the parameter has the wrong type
     */
    private static boolean isSkipMergeCommits(CommandParameters parameters) throws ScmException {
        if (parameters == null) {
            return true;
        }
        return parameters.getBoolean(CommandParameter.SCM_SKIP_MERGE_COMMITS, true);
    }
}
