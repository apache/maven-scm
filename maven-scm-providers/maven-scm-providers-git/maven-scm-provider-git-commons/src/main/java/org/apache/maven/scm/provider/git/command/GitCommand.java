package org.apache.maven.scm.provider.git.command;

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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Dominik Bartholdi
 */
public interface GitCommand
    extends Command
{
    /**
     * Whether the filesets passed to the command should be reset to the root of the repository prior to execute the
     * command
     * 
     * @return <code>true</code> if the fileset must have the repository as the basedirectory.
     * @see org.apache.maven.scm.provider.git.AbstractGitScmProvider.executeCommand(GitCommand, ScmProviderRepository,
     *      ScmFileSet, CommandParameters)
     */
    boolean requiresToWorkInRepoRootDir();
}
