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

import org.apache.maven.scm.repository.ScmRepository;

import java.io.Serializable;

/**
 * Base class for SCM wrapped parameters.
 * Unlike {@link ScmResult}, this is mutable, as its use requires more flexibility when configuring the call.
 * <p>Most parameters should be stored in {@link #getCommandParameters() parameters} field, as it makes them easy to pass
 * down to the implementation side.</p>
 * <p>
 * Methods in descendant classes should perform all neccessary (un)marshalling so that user can work with nice
 * semantic typesafe properties.</p>
 *
 * @author Petr Kozelka
 * @since 1.8
 */
public class ScmRequest
    implements Serializable
{
    private static final long serialVersionUID = 20120620L;

    private ScmRepository scmRepository;

    private ScmFileSet scmFileSet;

    protected final CommandParameters parameters = new CommandParameters();

    public ScmRequest()
    {
        // no op
    }

    public ScmRequest( ScmRepository scmRepository, ScmFileSet scmFileSet )
    {
        this.scmRepository = scmRepository;
        this.scmFileSet = scmFileSet;
    }

    public ScmRepository getScmRepository()
    {
        return scmRepository;
    }

    /**
     * @param scmRepository the source control system
     */
    public void setScmRepository( ScmRepository scmRepository )
    {
        this.scmRepository = scmRepository;
    }

    public ScmFileSet getScmFileSet()
    {
        return scmFileSet;
    }

    /**
     * The files being processed. Implementations can also work with all files from the
     *      {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param scmFileSet working copy and its selected files
     */
    public void setScmFileSet( ScmFileSet scmFileSet )
    {
        this.scmFileSet = scmFileSet;
    }

    /**
     * Holds all parameter values passed to the implementing part.
     * These parameters are usually translated to commandline options or arguments.
     * @return command parameters
     */
    public CommandParameters getCommandParameters()
    {
        return parameters;
    }
}
