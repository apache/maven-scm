package org.apache.maven.scm.provider.svn.svnjava.util;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmFileStatus;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Static helper library to consolidate calls to the {@link org.tmatesoft.svn.core.wc.SVNWCClient}.
 * The procedure comments were taken from the tmate.org example file:
 * <a href="http://svn.tmate.org/repos/jsvn/trunk/doc/examples/src/org/tmatesoft/svn/examples/wc/WorkingCopy.java">
 * org.tmatesoft.svn.examples.wc.WorkingCopy.java</a>
 *
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 */
public final class SvnJavaUtil
{
    private static final long MAX_LOG_ENTRIES = 1024;

    private SvnJavaUtil()
    {
        super();
    }

    /**
     * Converts a {@link SVNEventAction} to a {@link ScmFileStatus}
     */
    public static ScmFileStatus getScmFileStatus( SVNEventAction action )
    {
        // Updates
        if ( action == SVNEventAction.ADD )
        {
            return ScmFileStatus.ADDED;
        }
        else if ( action == SVNEventAction.UPDATE_ADD )
        {
            return ScmFileStatus.ADDED;
        }
        else if ( action == SVNEventAction.UPDATE_DELETE )
        {
            return ScmFileStatus.DELETED;
        }
        else if ( action == SVNEventAction.UPDATE_UPDATE )
        {
            return ScmFileStatus.UPDATED;
        }
        // Commits
        else if ( action == SVNEventAction.COMMIT_ADDED )
        {
            return ScmFileStatus.ADDED;
        }
        //else if ( action == SVNEventAction.COMMIT_COMPLETED )
        else if ( action == SVNEventAction.COMMIT_DELETED )
        {
            return ScmFileStatus.DELETED;
        }
        else if ( action == SVNEventAction.RESTORE )
        {
            // Denotes that the deleted item is restored (prior to be updated).
            return ScmFileStatus.UPDATED;
        }
//        else if ( action == SVNEventAction.COMMIT_DELTA_SENT )
//        {
//            return ScmFileStatus.MODIFIED;
//        }
        else if ( action == SVNEventAction.COMMIT_REPLACED )
        {
            return ScmFileStatus.CHECKED_IN;
        }
        else if ( action == SVNEventAction.COMMIT_MODIFIED )
        {
            return ScmFileStatus.CHECKED_IN;
        }
        else
        {
            return null;
        }
    }

    /**
     * Puts directories and files under version control scheduling them for addition
     * to a repository. They will be added in a next commit. Like 'svn add PATH'
     * command. It's done by invoking
     * <p/>
     * SVNWCClient.doAdd(File path, boolean force,
     * boolean mkdir, boolean climbUnversionedParents, boolean recursive)
     * <p/>
     * which takes the following parameters:
     * <p/>
     * path - an entry to be scheduled for addition;
     * <p/>
     * force - set to true to force an addition of an entry anyway;
     * <p/>
     * mkdir - if true doAdd(..) creates an empty directory at path and schedules
     * it for addition, like 'svn mkdir PATH' command;
     * <p/>
     * climbUnversionedParents - if true and the parent of the entry to be scheduled
     * for addition is not under version control, then doAdd(..) automatically schedules
     * the parent for addition, too;
     * <p/>
     * recursive - if true and an entry is a directory then doAdd(..) recursively
     * schedules all its inner dir entries for addition as well.
     */
    public static void add( SVNClientManager clientManager, File wcPath, boolean recursive )
        throws SVNException
    {
        clientManager.getWCClient().doAdd( wcPath, false,  // force
                                           false,  // mkdir
                                           false,  // climbUnversionedParents
                                           recursive );
    }

    /**
     * Checks out a working copy from a repository. Like 'svn checkout URL[@REV] PATH (-r..)'
     * command; It's done by invoking
     * <p/>
     * SVNUpdateClient.doCheckout(SVNURL url, File dstPath, SVNRevision pegRevision,
     * SVNRevision revision, boolean recursive)
     * <p/>
     * which takes the following parameters:
     * <p/>
     * url - a repository location from where a working copy is to be checked out;
     * <p/>
     * dstPath - a local path where the working copy will be fetched into;
     * <p/>
     * pegRevision - an SVNRevision representing a revision to concretize
     * url (what exactly URL a user means and is sure of being the URL he needs); in other
     * words that is the revision in which the URL is first looked up;
     * <p/>
     * revision - a revision at which a working copy being checked out is to be;
     * <p/>
     * recursive - if true and url corresponds to a directory then doCheckout(..) recursively
     * fetches out the entire directory, otherwise - only child entries of the directory;
     */
    public static long checkout( SVNClientManager clientManager, SVNURL url, SVNRevision revision, File destPath,
                                 boolean isRecursive )
        throws SVNException
    {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the checkout
         */
        updateClient.setIgnoreExternals( false );
        /*
         * returns the number of the revision at which the working copy is 
         */
        return updateClient.doCheckout( url, destPath, revision, revision, isRecursive );
    }

