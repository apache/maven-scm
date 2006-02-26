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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.tmatesoft.svn.core.wc.ISVNStatusHandler} implementation for status
 * events.  Converts the {@link org.tmatesoft.svn.core.wc.SVNStatus} file status to a
 * {@link org.apache.maven.scm.ScmFileStatus}
 * <p/>
 * The structure and comments in this method were taken from the
 * tmate.org example file:
 * <a href="http://svn.tmate.org/repos/jsvn/trunk/doc/examples/src/org/tmatesoft/svn/examples/wc/StatusHandler.java">
 * org.tmatesoft.svn.examples.wc.StatusHandler</a>
 *
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 */
public class SvnStatusHandler
    implements ISVNStatusHandler, ISVNEventHandler
{
    private List files = new ArrayList();

    public SvnStatusHandler()
    {
    }

    /**
     * This is  an  implementation  of {@link ISVNStatusHandler#handleStatus(org.tmatesoft.svn.core.wc.SVNStatus)
     */
    public void handleStatus( SVNStatus status )
    {
        /* Gets  the  status  of  file/directory/symbolic link  text  contents. 
         * It is  SVNStatusType  who  contains  information on the state of  an 
         * item. 
         */
        SVNStatusType contentsStatus = status.getContentsStatus();

        ScmFileStatus scmStatus = null;

        if ( contentsStatus == SVNStatusType.STATUS_MODIFIED )
        {
            scmStatus = ScmFileStatus.MODIFIED;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_CONFLICTED )
        {
            scmStatus = ScmFileStatus.CONFLICT;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_MERGED )
        {
            /* The file item was merGed (changes that came from the  repository 
             * did not overlap local changes and were merged into the file).
             * "G"
             */
            scmStatus = ScmFileStatus.PATCHED;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_DELETED )
        {
            /* The file, directory or symbolic link item has been scheduled for 
             * Deletion from the repository.
             * "D"
             */
            scmStatus = ScmFileStatus.DELETED;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_ADDED )
        {
            /* The file, directory or symbolic link item has been scheduled for 
             * Addition to the repository.
             * "A"
             */
            scmStatus = ScmFileStatus.ADDED;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_UNVERSIONED )
        {
            /* The file, directory or symbolic link item is not  under  version 
             * control.
             * "?"
             */
            scmStatus = ScmFileStatus.UNKNOWN;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_EXTERNAL )
        {
            /* The item is unversioned, but is used by an eXternals definition.
             * "X"
             */
            scmStatus = ScmFileStatus.UNKNOWN;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_IGNORED )
        {
            /* The file, directory or symbolic link item is not  under  version 
             * control, and is configured to be Ignored during 'add',  'import' 
             * and 'status' operations. 
             * "I"
             */
            // We don't care about files that are ignored.
            scmStatus = null;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_MISSING || contentsStatus == SVNStatusType.STATUS_INCOMPLETE )
        {
            /* The file, directory or  symbolic  link  item  is  under  version 
             * control but is missing or somehow incomplete. The  item  can  be 
             * missing if it is removed using a command incompatible  with  the 
             * native Subversion command line client (for example, just removed 
             * from the filesystem). In the case the item is  a  directory,  it 
             * can  be  incomplete if the user happened to interrupt a checkout 
             * or update.
             * "!"
             */
            // TODO: This isn't the right status here.  ScmFileStatus doesn't have an error.
            scmStatus = ScmFileStatus.UNKNOWN;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_OBSTRUCTED )
        {
            /* The file, directory or symbolic link item is in  the  repository 
             * as one kind of object, but what's actually in the user's working 
             * copy is some other kind. For example, Subversion  might  have  a 
             * file in the repository,  but  the  user  removed  the  file  and 
             * created a directory in its place, without using the 'svn delete' 
             * or 'svn add' command (or JavaSVN analogues for them).
             * "~"
             */
            // TODO: This isn't the right status here.  ScmFileStatus doesn't have an error.
            scmStatus = ScmFileStatus.CONFLICT;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_REPLACED )
        {
            /* The file, directory or symbolic link item was  Replaced  in  the 
             * user's working copy; that is, the item was deleted,  and  a  new 
             * item with the same name was added (within  a  single  revision). 
             * While they may have the same name, the repository considers them 
             * to be distinct objects with distinct histories.
             * "R"
             */
            scmStatus = ScmFileStatus.ADDED;
        }
        else if ( contentsStatus == SVNStatusType.STATUS_NONE || contentsStatus == SVNStatusType.STATUS_NORMAL )
        {
            /*
             * The item was not modified (normal).
             * " "
             */
            // Ignore these
            scmStatus = null;
        }

        /*
         * Now getting the status of properties of an item. SVNStatusType  also 
         * contains information on the properties state.
         */
        SVNStatusType propertiesStatus = status.getPropertiesStatus();
        /*
         * Default - properties are normal (unmodified).
         */
        if ( scmStatus == null && propertiesStatus == SVNStatusType.STATUS_MODIFIED )
        {
            /*
             * Properties were modified.
             * "M"
             */
            scmStatus = ScmFileStatus.MODIFIED;
        }
        else if ( scmStatus == null && propertiesStatus == SVNStatusType.STATUS_CONFLICTED )
        {
            /*
             * Properties are in conflict with the repository.
             * "C"
             */
            scmStatus = ScmFileStatus.CONFLICT;
        }

        // Only add files and not directories to our list.
        if ( scmStatus != null && status.getKind() != SVNNodeKind.DIR )
        {
            files.add( new ScmFile( status.getFile().getAbsolutePath(), scmStatus ) );
        }
    }

    /*
     * This is an implementation for 
     * ISVNEventHandler.handleEvent(SVNEvent event, double progress)
     */
    public void handleEvent( SVNEvent event, double progress )
    {

    }

    /*
     * Should be implemented to check if the current operation is cancelled. If 
     * it is, this method should throw an SVNCancelException. 
     */
    public void checkCancelled()
        throws SVNCancelException
    {

    }

    public List getFiles()
    {
        return files;
    }
}