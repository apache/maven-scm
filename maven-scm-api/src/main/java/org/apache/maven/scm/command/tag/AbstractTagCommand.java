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
package org.apache.maven.scm.command.tag;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.CommandParameters.SignOption;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractTagCommand extends AbstractCommand {
    /**
     * @param repository TODO
     * @param fileSet TODO
     * @param tagName TODO
     * @param message TODO
     * @return TODO
     * @throws ScmException if any
     * @deprecated use method {@link #executeTagCommand(ScmProviderRepository, ScmFileSet, String, ScmTagParameters)}
     */
    protected ScmResult executeTagCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, String tagName, String message) throws ScmException {
        return executeTagCommand(repository, fileSet, tagName, new ScmTagParameters(message));
    }

    protected abstract ScmResult executeTagCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, String tagName, ScmTagParameters scmTagParameters)
            throws ScmException;

    /**
     * {@inheritDoc}
     */
    public ScmResult executeCommand(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        String tagName = parameters.getString(CommandParameter.TAG_NAME);

        ScmTagParameters scmTagParameters = parameters.getScmTagParameters(CommandParameter.SCM_TAG_PARAMETERS);

        String message = parameters.getString(CommandParameter.MESSAGE, null);

        if (message != null) {
            // if message was passed by CommandParameter.MESSAGE then use it.
            scmTagParameters.setMessage(message);
        }

        if (scmTagParameters.getMessage() == null) {
            // if message hasn't been passed nor by ScmTagParameters nor by CommandParameter.MESSAGE then use default.
            scmTagParameters.setMessage("[maven-scm] copy for tag " + tagName);
        }

        SignOption signOption = parameters.getSignOption(CommandParameter.SIGN_OPTION);
        if (signOption != null) {
            scmTagParameters.setSignOption(signOption);
        }
        return executeTagCommand(repository, fileSet, tagName, scmTagParameters);
    }
}
