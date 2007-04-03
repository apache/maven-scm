package org.apache.maven.scm.provider.vss;

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
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.add.VssAddCommand;
import org.apache.maven.scm.provider.vss.commands.changelog.VssHistoryCommand;
import org.apache.maven.scm.provider.vss.commands.checkout.VssCheckOutCommand;
import org.apache.maven.scm.provider.vss.commands.edit.VssEditCommand;
import org.apache.maven.scm.provider.vss.commands.status.VssStatusCommand;
import org.apache.maven.scm.provider.vss.commands.update.VssUpdateCommand;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:george@neogrid.com.br">George Gastaldi</a>
 * @version $Id$
 * @link http://msdn.microsoft.com/library/default.asp?url=/library/en-us/guides/html/vstskuse_command_line_commands_and_options.asp
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="vss"
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
     * @see org.apache.maven.scm.provider.AbstractScmProvider#add(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        // TODO: Check whether the CREATE command must be called
        VssAddCommand command = new VssAddCommand();
        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkin(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    /*
     public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
     throws ScmException
     {
     VssCheckInCommand command = new VssCheckInCommand();

     command.setLogger( getLogger() );

     return (CheckInScmResult) command.execute( repository
     .getProviderRepository(), fileSet, parameters );
     }
     */

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkout(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        VssCheckOutCommand command = new VssCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        VssHistoryCommand command = new VssHistoryCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#tag(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    /*
     public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
     throws ScmException
     {
     VssLabelCommand command = new VssLabelCommand();

     command.setLogger( getLogger() );

     return (TagScmResult) command.execute( repository
     .getProviderRepository(), fileSet, parameters );
     }
     */

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#update(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssUpdateCommand command = new VssUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#status(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssStatusCommand command = new VssStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#edit(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    public EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssEditCommand command = new VssEditCommand();

        command.setLogger( getLogger() );

        return (EditScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#unedit(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    /*
    public UnEditScmResult unedit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        VssUnEditCommand command = new VssUnEditCommand();

        command.setLogger( getLogger() );

        return (UnEditScmResult) command.execute( repository, fileSet, parameters );
    }
    */

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#remove(org.apache.maven.scm.provider.ScmProviderRepository,
     *org.apache.maven.scm.ScmFileSet,
     *org.apache.maven.scm.CommandParameters)
     */
    /*
     protected RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
     throws ScmException
     {
     VssRemoveCommand command = new VssRemoveCommand();

     command.setLogger( getLogger() );

     return (RemoveScmResult) command.execute( repository
     .getProviderRepository(), fileSet, parameters );
     }
     */

}
