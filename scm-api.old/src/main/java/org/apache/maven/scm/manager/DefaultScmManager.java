package org.apache.maven.scm.manager;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.Scm;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.checkout.CheckOutCommand;
import org.apache.maven.scm.command.update.UpdateCommand;
import org.apache.maven.scm.repository.RepositoryInfo;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

public class DefaultScmManager
    extends AbstractLogEnabled
    implements ScmManager, Initializable
{
    private Map scmFactories;

    private RepositoryInfo repoInfo;

    public void initialize()
        throws Exception
    {
        if ( scmFactories == null )
        {
            getLogger().warn( "No SCM factories configured." );

            scmFactories = new HashMap();
        }
    }

    public void setRepositoryInfo( String scmUrl )
        throws ScmException
    {
        setRepositoryInfo( new RepositoryInfo( scmUrl ) );
    }

    public void setRepositoryInfo( RepositoryInfo repoInfo )
        throws ScmException
    {
        this.repoInfo = repoInfo;
    }

    public void checkout( String directory )
        throws ScmException
    {
        Command command = getCommand( CheckOutCommand.NAME );

        command.setWorkingDirectory( directory );

        try
        {
            command.execute();
        }
        catch ( Exception e )
        {
            throw new ScmException( "Cannot checkout sources.", e );
        }
    }

    public void update( String directory )
        throws ScmException
    {
        Command command = getCommand( UpdateCommand.NAME );

        command.setWorkingDirectory( directory );

        try
        {
            command.execute();
        }
        catch ( Exception e )
        {
            throw new ScmException( "Cannot update sources.", e );
        }
    }

    public Command getCommand( String commandName )
        throws ScmException
    {
        if ( repoInfo == null )
        {
            throw new ScmException( "The repository must be set." );
        }

        Scm scmFactory = (Scm)scmFactories.get( repoInfo.getType() );

        if ( scmFactory == null )
        {
            throw new ScmException("There is no providers corresponding to scm type (" + repoInfo.getType() + ")");
        }
        
        return scmFactory.createCommand( repoInfo, commandName );
    }
}