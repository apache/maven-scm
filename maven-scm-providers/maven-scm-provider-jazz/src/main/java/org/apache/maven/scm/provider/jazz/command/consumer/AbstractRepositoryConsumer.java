package org.apache.maven.scm.provider.jazz.command.consumer;

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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.util.AbstractConsumer;

/**
 * An extension of the AbstractConsumer class that also holds our Repository.
 */
public abstract class AbstractRepositoryConsumer
    extends AbstractConsumer
{
    // The repository that we are working with.
    private ScmProviderRepository repository = null;
    
    // Have we processed input?
    protected boolean fed = false;

    /**
     * AbstractRepositoryConsumer constructor.
     *
     * @param logger The logger to use in the consumer
     */
    public AbstractRepositoryConsumer( ScmProviderRepository repository, ScmLogger logger )
    {
        super( logger );
        setRepository( repository );
    }

    /**
     * @return The repository.
     */
    public ScmProviderRepository getRepository()
    {
        return repository;
    }

    /**
     * @param repository The repository to set.
     */
    public void setRepository( ScmProviderRepository repository )
    {
        this.repository = repository;
    }

    /**
     * @return The fed.
     */
    public boolean isFed()
    {
        return fed;
    }

    /**
     * @param fed The fed to set.
     */
    public void setFed( boolean fed )
    {
        this.fed = fed;
    }

    /**
     * Process one line of output from the execution of the "scm xxxx" command.
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        getLogger().debug( "Consumed line :" + line );
        this.fed = true;
    }
}
