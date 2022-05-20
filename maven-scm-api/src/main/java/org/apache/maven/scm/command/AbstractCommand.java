package org.apache.maven.scm.command;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.log.Slf4jScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public abstract class AbstractCommand
    implements Command
{
    private ScmLogger logger = new Slf4jScmLogger( getClass() );

    protected abstract ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                 CommandParameters parameters )
        throws ScmException;

    /** {@inheritDoc} */
    public final ScmResult execute( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        if ( repository == null )
        {
            throw new NullPointerException( "repository cannot be null" );
        }

        if ( fileSet == null )
        {
            throw new NullPointerException( "fileSet cannot be null" );
        }

        try
        {
            return executeCommand( repository, fileSet, parameters );
        }
        catch ( Exception ex )
        {
            throw new ScmException( "Exception while executing SCM command.", ex );
        }
    }

    /** {@inheritDoc} */
    public final ScmLogger getLogger()
    {
        return logger;
    }

    /** {@inheritDoc} */
    public final void setLogger( ScmLogger logger )
    {
        this.logger = logger;
    }
}
