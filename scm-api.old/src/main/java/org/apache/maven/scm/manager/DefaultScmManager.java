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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.Scm;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.checkout.CheckOutCommand;
import org.apache.maven.scm.command.update.UpdateCommand;
import org.apache.maven.scm.repository.RepositoryInfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class DefaultScmManager
    implements ScmManager
{
    private Map scmFactories = new HashMap();

    private RepositoryInfo repoInfo;
    
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
        throws Exception
    {
        try
        {
            Command command = getCommand( CheckOutCommand.NAME );

            command.setWorkingDirectory( directory );

            command.execute();
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            throw new Exception( "Cannot checkout sources: ", e );
        }
    }

    public void update( String directory )
        throws Exception
    {
        try
        {
            Command command = getCommand( UpdateCommand.NAME );

            command.setWorkingDirectory( directory );

            command.execute();
        }
        catch ( Exception e )
        {
            throw new Exception( "Cannot checkout sources: ", e );
        }
    }

    public Command getCommand( String commandName )
        throws ScmException
    {
        Scm scmFactory = (Scm)scmFactories.get( repoInfo.getType() );

        if ( scmFactory == null)
        {
            throw new ScmException("There is no providers corresponding to scm type (" +
                repoInfo.getType() + ")");
        }
        
        return scmFactory.createCommand( repoInfo, commandName );
    }
}