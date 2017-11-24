package org.apache.maven.scm;

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

import java.io.Serializable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class CommandParameter
    implements Serializable
{
    private static final long serialVersionUID = -3391190831054016735L;

    public static final CommandParameter BINARY = new CommandParameter( "binary" );

    public static final CommandParameter RECURSIVE = new CommandParameter( "recursive" );

    public static final CommandParameter SHALLOW = new CommandParameter( "shallow" );

    public static final CommandParameter MESSAGE = new CommandParameter( "message" );

    public static final CommandParameter BRANCH_NAME = new CommandParameter( "branchName" );

    public static final CommandParameter START_DATE = new CommandParameter( "startDate" );

    public static final CommandParameter END_DATE = new CommandParameter( "endDate" );

    public static final CommandParameter NUM_DAYS = new CommandParameter( "numDays" );

    public static final CommandParameter LIMIT = new CommandParameter( "limit" );

    public static final CommandParameter BRANCH = new CommandParameter( "branch" );

    public static final CommandParameter START_SCM_VERSION = new CommandParameter( "startScmVersion" );

    public static final CommandParameter END_SCM_VERSION = new CommandParameter( "endScmVersion" );

    public static final CommandParameter CHANGELOG_DATE_PATTERN = new CommandParameter( "changelogDatePattern" );

    public static final CommandParameter SCM_VERSION = new CommandParameter( "scmVersion" );

    public static final CommandParameter TAG_NAME = new CommandParameter( "tagName" );

    public static final CommandParameter FILE = new CommandParameter( "file" );

    public static final CommandParameter FILES = new CommandParameter( "files" );

    public static final CommandParameter OUTPUT_FILE = new CommandParameter( "outputFile" );

    public static final CommandParameter OUTPUT_DIRECTORY = new CommandParameter( "outputDirectory" );

    public static final CommandParameter RUN_CHANGELOG_WITH_UPDATE =
        new CommandParameter( "run_changelog_with_update" );

    public static final CommandParameter SCM_TAG_PARAMETERS = new CommandParameter( "ScmTagParameters" );

    public static final CommandParameter SCM_BRANCH_PARAMETERS = new CommandParameter( "ScmBranchParameters" );

    public static final CommandParameter SCM_MKDIR_CREATE_IN_LOCAL = new CommandParameter( "createInLocal" );

    /**
     * Parameter used only for Git SCM and simulate the <code>git rev-parse --short=lenght</code> command.
     *
     * @since 1.7
     */
    public static final CommandParameter SCM_SHORT_REVISION_LENGTH = new CommandParameter( "shortRevisionLength" );

    /**
     * Parameter to force add
     *
     * @since 1.7
     */
    public static final CommandParameter FORCE_ADD = new CommandParameter( "forceAdd" );

    /**
     * contains true or false
     * @since 1.8
     */
    public static final CommandParameter IGNORE_WHITESPACE = new CommandParameter( "ignoreWhitespace" );


    /**
     * Parameter name
     */
    private String name;

    /**
     * @param name The parameter name
     */
    private CommandParameter( String name )
    {
        this.name = name;
    }

    /**
     * @return The parameter name
     */
    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return name;
    }
}
