package org.apache.maven.scm.provider.git.jgit.command;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;

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
 * JGit utility functions.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @version $Id: JGitUtils.java 894145 2009-12-28 10:13:39Z struberg $
 */
public class JGitUtils
{

    private JGitUtils()
    {
        // no op
    }

    /**
     * Construct a logging ProgressMonitor for all JGit operations.
     *
     * @param logger
     * @return a ProgressMonitor for use
     */
    public static ProgressMonitor getMonitor( ScmLogger logger )
    {
        // X TODO write an own ProgressMonitor which logs to ScmLogger!
        return new TextProgressMonitor();
    }

    /**
     * Prepares the in memory configuration of git to connect to the configured
     * repository. It configures the following settings in memory: <br />
     * <li>push url</li> <li>fetch url</li>
     * <p/>
     *
     * @param logger     used to log some details
     * @param git        the instance to configure (only in memory, not saved)
     * @param repository the repo config to be used
     * @return {@link CredentialsProvider} in case there are credentials
     *         informations configured in the repository.
     */
    public static CredentialsProvider prepareSession( ScmLogger logger, Git git, GitScmProviderRepository repository )
    {
        StoredConfig config = git.getRepository().getConfig();
        config.setString( "remote", "origin", "url", repository.getFetchUrl() );
        config.setString( "remote", "origin", "pushURL", repository.getPushUrl() );

        // make sure we do not log any passwords to the output
        String password =
            StringUtils.isNotBlank( repository.getPassword() ) ? repository.getPassword().trim() : "no-pwd-defined";
        logger.info( "fetch url: " + repository.getFetchUrl().replace( password, "******" ) );
        logger.info( "push url: " + repository.getPushUrl().replace( password, "******" ) );
        return getCredentials( repository );
    }

    /**
     * Creates a credentials provider from the information passed in the
     * repository. Current implementation supports: <br />
     * <li>UserName/Password</li>
     * <p/>
     *
     * @param repository the config to get the details from
     * @return <code>null</code> if there is not enough info to create a
     *         provider with
     */
    public static CredentialsProvider getCredentials( GitScmProviderRepository repository )
    {
        if ( StringUtils.isNotBlank( repository.getUser() ) && StringUtils.isNotBlank( repository.getPassword() ) )
        {
            return new UsernamePasswordCredentialsProvider( repository.getUser().trim(),
                                                            repository.getPassword().trim() );
        }
        return null;
    }

