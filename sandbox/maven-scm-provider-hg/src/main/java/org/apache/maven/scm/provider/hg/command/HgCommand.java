package org.apache.maven.scm.provider.hg.command;

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

import org.apache.maven.scm.command.Command;

/**
 * Available/Used hg commands.
 * <p/>
 * These commands do not necessarily correspond to the SCM API.
 * Eg. "check in" is translated to be "commit" and "push".
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public interface HgCommand
    extends Command
{
    /**
     * Executable for Hg
     */
    public static final String EXEC = "hg";

    /**
     * Use to create an empty branch or before importing an existing project
     */
    public static final String INIT_CMD = "init";

    /**
     * Default recursive. Common option: --dry-run and --no-recursive
     */
    public static final String ADD_CMD = "add";

    /**
     * Reports the following states: added, removed, modified, unchanged, unknown
     */
    public static final String STATUS_CMD = "status";

    /**
     * Make a file unversioned
     */
    public static final String REMOVE_CMD = "remove";

    /**
     * Create a new copy of a branch. Alias get or clone
     */
    public static final String BRANCH_CMD = "clone";

    /**
     * Commit changes into a new revision
     */
    public static final String COMMIT_CMD = "commit";

    /**
     * Pull any changes from another branch into the current one
     */
    public static final String PULL_CMD = "pull";

    /**
     * Show log of this branch Common option: --revision
     */
    public static final String LOG_CMD = "log";

    /**
     * Show differences in workingtree. Common option: --revision
     */
    public static final String DIFF_CMD = "diff";

    /**
     * Push this branch into another branch
     */
    public static final String PUSH_CMD = "push";

    /**
     * Show current revision number
     */
    public static final String REVNO_CMD = "id";

    /**
     * Show inventory of the current working copy or a revision
     */
    public static final String INVENTORY_CMD = "locate";

    /**
     * no recurse option does not exist in mercurial
     */
    public static final String NO_RECURSE_OPTION = ""; 

    public static final String MESSAGE_OPTION = "--message";

    public static final String REVISION_OPTION = "-r";

    public static final String VERBOSE_OPTION = "--verbose";

    public static final String VERSION = "version";

    public static final String CHECK = "check";
}
