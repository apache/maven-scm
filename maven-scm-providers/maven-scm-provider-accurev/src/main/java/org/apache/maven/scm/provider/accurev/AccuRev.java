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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents the AccuRev CLI interface
 * 
 * @author ggardner
 */
public interface AccuRev
{

    public static final SimpleDateFormat ACCUREV_TIME_SPEC = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );

    public static final String DEFAULT_REMOVE_MESSAGE = "removed (maven-scm)";

    String DEFAULT_ADD_MESSAGE = "initial version (maven-scm)";

    String DEFAULT_PROMOTE_MESSAGE = "promote (maven-scm)";

    /**
     * Reset command process, clear command output accumulators
     */
    void reset();

    /**
     * Populate external to a workspace a specific version, to a specific location.
     * 
     * @param basedir
     * @param versionSpec
     * @param elements (must be depot relative. if null "/./" root is used)
     * @param poppedFiles
     * @return
     * @throws AccuRevException
     */
    boolean pop( File basedir, String versionSpec, Collection<File> elements, List<File> poppedFiles )
        throws AccuRevException;

    /**
     * Re populate missing files to existing workspace.
     * 
     * @param basedir
     * @param elements
     * @param poppedFiles
     * @return
     * @throws AccuRevException
     */
    boolean pop( File basedir, Collection<File> elements, List<File> poppedFiles )
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
     * @param updatedFiles
     * @return
     * @throws AccuRevException
     */
    boolean update( File basedir, String transactionId, List<File> updatedFiles )
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
     * @param addedFiles , list to which accurev will confirm the added files
     */
    boolean add( File basedir, List<File> files, String message, List<File> addedFiles )
        throws AccuRevException;

    /**
     * Remove the file from the repository. Files must be within a workspace
     * 
     * @param basedir
     * @param files
     * @param message
     * @param defunctedFiles
     * @return
     * @throws AccuRevException
     */
    boolean defunct( File basedir, List<File> files, String message, List<File> defunctedFiles )
        throws AccuRevException;

    /**
     * Any elements that have been kept previously or are currently modified will be promoted.
     * 
     * @param basedir - location of the workspace to act on
     * @param message
     * @param promotedFiles list to which the promoted elements should be added
     * @return
     * @throws AccuRevException
     */
    boolean promoteAll( File basedir, String message, List<File> promotedFiles )
        throws AccuRevException;

    boolean promote( File basedir, List<File> files, String message, List<File> promotedFiles )
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

    boolean statTag( String streamName, List<File> taggedFiles )
        throws AccuRevException;

    /**
     * Sorts list of elements by whether they exist in the backing stream or not.
     * 
     * @param basedir
     * @param elements
     * @param memberElements
     * @param nonMemberElements
     * @return
     * @throws AccuRevException
     */
    boolean statBackingStream( File basedir, Collection<File> elements, Collection<File> memberElements,
                               Collection<File> nonMemberElements )
        throws AccuRevException;

    /**
     * @param basedir
     * @param elements list of elements to stat, relative to basedir
     * @param statType
     * @param matchingElements list of files relative to basedir that match the statType
     * @return
     * @throws AccuRevException
     */
    boolean stat( File basedir, Collection<File> elements, AccuRevStat statType, List<File> matchingElements )
        throws AccuRevException;

    /**
     * Accurev status of an element
     * 
     * @param element
     * @return null if ignored or not in workspace
     */
    String stat( File element )
        throws AccuRevException;

    boolean history( String baseStream, String fromTimeSpec, String toTimeSpec, int count,
                     List<Transaction> transactions )
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

    boolean showWorkSpaces( Map<String, WorkSpace> workSpaces )
        throws AccuRevException;

    boolean showRefTrees( Map<String, WorkSpace> workSpaces )
        throws AccuRevException;

}