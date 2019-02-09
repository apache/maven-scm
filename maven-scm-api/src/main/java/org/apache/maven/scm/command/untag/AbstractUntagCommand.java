package org.apache.maven.scm.command.untag;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmUntagParameters;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/** {@inheritDoc} */
public abstract class AbstractUntagCommand
    extends AbstractCommand
{
    /**
     * execute untag command
     *
     * @param repository         scm repo
     * @param fileSet            set of files (unused)
     * @param scmUntagParameters parameters used by untag implementations
     * @return result of untag command
     * @throws ScmException  in case of error
     */
    protected abstract ScmResult executeUntagCommand( ScmProviderRepository repository,
        ScmFileSet fileSet, ScmUntagParameters scmUntagParameters ) throws ScmException;

    /** {@inheritDoc} */
    @Override
    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        String tagName = parameters.getString( CommandParameter.TAG_NAME );
        String message = parameters.getString( CommandParameter.MESSAGE, "[maven-scm] remove tag " + tagName );
        ScmUntagParameters scmUntagParameters = new ScmUntagParameters( tagName, message );

        return executeUntagCommand( repository, fileSet, scmUntagParameters );
    }

}
