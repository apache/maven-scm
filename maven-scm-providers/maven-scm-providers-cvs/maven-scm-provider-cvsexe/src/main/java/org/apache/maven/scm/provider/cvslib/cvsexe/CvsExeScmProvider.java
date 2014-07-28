package org.apache.maven.scm.provider.cvslib.cvsexe;

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

import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmProvider;
import org.apache.maven.scm.provider.cvslib.command.login.CvsLoginCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.add.CvsExeAddCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.blame.CvsExeBlameCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.branch.CvsExeBranchCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.changelog.CvsExeChangeLogCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.checkin.CvsExeCheckInCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.checkout.CvsExeCheckOutCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.diff.CvsExeDiffCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.export.CvsExeExportCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.list.CvsExeListCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.mkdir.CvsExeMkdirCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.remove.CvsExeRemoveCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.status.CvsExeStatusCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.tag.CvsExeTagCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.update.CvsExeUpdateCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="cvs_native"
 */
public class CvsExeScmProvider
    extends AbstractCvsScmProvider
{
    /** sserver transport method */
    public static final String TRANSPORT_SSERVER = "sserver";

    /** {@inheritDoc} */
    protected Command getAddCommand()
    {
        return new CvsExeAddCommand();
    }

    /** {@inheritDoc} */
    protected Command getBranchCommand()
    {
        return new CvsExeBranchCommand();
    }

    /** {@inheritDoc} */
    protected Command getBlameCommand()
    {
        return new CvsExeBlameCommand();
    }

    /** {@inheritDoc} */
    protected Command getChangeLogCommand()
    {
        return new CvsExeChangeLogCommand();
    }

    /** {@inheritDoc} */
    protected Command getCheckInCommand()
    {
        return new CvsExeCheckInCommand();
    }

    /** {@inheritDoc} */
    protected Command getCheckOutCommand()
    {
        return new CvsExeCheckOutCommand();
    }

    /** {@inheritDoc} */
    protected Command getDiffCommand()
    {
        return new CvsExeDiffCommand();
    }

    /** {@inheritDoc} */
    protected Command getExportCommand()
    {
        return new CvsExeExportCommand();
    }

    /** {@inheritDoc} */
    protected Command getListCommand()
    {
        return new CvsExeListCommand();
    }

    /** {@inheritDoc} */
    protected Command getLoginCommand()
    {
        return new CvsLoginCommand();
    }

    /** {@inheritDoc} */
    protected Command getRemoveCommand()
    {
        return new CvsExeRemoveCommand();
    }

    /** {@inheritDoc} */
    protected Command getStatusCommand()
    {
        return new CvsExeStatusCommand();
    }

    /** {@inheritDoc} */
    protected Command getTagCommand()
    {
        return new CvsExeTagCommand();
    }

    /** {@inheritDoc} */
    protected Command getUpdateCommand()
    {
        return new CvsExeUpdateCommand();
    }
    
    /** {@inheritDoc} */
    protected Command getMkdirCommand()
    {
        return new CvsExeMkdirCommand();
    }    

    /** {@inheritDoc} */
    protected ScmUrlParserResult parseScmUrl( String scmSpecificUrl, char delimiter )
    {
        ScmUrlParserResult result = super.parseScmUrl( scmSpecificUrl, delimiter );
        if ( result.getMessages().isEmpty() )
        {
            return result;
        }

        result.resetMessages();

        String[] tokens = StringUtils.split( scmSpecificUrl, Character.toString( delimiter ) );

        String cvsroot;

        String transport = tokens[0];

        // support sserver
        if ( transport.equalsIgnoreCase( TRANSPORT_SSERVER ) )
        {
            if ( tokens.length < 4 || tokens.length > 5 && transport.equalsIgnoreCase( TRANSPORT_SSERVER ) )
            {
                result.getMessages().add( "The connection string contains too few tokens." );

                return result;
            }

            //create the cvsroot as the remote cvsroot
            if ( tokens.length == 4 )
            {
                cvsroot = ":" + transport + ":" + tokens[1] + ":" + tokens[2];
            }
            else
            {
                cvsroot = ":" + transport + ":" + tokens[1] + ":" + tokens[2] + ":" + tokens[3];
            }
        }
        else
        {
            result.getMessages().add( "Unknown transport: " + transport );

            return result;
        }

        String user = null;

        String password = null;

        String host = null;

        String path = null;

        String module = null;

        int port = -1;

        if ( transport.equalsIgnoreCase( TRANSPORT_SSERVER ) )
        {
            //sspi:[username@]host:[port]path:module
            String userhost = tokens[1];

            int index = userhost.indexOf( '@' );

            if ( index == -1 )
            {
                user = "";

                host = userhost;
            }
            else
            {
                user = userhost.substring( 0, index );

                host = userhost.substring( index + 1 );
            }

            // no port specified
            if ( tokens.length == 4 )
            {
                path = tokens[2];
                module = tokens[3];
            }
            else
            {
                // getting port
                try
                {
                    port = Integer.valueOf( tokens[2] ).intValue();
                    path = tokens[3];
                    module = tokens[4];
                }
                catch ( Exception e )
                {
                    //incorrect
                    result.getMessages().add( "Your scm url is invalid, could not get port value." );

                    return result;
                }
            }

            // cvsroot format is :sspi:host:path
            cvsroot = ":" + transport + ":" + host + ":";

            if ( port != -1 )
            {
                cvsroot += port;
            }

            cvsroot += path;
        }

        if ( port == -1 )
        {
            result.setRepository( new CvsScmProviderRepository( cvsroot, transport, user, password, host, path,
                                                                module ) );
        }
        else
        {
            result.setRepository( new CvsScmProviderRepository( cvsroot, transport, user, password, host, port,
                                                                path, module ) );
        }

        return result;
    }
}