    /**
     * Updates a working copy to a different URL. Like 'svn switch URL' command.
     * It's done by invoking
     * <p/>
     * SVNUpdateClient.doSwitch(File file, SVNURL url, SVNRevision revision, boolean recursive)
     * <p/>
     * which takes the following parameters:
     * <p/>
     * file - a working copy entry that is to be switched to a new url;
     * <p/>
     * url - a target URL a working copy is to be updated against;
     * <p/>
     * revision - a revision to which a working copy is to be updated;
     * <p/>
     * recursive - if true and an entry (file) is a directory then doSwitch(..) recursively
     * switches the entire directory, otherwise - only child entries of the directory;
     */
    public static long switchToURL( SVNClientManager clientManager, File wcPath, SVNURL url,
                                    SVNRevision updateToRevision, boolean isRecursive )
        throws SVNException
    {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();

        /*
         * sets externals not to be ignored during the switch
         */
        updateClient.setIgnoreExternals( false );

        /*
         * returns the number of the revision wcPath was updated to
         */
        return updateClient.doSwitch( wcPath, url, updateToRevision, isRecursive );
    }

    /**
     * Updates a working copy (brings changes from the repository into the working copy).
     * Like 'svn update PATH' command; It's done by invoking
     * <p/>
     * SVNUpdateClient.doUpdate(File file, SVNRevision revision, boolean recursive)
     * <p/>
     * which takes the following parameters:
     * <p/>
     * file - a working copy entry that is to be updated;
     * <p/>
     * revision - a revision to which a working copy is to be updated;
     * <p/>
     * recursive - if true and an entry is a directory then doUpdate(..) recursively
     * updates the entire directory, otherwise - only child entries of the directory;
     */
    public static long update( SVNClientManager clientManager, File wcPath, SVNRevision updateToRevision,
                               boolean isRecursive )
        throws SVNException
    {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the update
         */
        updateClient.setIgnoreExternals( false );
        /*
         * returns the number of the revision wcPath was updated to
         */
        return updateClient.doUpdate( wcPath, updateToRevision, isRecursive );
    }

    public static void changelog( SVNClientManager clientManager, SVNURL svnUrl, SVNRevision startRevision,
                                  SVNRevision endRevision, boolean stopOnCopy, boolean reportPaths,
                                  ISVNLogEntryHandler handler )
        throws SVNException
    {
        SVNLogClient logClient = clientManager.getLogClient();

        logClient.doLog( svnUrl, null, startRevision, startRevision, endRevision, stopOnCopy, reportPaths,
                         MAX_LOG_ENTRIES, handler );
    }

    /**
     * Commits changes in a working copy to a repository. Like
     * 'svn commit PATH -m "some comment"' command. It's done by invoking
     * <p/>
     * SVNCommitClient.doCommit(File[] paths, boolean keepLocks, String commitMessage,
     * boolean force, boolean recursive)
     * <p/>
     * which takes the following parameters:
     * <p/>
     * paths - working copy paths which changes are to be committed;
     * <p/>
     * keepLocks - if true then doCommit(..) won't unlock locked paths; otherwise they will
     * be unlocked after a successful commit;
     * <p/>
     * commitMessage - a commit log message;
     * <p/>
     * force - if true then a non-recursive commit will be forced anyway;
     * <p/>
     * recursive - if true and a path corresponds to a directory then doCommit(..) recursively
     * commits changes for the entire directory, otherwise - only for child entries of the
     * directory;
     */
    public static SVNCommitInfo commit( SVNClientManager clientManager, File[] paths, boolean keepLocks,
                                        String commitMessage, boolean recursive )
        throws SVNException
    {
        /*
         * Returns SVNCommitInfo containing information on the new revision committed 
         * (revision number, etc.) 
         */
        return clientManager.getCommitClient().doCommit( paths, keepLocks, commitMessage, false, recursive );
    }

    /**
     * Schedules directories and files for deletion from version control upon the next
     * commit (locally). Like 'svn delete PATH' command. It's done by invoking
     * <p/>
     * SVNWCClient.doDelete(File path, boolean force, boolean dryRun)
     * <p/>
     * which takes the following parameters:
     * <p/>
     * path - an entry to be scheduled for deletion;
     * <p/>
     * force - a boolean flag which is set to true to force a deletion even if an entry
     * has local modifications;
     * <p/>
     * dryRun - set to true not to delete an entry but to check if it can be deleted;
     * if false - then it's a deletion itself.
     */
    public static void delete( SVNClientManager clientManager, File[] files, boolean force )
        throws SVNException
    {
        for ( int i = 0; i < files.length; i++ )
        {
            clientManager.getWCClient().doDelete( files[i], force, false );
        }
    }

