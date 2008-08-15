package org.apache.maven.scm.provider.hg.command;

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

import org.apache.maven.scm.command.Command;

/**
 * Available/Used hg commands.
 * <p/>
 * These commands do not necessarily correspond to the SCM API.
 * Eg. "check in" is translated to be "commit" and "push".
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @version $Id$
 */
public interface HgCommand
    extends Command
{
    /**
     * Executable for Hg
     */
    String EXEC = "hg";

    /**
     * Use to create an empty branch or before importing an existing project
     */
    String INIT_CMD = "init";

    /**
     * Default recursive. Common option: --dry-run and --no-recursive
     */
    String ADD_CMD = "add";

    /**
     * Reports the following states: added, removed, modified, unchanged, unknown
     */
    String STATUS_CMD = "status";

    /**
     * Make a file unversioned
     */
    String REMOVE_CMD = "remove";

    /**
     * Create a new copy of a branch. Alias get or clone
     */
    String BRANCH_CMD = "clone";

    /**
     * Commit changes into a new revision
     */
    String COMMIT_CMD = "commit";

    /**
     * Pull any changes from another branch into the current one
     */
    String PULL_CMD = "pull";

    /**
     * Show log of this branch Common option: --revision
     */
    String LOG_CMD = "log";

    /**
     * Show differences in workingtree. Common option: --revision
     */
    String DIFF_CMD = "diff";

    /**
     * Push this branch into another branch
     */
    String PUSH_CMD = "push";

    /**
     * Show current revision number
     */
    String REVNO_CMD = "id";

    /**
     * Tag this revision
     */
    String TAG_CMD = "tag";

    /**
     * Show list of the current working copy or a revision
     */
    String INVENTORY_CMD = "locate";

    /**
     * no recurse option does not exist in mercurial
     */
    String NO_RECURSE_OPTION = "";

    String MESSAGE_OPTION = "--message";

    String REVISION_OPTION = "-r";

    String VERBOSE_OPTION = "--verbose";

    String VERSION = "version";

    String CHECK = "check";
    String ALL_OPTION = "-A";
}