    public static Iterable<PushResult> push( ScmLogger logger, Git git, GitScmProviderRepository repo, RefSpec refSpec )
        throws GitAPIException, InvalidRemoteException, TransportException
    {
        CredentialsProvider credentials = JGitUtils.prepareSession( logger, git, repo );
        Iterable<PushResult> pushResultList =
            git.push().setCredentialsProvider( credentials ).setRefSpecs( refSpec ).call();
        for ( PushResult pushResult : pushResultList )
        {
            Collection<RemoteRefUpdate> ru = pushResult.getRemoteUpdates();
            for ( RemoteRefUpdate remoteRefUpdate : ru )
            {
                logger.info( remoteRefUpdate.getStatus() + " - " + remoteRefUpdate.toString() );
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
    public static boolean hasCommits( Repository repo )
    {
        if ( repo != null && repo.getDirectory().exists() )
        {
            return ( new File( repo.getDirectory(), "objects" ).list().length > 2 ) || (
                new File( repo.getDirectory(), "objects/pack" ).list().length > 0 );
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
    public static List<ScmFile> getFilesInCommit( Repository repository, RevCommit commit )
        throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException
    {
        List<ScmFile> list = new ArrayList<ScmFile>();
        if ( JGitUtils.hasCommits( repository ) )
        {
            TreeWalk tw = new TreeWalk( repository );
            tw.reset();
            tw.setRecursive( true );
            tw.addTree( commit.getTree() );
            while ( tw.next() )
            {
                list.add( new ScmFile( tw.getPathString(), ScmFileStatus.CHECKED_IN ) );
            }
            tw.release();
        }
        return list;
    }

    /**
     * Translate a {@code FileStatus} in the matching {@code ScmFileStatus}.
     *
     * @param status
     * @return the matching ScmFileStatus
     * @throws ScmException if the given Status cannot be translated
     */
    public static ScmFileStatus getScmFileStatus( ChangeType changeType )
        throws ScmException
    {
        switch ( changeType )
        {
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
     * @return a list of files changed
     * @throws GitAPIException
     * @throws NoFilepatternException
     */
    public static List<ScmFile> addAllFiles( Git git, ScmFileSet fileSet )
        throws GitAPIException, NoFilepatternException
    {
        URI baseUri = fileSet.getBasedir().toURI();
        AddCommand add = git.add();
        for ( File file : fileSet.getFileList() )
        {
            if ( file.exists() )
            {
                String path = relativize( baseUri, file );
                add.addFilepattern( path );
            }
        }
        add.call();

        Status status = git.status().call();
        Set<String> changed = status.getChanged();

        List<ScmFile> changedFiles = new ArrayList<ScmFile>();

        // rewrite all detected files to now have status 'checked_in'
        for ( String entry : changed )
        {
            ScmFile scmfile = new ScmFile( entry, ScmFileStatus.MODIFIED );

            // if a specific fileSet is given, we have to check if the file is
            // really tracked
            for ( Iterator<File> itfl = fileSet.getFileList().iterator(); itfl.hasNext(); )
            {
                String path = relativize( baseUri, itfl.next() );
                if ( path.equals( scmfile.getPath() ) )
                {
                    changedFiles.add( scmfile );
                }
            }
        }
        return changedFiles;
    }

    private static String relativize( URI baseUri, File f )
    {
        String path = f.getPath();
        if ( f.isAbsolute() )
        {
            path = baseUri.relativize( new File( path ).toURI() ).getPath();
        }
        return path;
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
    public static List<RevCommit> getRevCommits( Repository repo, RevSort[] sortings, String fromRev, String toRev,
                                                 Date fromDate, Date toDate, int maxLines )
        throws IOException, MissingObjectException, IncorrectObjectTypeException
    {
        List<RevCommit> revs = new ArrayList<RevCommit>();
        RevWalk walk = new RevWalk( repo );

        ObjectId fromRevId = fromRev != null ? repo.resolve( fromRev ) : null;
        ObjectId toRevId = toRev != null ? repo.resolve( toRev ) : null;

        if ( sortings == null || sortings.length == 0 )
        {
            sortings = new RevSort[]{ RevSort.TOPO, RevSort.COMMIT_TIME_DESC };
        }

        for ( final RevSort s : sortings )
        {
            walk.sort( s, true );
        }

        if ( fromDate != null && toDate != null )
        {
            walk.setRevFilter( CommitTimeRevFilter.between( fromDate, toDate ) );
        }
        else
        {
            if ( fromDate != null )
            {
                walk.setRevFilter( CommitTimeRevFilter.after( fromDate ) );
            }

            if ( toDate != null )
            {
                walk.setRevFilter( CommitTimeRevFilter.before( toDate ) );
            }
        }

        if ( fromRevId != null )
        {
            RevCommit c = walk.parseCommit( fromRevId );
            c.add( RevFlag.UNINTERESTING );
            RevCommit real = walk.parseCommit( c );
            walk.markUninteresting( real );
        }

        if ( toRevId != null )
        {
            RevCommit c = walk.parseCommit( toRevId );
            c.remove( RevFlag.UNINTERESTING );
            RevCommit real = walk.parseCommit( c );
            walk.markStart( real );
        }
        else
        {
            final ObjectId head = repo.resolve( Constants.HEAD );
            if ( head == null )
            {
                throw new RuntimeException( "Cannot resolve " + Constants.HEAD );
            }
            RevCommit real = walk.parseCommit( head );
            walk.markStart( real );
        }

        int n = 0;
        for ( final RevCommit c : walk )
        {
            n++;
            if ( maxLines != -1 && n > maxLines )
            {
                break;
            }

            revs.add( c );
        }
        return revs;
    }

}
