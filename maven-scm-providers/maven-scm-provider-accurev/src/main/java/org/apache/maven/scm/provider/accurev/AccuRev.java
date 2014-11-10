package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.text.DateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.util.ThreadSafeDateFormat;

/**
 * Represents the AccuRev CLI interface
 * 
 * @author ggardner
 */
public interface AccuRev
{

    String DEFAULT_ACCUREV_EXECUTABLE = "accurev";

    int DEFAULT_PORT = 5050;

    String ACCUREV_TIME_FORMAT_STRING = "yyyy/MM/dd HH:mm:ss";

    DateFormat ACCUREV_TIME_SPEC = new ThreadSafeDateFormat( ACCUREV_TIME_FORMAT_STRING );

    String DEFAULT_REMOVE_MESSAGE = "removed (maven-scm)";

    String DEFAULT_ADD_MESSAGE = "initial version (maven-scm)";

    String DEFAULT_PROMOTE_MESSAGE = "promote (maven-scm)";

    /**
     * Reset command process, clear command output accumulators
     */
    void reset();

    /**
     * Populate external to a workspace a (stream) and transactionId/time, to a specific location.
     * 
     * <p>
     * You must check {@link AccuRevCapability#POPULATE_TO_TRANSACTION} before passing a tranid/time
     * to this method. If not supported should pass "now","highest" or null for tranSpec
     * 
     * @param basedir
     * @param stream stream to update to
     * @param tranSpec transaction to update to or "now" if not supported.
     * @param elements (must be depot relative. if null "/./" root is used)
     * @return
     * @throws AccuRevException
     */
    List<File> popExternal( File basedir, String stream, String tranSpec, Collection<File> elements )
        throws AccuRevException;

    /**
     * Re populate missing files to existing workspace.
     * 
     * @param basedir
     * @param elements
     * @return
     * @throws AccuRevException
     */
    List<File> pop( File basedir, Collection<File> elements )
        throws AccuRevException;

    /**
     * Make workspace
     * 
     * @param basisStream
     * @param workspaceName
     * @param basedir
     * @return
     * @throws AccuRevException
     */
    boolean mkws( String basisStream, String workspaceName, File basedir )
        throws AccuRevException;

    /**
     * Update a workspace or reftree, to a particular transaction id
     * 
     * @param basedir
     * @param transactionId
     * @return
     * @throws AccuRevException
     */
    List<File> update( File basedir, String transactionId )
        throws AccuRevException;

    /**
     * Get info about the current logged in user for the current workspace.
     * 
     * @param basedir
     * @return
     */
    AccuRevInfo info( File basedir )
        throws AccuRevException;

    /**
     * Deactivate a workspace
     * 
     * @param workSpaceName full name of the workspace, including the user suffix
     * @return
     */
    boolean rmws( String workSpaceName )
        throws AccuRevException;

    /**
     * Reactivate a workspace
     * 
     * @param workSpaceName full name of the workspace, including the user suffix
     * @return
     */
    boolean reactivate( String workSpaceName )
        throws AccuRevException;

    /**
     * The accurev command line strings since last reset(), separated by ";"
     * 
     * @return
     */
    String getCommandLines();

    /**
     * Full output of accurev command line invocations since reset
     * 
     * @return
     */
    String getErrorOutput();

    /**
     * Add the file to the repository. File must be within a workspace
     * 
     * @param basedir base directory of the workspace
     * @param files to add (relative to basedir, or absolute)
     * @param message the commit message
     */
    List<File> add( File basedir, List<File> files, String message )
        throws AccuRevException;

    /**
     * Remove the file from the repository. Files must be within a workspace
     * 
     * @param basedir
     * @param files
     * @param message
     * @return
     * @throws AccuRevException
     */
    List<File> defunct( File basedir, List<File> files, String message )
        throws AccuRevException;

    /**
     * Any elements that have been kept previously or are currently modified will be promoted.
     * 
     * @param basedir - location of the workspace to act on
     * @param message
     * @return
     * @throws AccuRevException
     */
    List<File> promoteAll( File basedir, String message )
        throws AccuRevException;

    List<File> promote( File basedir, List<File> files, String message )
        throws AccuRevException;

    /**
     * Relocate/reparent a workspace
     * 
     * @param basedir
     * @param workSpaceName (full workspacename including user)
     * @param newBasisStream
     * @return
     * @throws AccuRevException
     */
    boolean chws( File basedir, String workSpaceName, String newBasisStream )
        throws AccuRevException;

    boolean mksnap( String snapShotName, String basisStream )
        throws AccuRevException;

    List<File> statTag( String streamName )
        throws AccuRevException;

    /**
     * Sorts list of elements by whether they exist in the backing stream or not.
     * 
     * @param basedir
     * @param elements
     * @return
     * @throws AccuRevException
     */
    CategorisedElements statBackingStream( File basedir, Collection<File> elements )
        throws AccuRevException;

    /**
     * @param basedir
     * @param elements list of elements to stat, relative to basedir
     * @param statType
     * @return
     * @throws AccuRevException
     */
    List<File> stat( File basedir, Collection<File> elements, AccuRevStat statType )
        throws AccuRevException;

    /**
     * Accurev status of an element
     * 
     * @param element
     * @return null if ignored or not in workspace
     */
    String stat( File element )
        throws AccuRevException;

    List<Transaction> history( String baseStream, String fromTimeSpec, String toTimeSpec, int count,
                               boolean depotHistory, boolean transactionsOnly )
        throws AccuRevException;

    /**
     * AccuRev differences of a stream between to timespecs
     * 
     * @param baseStream
     * @param fromTimeSpec
     * @param toTimeSpec
     * @return
     * @throws AccuRevException
     */
    List<FileDifference> diff( String baseStream, String fromTimeSpec, String toTimeSpec )
        throws AccuRevException;

    /**
     * AccuRev annotate an element
     * 
     * @param file
     * @return
     * @throws AccuRevException
     */
    List<BlameLine> annotate( File baseDir, File file )
        throws AccuRevException;

    /**
     * Logins in as the given user, retains authtoken for use with subsequent commands.
     * 
     * @param user
     * @param password
     * @return
     * @throws AccuRevException
     */
    boolean login( String user, String password )
        throws AccuRevException;

    Map<String, WorkSpace> showWorkSpaces()
        throws AccuRevException;

    Map<String, WorkSpace> showRefTrees()
        throws AccuRevException;

    Stream showStream( String stream )
        throws AccuRevException;

    String getExecutable();

    String getClientVersion()
        throws AccuRevException;

    boolean syncReplica()
        throws AccuRevException;

}