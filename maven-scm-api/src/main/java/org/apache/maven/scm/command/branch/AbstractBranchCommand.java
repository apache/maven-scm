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
package org.apache.maven.scm.command.branch;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranchParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public abstract class AbstractBranchCommand extends AbstractCommand {
    protected abstract ScmResult executeBranchCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, String branchName, String message)
            throws ScmException;

    /**
     * default impl to provide backward comp
     * @since 1.3
     * @param repository TODO
     * @param fileSet TODO
     * @param branchName TODO
     * @param scmBranchParameters TODO
     * @return TODO
     * @throws ScmException if any
     */
    protected ScmResult executeBranchCommand(
            ScmProviderRepository repository,
            ScmFileSet fileSet,
            String branchName,
            ScmBranchParameters scmBranchParameters)
            throws ScmException {
        return executeBranchCommand(repository, fileSet, branchName, scmBranchParameters.getMessage());
    }

    /** {@inheritDoc} */
    public ScmResult executeCommand(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        String branchName = parameters.getString(CommandParameter.BRANCH_NAME);

        ScmBranchParameters scmBranchParameters =
                parameters.getScmBranchParameters(CommandParameter.SCM_BRANCH_PARAMETERS);

        String message = parameters.getString(CommandParameter.MESSAGE, "[maven-scm] copy for branch " + branchName);

        if (StringUtils.isBlank(scmBranchParameters.getMessage()) && StringUtils.isNotBlank(message)) {
            scmBranchParameters.setMessage(message);
        }

        return executeBranchCommand(repository, fileSet, branchName, scmBranchParameters);
    }
}
