package org.apache.maven.scm.command;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCommand
    extends AbstractLogEnabled
    implements Command
{
    protected abstract ScmResult executeCommand( ScmProviderRepository repository, File workingDirectory, CommandParameters parameters )
        throws ScmException;

    public final ScmResult execute( ScmProviderRepository repository, File workingDirectory, CommandParameters parameters )
        throws ScmException
    {
        if ( repository == null )
        {
            throw new NullPointerException( "repository cannot be null" );
        }

        if ( workingDirectory == null )
        {
            throw new NullPointerException( "workingDirectory cannot be null" );
        }

        try
        {
            return executeCommand( repository, workingDirectory, parameters );
        }
        catch( Exception ex )
        {
            throw new ScmException( "Exception while executing SCM command.", ex );
        }
    }
}
