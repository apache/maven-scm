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
package org.apache.maven.scm.provider.git.jgit.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.util.FilenameUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.jgit.lib.Constants.R_TAGS;

/**
 * JGit utility functions.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JGitUtils.class);

    private JGitUtils() {
        // no op
    }

    /**
     * Opens a JGit repository in the current directory or a parent directory.
     * @param basedir The directory to start with
     * @throws IOException If the repository cannot be opened
     */
    public static Git openRepo(File basedir) throws IOException {
        return new Git(new RepositoryBuilder()
                .readEnvironment()
                .findGitDir(basedir)
                .setMustExist(true)
                .build());
    }

    /**
     * Closes the repository wrapped by the passed git object
     * @param git
     */
    public static void closeRepo(Git git) {
        if (git != null && git.getRepository() != null) {
            git.getRepository().close();
        }
    }

    /**
     * Construct a logging ProgressMonitor for all JGit operations.
     *
     * @return a ProgressMonitor for use
     */
    public static ProgressMonitor getMonitor() {
        // X TODO write an own ProgressMonitor which logs to ScmLogger!
        return new TextProgressMonitor();
    }

    /**
     * Prepares the in memory configuration of git to connect to the configured
     * repository. It configures the following settings in memory: <br>
     * <ul><li>push url</li> <li>fetch url</li></ul>
     * <p>
     *
     * @param git        the instance to configure (only in memory, not saved)
     * @param repository the repo config to be used
     * @return {@link CredentialsProvider} in case there are credentials
     * informations configured in the repository.
     */
    public static CredentialsProvider prepareSession(Git git, GitScmProviderRepository repository) {
        StoredConfig config = git.getRepository().getConfig();
        config.setString("remote", "origin", "url", repository.getFetchUrl());
        config.setString("remote", "origin", "pushURL", repository.getPushUrl());

        // make sure we do not log any passwords to the output
        LOGGER.info("fetch url: " + repository.getFetchUrlWithMaskedPassword());
        LOGGER.info("push url: " + repository.getPushUrlWithMaskedPassword());
        return getCredentials(repository);
    }

    /**
     * Creates a credentials provider from the information passed in the
     * repository. Current implementation supports: <br>
     * <ul><li>UserName/Password</li></ul>
     * <p>
     *
     * @param repository the config to get the details from
     * @return <code>null</code> if there is not enough info to create a
     *         provider with
     */
    public static CredentialsProvider getCredentials(GitScmProviderRepository repository) {
        if (StringUtils.isNotBlank(repository.getUser()) && StringUtils.isNotBlank(repository.getPassword())) {
            return new UsernamePasswordCredentialsProvider(
                    repository.getUser().trim(), repository.getPassword().trim());
        }

        return null;
    }

    public static Iterable<PushResult> push(
            Git git,
            GitScmProviderRepository repo,
            RefSpec refSpec,
            Set<RemoteRefUpdate.Status> successfulStatuses,
            Optional<TransportConfigCallback> transportConfigCallback)
            throws PushException {
        CredentialsProvider credentials = prepareSession(git, repo);
        PushCommand command = git.push().setRefSpecs(refSpec).setCredentialsProvider(credentials);
        transportConfigCallback.ifPresent(command::setTransportConfigCallback);

        Iterable<PushResult> pushResultList;
        try {
            pushResultList = command.call();
        } catch (GitAPIException e) {
            throw new PushException(repo.getPushUrlWithMaskedPassword(), e);
        }
        for (PushResult pushResult : pushResultList) {
            Collection<RemoteRefUpdate> ru = pushResult.getRemoteUpdates();
            for (RemoteRefUpdate remoteRefUpdate : ru) {
                if (!successfulStatuses.contains(remoteRefUpdate.getStatus())) {
                    throw new PushException(repo.getPushUrlWithMaskedPassword(), remoteRefUpdate);
                }
                LOGGER.debug("Push succeeded {}", remoteRefUpdate);
            }
        }
        return pushResultList;
    }

    /**
     * Does the Repository have any commits?
     *
     * @param repo
     * @return false if there are no commits
     */
    public static boolean hasCommits(Repository repo) {
        if (repo != null && repo.getDirectory().exists()) {
            return (new File(repo.getDirectory(), "objects").list().length > 2)
                    || (new File(repo.getDirectory(), "objects/pack").list().length > 0);
        }
        return false;
    }

    /**
     * get a list of all files in the given commit
     *
     * @param repository the repo
     * @param commit     the commit to get the files from
     * @return a list of files included in the commit
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     * @throws CorruptObjectException
     * @throws IOException
     */
    public static List<ScmFile> getFilesInCommit(Repository repository, RevCommit commit)
            throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
        return getFilesInCommit(repository, commit, null);
    }

    /**
     * get a list of all files in the given commit
     *
     * @param repository the repo
     * @param commit     the commit to get the files from
     * @param baseDir    the directory to which the returned files should be relative.
     *                   May be {@code null} in case they should be relative to the working directory root.
     * @return a list of files included in the commit
     *
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     * @throws CorruptObjectException
     * @throws IOException
     */
    public static List<ScmFile> getFilesInCommit(Repository repository, RevCommit commit, File baseDir)
            throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
        List<ScmFile> list = new ArrayList<>();
        if (JGitUtils.hasCommits(repository)) {

            try (RevWalk rw = new RevWalk(repository);
                    DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                RevCommit realParent = commit.getParentCount() > 0 ? commit.getParent(0) : commit;
                RevCommit parent = rw.parseCommit(realParent.getId());
                df.setRepository(repository);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
                for (DiffEntry diff : diffs) {
                    final String path;
                    if (baseDir != null) {
                        path = relativize(baseDir, new File(repository.getWorkTree(), diff.getNewPath()))
                                .getPath();
                    } else {
                        path = diff.getNewPath();
                    }
                    list.add(new ScmFile(path, ScmFileStatus.CHECKED_IN));
                }
            }
        }
        return list;
    }

    /**
     * Translate a {@code FileStatus} in the matching {@code ScmFileStatus}.
     *
     * @param changeType
     * @return the matching ScmFileStatus
     */
    public static ScmFileStatus getScmFileStatus(ChangeType changeType) {
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

    /**
     * Adds all files in the given fileSet to the repository.
     *
     * @param git     the repo to add the files to
     * @param fileSet the set of files within the workspace, the files are added
     *                relative to the basedir of this fileset
     * @return a list of added files
     * @throws GitAPIException
     */
    public static List<ScmFile> addAllFiles(Git git, ScmFileSet fileSet) throws GitAPIException {
        File workingCopyRootDirectory = git.getRepository().getWorkTree();
        AddCommand add = git.add();
        getWorkingCopyRelativePaths(workingCopyRootDirectory, fileSet).stream()
                .forEach(f -> add.addFilepattern(toNormalizedFilePath(f)));
        add.call();

        Status status = git.status().call();

        Set<String> allInIndex = new HashSet<>();
        allInIndex.addAll(status.getAdded());
        allInIndex.addAll(status.getChanged());
        return getScmFilesForAllFileSetFilesContainedInRepoPath(
                workingCopyRootDirectory, fileSet, allInIndex, ScmFileStatus.ADDED);
    }

    /**
     * Remove all files in the given fileSet from the repository.
     *
     * @param git     the repo to remove the files from
     * @param fileSet the set of files within the workspace, the files are removed
     *                relative to the basedir of this fileset
     * @return a list of removed files
     * @throws GitAPIException
     */
    public static List<ScmFile> removeAllFiles(Git git, ScmFileSet fileSet) throws GitAPIException {
        File workingCopyRootDirectory = git.getRepository().getWorkTree();
        RmCommand remove = git.rm();
        getWorkingCopyRelativePaths(workingCopyRootDirectory, fileSet).stream()
                .forEach(f -> remove.addFilepattern(toNormalizedFilePath(f)));
        remove.call();

        Status status = git.status().call();

        Set<String> allInIndex = new HashSet<>(status.getRemoved());
        return getScmFilesForAllFileSetFilesContainedInRepoPath(
                workingCopyRootDirectory, fileSet, allInIndex, ScmFileStatus.DELETED);
    }

    /**
     * Convert each file in the {@code fileSet} to their relative file path to workingCopyDirectory
     * and return them in a list.
     * @param workingCopyDirectory the working copy root directory
     * @param fileSet the file set to convert
     */
    public static List<File> getWorkingCopyRelativePaths(File workingCopyDirectory, ScmFileSet fileSet) {
        List<File> repositoryRelativePaths = new ArrayList<>();
        for (File path : fileSet.getFileList()) {
            if (!path.isAbsolute()) {
                path = new File(fileSet.getBasedir().getPath(), path.getPath());
            }
            File repositoryRelativePath = relativize(workingCopyDirectory, path);
            repositoryRelativePaths.add(repositoryRelativePath);
        }
        return repositoryRelativePaths;
    }

    /**
     * Converts the given file to a string only containing forward slashes
     * @param file
     * @return the normalized file path
     */
    public static String toNormalizedFilePath(File file) {
        return FilenameUtils.normalizeFilename(file);
    }

    private static List<ScmFile> getScmFilesForAllFileSetFilesContainedInRepoPath(
            File workingCopyDirectory, ScmFileSet fileSet, Set<String> repoFilePaths, ScmFileStatus fileStatus) {
        List<ScmFile> files = new ArrayList<>(repoFilePaths.size());
        getWorkingCopyRelativePaths(workingCopyDirectory, fileSet).stream().forEach((relativeFile) -> {
            // check if repo relative path is contained
            if (repoFilePaths.contains(toNormalizedFilePath(relativeFile))) {
                // returned ScmFiles should be relative to given fileset's basedir
                ScmFile scmfile = new ScmFile(
                        relativize(fileSet.getBasedir(), new File(workingCopyDirectory, relativeFile.getPath()))
                                .getPath(),
                        fileStatus);
                files.add(scmfile);
            }
        });
        return files;
    }

    private static File relativize(File baseDir, File file) {
        if (file.isAbsolute()) {
            return baseDir.toPath().relativize(file.toPath()).toFile();
        }
        return file;
    }

    /**
     * Get a list of commits between two revisions.
     *
     * @param repo     the repository to work on
     * @param sortings sorting
     * @param fromRev  start revision
     * @param toRev    if null, falls back to head
     * @param fromDate from which date on
     * @param toDate   until which date
     * @param maxLines max number of lines
     * @return a list of commits, might be empty, but never <code>null</code>
     * @throws IOException
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     */
    public static List<RevCommit> getRevCommits(
            Repository repo,
            RevSort[] sortings,
            String fromRev,
            String toRev,
            final Date fromDate,
            final Date toDate,
            int maxLines)
            throws IOException, MissingObjectException, IncorrectObjectTypeException {

        List<RevCommit> revs = new ArrayList<>();

        ObjectId fromRevId = fromRev != null ? repo.resolve(fromRev) : null;
        ObjectId toRevId = toRev != null ? repo.resolve(toRev) : null;

        if (sortings == null || sortings.length == 0) {
            sortings = new RevSort[] {RevSort.TOPO, RevSort.COMMIT_TIME_DESC};
        }

        try (RevWalk walk = new RevWalk(repo)) {
            for (final RevSort s : sortings) {
                walk.sort(s, true);
            }

            if (fromDate != null && toDate != null) {
                // walk.setRevFilter( CommitTimeRevFilter.between( fromDate, toDate ) );
                walk.setRevFilter(new RevFilter() {
                    @Override
                    public boolean include(RevWalk walker, RevCommit cmit)
                            throws StopWalkException, MissingObjectException, IncorrectObjectTypeException,
                                    IOException {
                        int cmtTime = cmit.getCommitTime();

                        return (cmtTime >= (fromDate.getTime() / 1000)) && (cmtTime <= (toDate.getTime() / 1000));
                    }

                    @Override
                    public RevFilter clone() {
                        return this;
                    }
                });
            } else {
                if (fromDate != null) {
                    walk.setRevFilter(CommitTimeRevFilter.after(fromDate));
                }
                if (toDate != null) {
                    walk.setRevFilter(CommitTimeRevFilter.before(toDate));
                }
            }

            if (fromRevId != null) {
                RevCommit c = walk.parseCommit(fromRevId);
                c.add(RevFlag.UNINTERESTING);
                RevCommit real = walk.parseCommit(c);
                walk.markUninteresting(real);
            }

            if (toRevId != null) {
                RevCommit c = walk.parseCommit(toRevId);
                c.remove(RevFlag.UNINTERESTING);
                RevCommit real = walk.parseCommit(c);
                walk.markStart(real);
            } else {
                final ObjectId head = repo.resolve(Constants.HEAD);
                if (head == null) {
                    throw new RuntimeException("Cannot resolve " + Constants.HEAD);
                }
                RevCommit real = walk.parseCommit(head);
                walk.markStart(real);
            }

            int n = 0;
            for (final RevCommit c : walk) {
                n++;
                if (maxLines != -1 && n > maxLines) {
                    break;
                }

                revs.add(c);
            }
            return revs;
        }
    }

    /**
     * Get a list of tags that has been set in the specified commit.
     *
     * @param repo the repository to work on
     * @param commit the commit for which we want the tags
     * @return a list of tags, might be empty, and never <code>null</code>
     */
    public static List<String> getTags(Repository repo, RevCommit commit) throws IOException {
        List<Ref> refList = repo.getRefDatabase().getRefsByPrefix(R_TAGS);

        try (RevWalk revWalk = new RevWalk(repo)) {
            ObjectId commitId = commit.getId();
            List<String> result = new ArrayList<>();

            for (Ref ref : refList) {
                ObjectId tagId = ref.getObjectId();
                RevCommit tagCommit = revWalk.parseCommit(tagId);
                if (commitId.equals(tagCommit.getId())) {
                    result.add(ref.getName().substring(R_TAGS.length()));
                }
            }
            return result;
        }
    }
}
