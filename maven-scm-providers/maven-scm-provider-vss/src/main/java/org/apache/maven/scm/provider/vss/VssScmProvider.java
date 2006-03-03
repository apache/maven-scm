package org.apache.maven.scm.provider.vss;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.changelog.VssHistoryCommand;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:george@neogrid.com.br">George Gastaldi</a>
 * @version $Id$
 *          <p/>
 *          http://msdn.microsoft.com/library/default.asp?url=/library/en-us/guides/html/vstskuse_command_line_commands_and_options.asp
 */
public class VssScmProvider
    extends AbstractScmProvider
{

    public static final String VSS_URL_FORMAT = "[username[|password]@]vssdir|projectPath";

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public String getScmSpecificFilename()
    {
        return "vssver.scc";
    }

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        String user = null;
        String password = null;
        String vssDir;
        String project;

        int index = scmSpecificUrl.indexOf( '@' );

        String rest = scmSpecificUrl;

        if ( index != -1 )
        {
            String userAndPassword = scmSpecificUrl.substring( 0, index );

            rest = scmSpecificUrl.substring( index + 1 );

            index = userAndPassword.indexOf( delimiter );

            if ( index != -1 )
            {
                user = userAndPassword.substring( 0, index );

                password = userAndPassword.substring( index + 1 );
            }
            else
            {
                user = userAndPassword;
            }
        }
        String[] tokens = StringUtils.split( rest, String.valueOf( delimiter ) );

        if ( tokens.length < 2 )
        {
            throw new ScmRepositoryException( "Invalid SCM URL: The url has to be on the form: " + VSS_URL_FORMAT );
        }
        else
        {
            vssDir = tokens[0];

            project = tokens[1];
        }

        return new VssScmProviderRepository( user, password, vssDir, project );
    }

    public String getScmType()
    {
        return "vss";
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#add(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
/*
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        // TODO: Check whether the CREATE command must be called
        VssAddCommand command = new VssAddCommand();
        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository
            .getProviderRepository(), fileSet, parameters );
    }
*/

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkin(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
/*
    public CheckInScmResult checkin( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssCheckInCommand command = new VssCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository
            .getProviderRepository(), fileSet, parameters );
    }
*/

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkout(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
/*
    public CheckOutScmResult checkout( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssCheckOutCommand command = new VssCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository
            .getProviderRepository(), fileSet, parameters );
    }
*/

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssHistoryCommand command = new VssHistoryCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository
            .getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#tag(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
/*
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssLabelCommand command = new VssLabelCommand();

        command.setLogger( getLogger() );

        return (TagScmResult) command.execute( repository
            .getProviderRepository(), fileSet, parameters );
    }
*/

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#update(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
/*
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssGetCommand command = new VssGetCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository
            .getProviderRepository(), fileSet, parameters );
    }
*/

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#remove(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
/*
    protected RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssRemoveCommand command = new VssRemoveCommand();

        command.setLogger( getLogger() );

        return (RemoveScmResult) command.execute( repository
            .getProviderRepository(), fileSet, parameters );
    }
*/
}
