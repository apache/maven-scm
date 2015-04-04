package org.apache.maven.scm.provider.jazz.command;

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
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzConstants
{
    // -------------------------------------------------------------------------------------------------
    // MISC.
    // -------------------------------------------------------------------------------------------------

    /**
     * Executable for Jazz SCM (Rational Team Concert).
     */
    public static final String SCM_EXECUTABLE = "scm";

    /**
     * Folder created by the SCM to store metadata.
     */
    public static final String SCM_META_DATA_FOLDER = ".jazz5";

    /**
     * SCM type identifier
     */
    public static final String SCM_TYPE = "jazz";

    // -------------------------------------------------------------------------------------------------
    // COMMANDS
    // -------------------------------------------------------------------------------------------------

    /**
     * Accept command - Accept change sets into a repository workspace and load them into the local workspace.
     */
    public static final String CMD_ACCEPT = "accept";

    /**
     * Annotate command - Show line-by-line revision information for a file.
     */
    public static final String CMD_ANNOTATE = "annotate";

    /**
     * Checkin command - Check in locally modified files, adding them to the current change set.
     */
    public static final String CMD_CHECKIN = "checkin";

    /**
     * Create command - Can be used with a sub command to create a snapshot (tag) or repository workspace (branch).
     */
    public static final String CMD_CREATE = "create";

    /**
     * Deliver command - Deliver to a target.
     */
    public static final String CMD_DELIVER = "deliver";

    /**
     * Diff command - Compare two states of a file.
     */
    public static final String CMD_DIFF = "diff";

    /**
     * History command - Show the history of a file or component.
     */
    public static final String CMD_HISTORY = "history";

    /**
     * List command - List repository objects.
     */
    public static final String CMD_LIST = "list";

    /**
     * Load command - Load components from a repository workspace into a local workspace.
     */
    public static final String CMD_LOAD = "load";

    /**
     * Lock command - Used to lock or revoke locks on files in a stream. (requires 'acquire' or 'release' sub commands).
     */
    public static final String CMD_LOCK = "lock";

    /**
     * Snapshot command - Used to promote snapshots. Requires the 'promote' sub command.
     */
    public static final String CMD_SNAPSHOT = "snapshot";

    /**
     * Status command - Show modification status of items in a workspace.
     */
    public static final String CMD_STATUS = "status";

    /**
     * Changeset command - Modifies change sets.
     */
    public static final String CMD_CHANGESET = "changeset";

    // -------------------------------------------------------------------------------------------------
    // SUB-COMMANDS
    // -------------------------------------------------------------------------------------------------

    // CREATE sub commands

    /**
     * The 'type' (snapshot) of the create command.
     */
    public static final String CMD_SUB_SNAPSHOT = "snapshot";

    /**
     * The 'type' (workspace) of the create command.
     */
    public static final String CMD_SUB_WORKSPACE = "workspace";

    /**
     * The 'type' (changeset) of the create command.
     */
    public static final String CMD_SUB_CHANGESET = "changeset";

    // LIST sub commands

    /**
     * List files in a remote workspace.
     */
    public static final String CMD_SUB_REMOTEFILES = "remotefiles";

    /**
     * List files in a remote workspace.
     */
    public static final String CMD_SUB_CHANGESETS = "changesets";

    // LOCK sub commands

    /**
     * Locks files in a stream.
     */
    public static final String CMD_SUB_ACQUIRE = "acquire";

    /**
     * Revoke locks on files in a stream.
     */
    public static final String CMD_SUB_RELEASE = "release";

    // SNAPSHOT sub commands

    /**
     * Promotes a snapshot to a stream or workspace.
     */
    public static final String CMD_SUB_PROMOTE = "promote";

    // CHANGESET sub commands

    /**
     * Associate a Work Item with a change set.
     */
    public static final String CMD_SUB_ASSOCIATE = "associate";

    // -------------------------------------------------------------------------------------------------
    // ARGUMENTS
    // -------------------------------------------------------------------------------------------------

    /**
     * Accept component additions and deletions (used with "accept" command).
     */
    public static final String ARG_FLOW_COMPONENTS = "--flow-components";

    /**
     * Overwrite existing files when loading (used with "load" command).
     */
    public static final String ARG_FORCE = "--force";

    /**
     * Local workspace path.
     */
    public static final String ARG_LOCAL_WORKSPACE_PATH = "--dir";

    /**
     * Load Root Directory.
     */
    public static final String ARG_LOAD_ROOT_DIRECTORY = "--directory";

    /**
     * The repository name.
     */
    public static final String ARG_REPOSITORY_URI = "--repository-uri";

    /**
     * Description for the snapshot (used with "create snapshot" command).
     */
    public static final String ARG_SNAPSHOT_DESCRIPTION = "--description";

    /**
     * Name of the snapshot (used with "create snapshot" command).
     */
    public static final String ARG_SNAPSHOT_NAME = "--name";

    /**
     * Forces the output to not shorten, otherwise the width will be based on the COLUMNS environment variable, or if
     * that is not set, to 80 characters. (used with the "status" command).
     */
    public static final String ARG_STATUS_WIDE_PRINT_OUT = "--wide";

    /**
     * The user ID in the repository.
     */
    public static final String ARG_USER_NAME = "--username";

    /**
     * The user password in the repository.
     */
    public static final String ARG_USER_PASSWORD = "--password";

    /**
     * Description for the repository workspace (used with "create workspace" command).
     */
    public static final String ARG_WORKSPACE_DESCRIPTION = "--description";

    /**
     * Name of the repository workspace (used with "create workspace" command).
     */
    public static final String ARG_WORKSPACE_NAME = "--name";

    /**
     * Name of the repository workspace (used with "create workspace" command).
     */
    public static final String ARG_WORKSPACE_SNAPSHOT = "--snapshot";

    /**
     * Name of the source repository workspace (used with "deliver" command).
     */
    public static final String ARG_DELIVER_SOURCE = "--source";

    /**
     * Name of the target repository workspace or stream (used with "deliver" command).
     */
    public static final String ARG_DELIVER_TARGET = "--target";

    /**
     * Ignore uncommitted changes and deliver (used with "deliver" command).
     */
    public static final String ARG_OVERWRITE_UNCOMMITTED = "--overwrite-uncommitted";

    /**
     * Perform a file base diff (aftertype = file) (used with "diff" command).
     */
    public static final String ARG_FILE = "file";

    /**
     * Specify the maximum number of results to return, must be greater than zero.
     * Used by numerous commands.
     */
    public static final String ARG_MAXIMUM = "--maximum";

    /**
     * Name of the repository workspace (used with "list changesets" command).
     */
    public static final String ARG_WORKSPACE = "--workspace";

}