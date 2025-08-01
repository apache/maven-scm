/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.command.mkdir;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 *
 */
public abstract class AbstractMkdirCommand extends AbstractCommand<MkdirScmResult> {
    /**
     * Creates directories in the remote repository.
     *
     * @param repository TODO
     * @param fileSet TODO
     * @param message TODO
     * @param createInLocal TODO
     * @return TODO
     * @throws ScmException if any
     */
    protected abstract MkdirScmResult executeMkdirCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, String message, boolean createInLocal)
            throws ScmException;

    /** {@inheritDoc} */
    protected MkdirScmResult executeCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
        if (fileSet.getFileList().isEmpty()) {
            throw new IllegalArgumentException("fileSet can not be empty");
        }

        return executeMkdirCommand(
                repository,
                fileSet,
                parameters.getString(CommandParameter.MESSAGE),
                parameters.getBoolean(CommandParameter.SCM_MKDIR_CREATE_IN_LOCAL));
    }
}
