package org.apache.maven.scm.provider.git.jgit.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
//import org.eclipse.jgit.simple.SimpleRepository;
//import org.eclipse.jgit.simple.StatusEntry;
//import org.eclipse.jgit.simple.StatusEntry.IndexStatus;
//import org.eclipse.jgit.simple.StatusEntry.RepoStatus;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

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

/**
 * JGit SimpleRepository utility functions.
 * 
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id: JGitUtils.java 894145 2009-12-28 10:13:39Z struberg $
 */
public class JGitUtils {

	private JGitUtils() {
	}

	/**
	 * Construct a logging ProgressMonitor for all JGit operations.
	 * 
	 * @param logger
	 * @return a ProgressMonitor for use
	 */
	public static ProgressMonitor getMonitor(ScmLogger logger) {
		// X TODO write an own ProgressMonitor which logs to ScmLogger!
		return new TextProgressMonitor();
	}

	public static CredentialsProvider prepareSession(ScmLogger logger, Git git, GitScmProviderRepository repository) {
		StoredConfig config = git.getRepository().getConfig();
		config.setString("remote", "origin", "url", repository.getFetchUrl());
		config.setString("remote", "origin", "pushURL", repository.getPushUrl());
		logger.info("fetch url: " + repository.getFetchUrl());
		logger.info("push url: " + repository.getPushUrl());
		return getCredentials(repository);
	}

	public static CredentialsProvider getCredentials(GitScmProviderRepository repository) {
		if (StringUtils.isNotBlank(repository.getUser())) {
			return new UsernamePasswordCredentialsProvider(repository.getUser(), repository.getPassword());
		}
		return null;
	}

	public static Iterable<PushResult> push(ScmLogger logger, Git git, GitScmProviderRepository repo, RefSpec refSpec) throws GitAPIException, InvalidRemoteException, TransportException {
		CredentialsProvider credentials = JGitUtils.prepareSession(logger, git, repo);
		Iterable<PushResult> pushResultList = git.push().setCredentialsProvider(credentials).setRefSpecs(refSpec).call();
		for (PushResult pushResult : pushResultList) {
			Collection<RemoteRefUpdate> ru = pushResult.getRemoteUpdates();
			for (RemoteRefUpdate remoteRefUpdate : ru) {
				logger.info(remoteRefUpdate.getStatus() + " - " + remoteRefUpdate.toString());
			}
		}
		return pushResultList;
	}

	//
	// /**
	// * Translate a {@code FileStatus} in the matching {@code ScmFileStatus}.
	// *
	// * @param status
	// * @return the matching ScmFileStatus
	// * @throws ScmException if the given Status cannot be translated
	// */
	// public static ScmFileStatus getScmFileStatus( StatusEntry status )
	// throws ScmException
	// {
	// IndexStatus is = status.getIndexStatus();
	// RepoStatus rs = status.getRepoStatus();
	//
	// if ( is.equals( IndexStatus.ADDED ) )
	// {
	// return ScmFileStatus.ADDED;
	// }
	// else if ( is.equals( IndexStatus.UNCHANGED ) && rs.equals(
	// RepoStatus.UNCHANGED ) )
	// {
	// return ScmFileStatus.CHECKED_IN;
	// }
	// else if ( is.equals( IndexStatus.MODIFIED ) )
	// {
	// return ScmFileStatus.MODIFIED;
	// }
	// else if ( is.equals( IndexStatus.DELETED ) && rs.equals(
	// RepoStatus.REMOVED ) )
	// {
	// return ScmFileStatus.DELETED;
	// }
	// else {
	// return ScmFileStatus.UNKNOWN;
	// }
	//
	// /*X
	// switch (status) {
	// case UNMERGED:
	// return ScmFileStatus.CONFLICT;
	// case OTHER:
	// return ScmFileStatus.ADDED;
	// default:
	//
	// }
	// */
	// }
	//
	// /**
	// * Get the branch name from the ScmVersion
	// * @param scmVersion
	// * @return branch name if the ScmVersion indicates a branch, with taking
	// <code>&quot;master&quot;</code>
	// * as default branch name. For tags <code>null</code> will be returned.
	// */
	// public static String getBranchName( ScmVersion scmVersion )
	// {
	// String branchName = "master";
	//
	// // we explicitly request branches, since tags will be handled differently
	// in git
	// if (scmVersion instanceof ScmTag) {
	// return null;
	// }
	//
	// if (scmVersion instanceof ScmBranch)
	// {
	// branchName = scmVersion.getName();
	// }
	//
	// return branchName;
	// }
	//
	// /**
	// * get the tag name from the ScmVersion
	// * @param scmVersion
	// * @return tag name if the ScmVersion indicates a tag, <code>null</code>
	// otherwise
	// */
	// public static String getTagName( ScmVersion scmVersion )
	// {
	// // we explicitly request branches, since tags will be handled differently
	// in git
	// if (scmVersion instanceof ScmTag) {
	// return scmVersion.getName();
	// }
	//
	// return null;
	// }
	//
	// /**
	// * Add all files of the given fileSet to the SimpleRepository.
	// * This will make all relative paths be under the repositories base
	// directory.
	// *
	// * @param srep
	// * @param fileSet
	// * @throws Exception
	// */
	// public static void addAllFiles( SimpleRepository srep, ScmFileSet fileSet
	// )
	// throws Exception
	// {
	// @SuppressWarnings("unchecked")
	// List<File> addFiles = fileSet.getFileList();
	// if ( addFiles != null )
	// {
	// for ( File addFile : addFiles )
	// {
	// if ( !addFile.isAbsolute() )
	// {
	// addFile = new File( fileSet.getBasedir(), addFile.getPath() );
	// }
	//
	// srep.add( addFile, false );
	// }
	//
	// }
	// }
	//
	//
	// /**
	// * Convert from List<StatusEntry> to List<ScmFile>
	// * @param statusEntries
	// * @param addUnknown if <code>false</code>, this function will not add
	// files with 'unknown' status to the returned list
	// * @return list with ScmFiles ready for use in ScmResult and other
	// maven-scm APIs
	// * @throws ScmException
	// */
	// public static List<ScmFile> getChangedFiles( List<StatusEntry>
	// statusEntries, boolean addUnknown )
	// throws ScmException
	// {
	// if ( statusEntries == null )
	// {
	// return null;
	// }
	//
	// List<ScmFile> changedFiles = new ArrayList<ScmFile>();
	//
	// for ( StatusEntry statusEntry : statusEntries )
	// {
	// ScmFileStatus status = getScmFileStatus( statusEntry );
	// if ( addUnknown || !status.equals( ScmFileStatus.UNKNOWN ) )
	// {
	// changedFiles.add( new ScmFile( statusEntry.getFilePath(), status ) );
	// }
	//
	// }
	// return changedFiles;
	// }

}
