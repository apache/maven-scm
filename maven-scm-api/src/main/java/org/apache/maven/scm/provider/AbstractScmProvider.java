package org.apache.maven.scm.provider;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.NoSuchCommandScmException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.Command;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractScmProvider
    extends AbstractLogEnabled
    implements ScmProvider, Initializable
{
    private Map cmds;

    protected abstract Map getCommands();

    // ----------------------------------------------------------------------
    // Component Implementation
    // ----------------------------------------------------------------------

    public final void initialize()
    {
        cmds = getCommands();

        if ( cmds == null )
        {
            cmds = Collections.EMPTY_MAP;
        }

        if ( cmds.size() == 0 )
        {
            getLogger().warn( "No SCM commands defined for SCM type " + getScmType() );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Registered " + getScmType() + " SCM:" );

            for ( Iterator it = cmds.keySet().iterator(); it.hasNext(); )
            {
                String name = (String) it.next();

                getLogger().debug( "  " + name );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Scm Implementation
    // ----------------------------------------------------------------------

    public ScmResult execute( String commandName, ScmProviderRepository repository, File workingDirectory, CommandParameters parameters )
        throws ScmException
    {
        Command command = getCommand( commandName );

        return command.execute( repository, workingDirectory, parameters );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected Command getCommand( String name)
        throws ScmException
    {
        Command command = (Command) cmds.get( name );

        if ( command == null )
        {
            throw new NoSuchCommandScmException( name );
        }

        return command;
    }
}