    /**
     * Collects status information on local path(s). Like 'svn status (-u) (-N)'
     * command. It's done by invoking
     * <p/>
     * SVNStatusClient.doStatus(File path, boolean recursive,
     * boolean remote, boolean reportAll, boolean includeIgnored,
     * boolean collectParentExternals, ISVNStatusHandler handler)
     * <p/>
     * which takes the following parameters:
     * <p/>
     * path - an entry which status info to be gathered;
     * <p/>
     * recursive - if true and an entry is a directory then doStatus(..) collects status
     * info not only for that directory but for each item inside stepping down recursively;
     * <p/>
     * remote - if true then doStatus(..) will cover the repository (not only the working copy)
     * as well to find out what entries are out of date;
     * <p/>
     * reportAll - if true then doStatus(..) will also include unmodified entries;
     * <p/>
     * includeIgnored - if true then doStatus(..) will also include entries being ignored;
     * <p/>
     * collectParentExternals - if true then externals definitions won't be ignored;
     * <p/>
     * handler - an implementation of ISVNStatusHandler to process status info per each entry
     * doStatus(..) traverses; such info is collected in an SVNStatus object and
     * is passed to a handler's handleStatus(SVNStatus status) method where an implementor
     * decides what to do with it.
     */
    public static void status( SVNClientManager clientManager, File wcPath, boolean isRecursive, boolean isRemote,
                               SvnStatusHandler handler )
        throws SVNException
    {
        boolean isIncludeIgnored = true;
        boolean isReportAll = true;
        boolean isCollectParentExternals = true;

        clientManager.getStatusClient().doStatus( wcPath, isRecursive, isRemote, isReportAll, isIncludeIgnored,
                                                  isCollectParentExternals, handler );
    }

    /*
    * Duplicates srcURL to dstURL (URL->URL)in a repository remembering history.
    * Like 'svn copy srcURL dstURL -m "some comment"' command. It's done by
    * invoking
    *
    * doCopy(SVNURL srcURL, SVNRevision srcRevision, SVNURL dstURL,
    * boolean isMove, String commitMessage)
    *
    * which takes the following parameters:
    *
    * srcURL - a source URL that is to be copied;
    *
    * srcRevision - a definite revision of srcURL
    *
    * dstURL - a URL where srcURL will be copied; if srcURL & dstURL are both
    * directories then there are two cases:
    * a) dstURL already exists - then doCopy(..) will duplicate the entire source
    * directory and put it inside dstURL (for example,
    * consider srcURL = svn://localhost/rep/MyRepos,
    * dstURL = svn://localhost/rep/MyReposCopy, in this case if doCopy(..) succeeds
    * MyRepos will be in MyReposCopy - svn://localhost/rep/MyReposCopy/MyRepos);
    * b) dstURL doesn't exist yet - then doCopy(..) will create a directory and
    * recursively copy entries from srcURL into dstURL (for example, consider the same
    * srcURL = svn://localhost/rep/MyRepos, dstURL = svn://localhost/rep/MyReposCopy,
    * in this case if doCopy(..) succeeds MyRepos entries will be in MyReposCopy, like:
    * svn://localhost/rep/MyRepos/Dir1 -> svn://localhost/rep/MyReposCopy/Dir1...);
    *
    * isMove - if false then srcURL is only copied to dstURL what
    * corresponds to 'svn copy srcURL dstURL -m "some comment"'; but if it's true then
    * srcURL will be copied and deleted - 'svn move srcURL dstURL -m "some comment"';
    *
    * commitMessage - a commit log message since URL->URL copying is immediately
    * committed to a repository.
    */
    public static SVNCommitInfo copy( SVNClientManager clientManager, SVNURL srcURL, SVNURL dstURL, boolean isMove,
                                      String commitMessage )
        throws SVNException
    {
        /*
         * SVNRevision.HEAD means the latest revision.
         * Returns SVNCommitInfo containing information on the new revision committed 
         * (revision number, etc.) 
         */
        return clientManager.getCopyClient().doCopy( srcURL, SVNRevision.HEAD, dstURL, isMove, commitMessage );
    }

    public static ByteArrayOutputStream diff( SVNClientManager clientManager, File baseDir, SVNRevision startRevision,
                                              SVNRevision endRevision )
        throws SVNException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        /*
         * SVNRevision.HEAD means the latest revision.
         * Returns SVNCommitInfo containing information on the new revision committed 
         * (revision number, etc.) 
         */
        clientManager.getDiffClient().doDiff( baseDir, startRevision, startRevision, endRevision, true, true, result );

        return result;
    }

    /**
     * Generic event handler that collects all events internally and will return them with a call to
     * {@link #getEvents()}
     */
    public static class GenericEventHandler
        implements ISVNEventHandler
    {
        private final List events = new ArrayList();

        public GenericEventHandler()
        {
        }

        public void handleEvent( SVNEvent event, double progress )
        {
            events.add( event );
        }

        public void checkCancelled()
            throws SVNCancelException
        {
            // null
        }

        public List getEvents()
        {
            return events;
        }
    }
}
