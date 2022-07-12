package org.apache.maven.scm.provider.git.jgit.command;

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


import java.util.Arrays;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * {@link CredentialsProvider} leveraging the {@link Prompter} component.
 *
 */
public class PlexusInteractivityCredentialsProvider extends CredentialsProvider
{
    private boolean interactive;
    private final Prompter prompter;

    public PlexusInteractivityCredentialsProvider( Prompter prompter )
    {
        this.interactive = true;
        this.prompter = prompter;
    }

    @Override
    public boolean get( URIish uri, CredentialItem... items )
    {
        for ( CredentialItem item : items )
        {
            try
            {
                get( uri, item );
            }
            catch ( PrompterException e )
            {
                throw new IllegalStateException( "Cannot prompt user", e );
            }
        }
        return true;
    }

    private void get( URIish uri, CredentialItem item ) throws PrompterException
    {
        if ( item instanceof CredentialItem.InformationalMessage )
        {
            // works even in non-interactive mode
            prompter.showMessage( item.getPromptText() );
        }
        else
        {
            if ( !interactive )
            {
                throw new UnsupportedCredentialItem( uri,
                        "Cannot provide '" + item.getClass() + "' in non-interactive mode" );
            }
            if ( item instanceof CredentialItem.YesNoType )
            {
                CredentialItem.YesNoType yesNoItem = ( CredentialItem.YesNoType ) item;
                String value = prompter.prompt( item.getPromptText(), Arrays.asList( "yes", "no" ) );
                yesNoItem.setValue( value.equals( "yes" ) );
            }
            else if ( item instanceof CredentialItem.Password )
            {
                CredentialItem.Password passwordItem = ( CredentialItem.Password ) item;
                String password = prompter.promptForPassword( passwordItem.getPromptText() );
                passwordItem.setValue( password.toCharArray() );
            }
            else if ( item instanceof CredentialItem.Username )
            {
                CredentialItem.Username usernameItem = ( CredentialItem.Username ) item;
                String username = prompter.prompt( usernameItem.getPromptText() );
                usernameItem.setValue( username );
            }
            else if ( item instanceof CredentialItem.StringType )
            {
                CredentialItem.StringType stringItem = ( CredentialItem.StringType ) item;
                String value = prompter.prompt( stringItem.getPromptText() );
                stringItem.setValue( value );
            }
            else if ( item instanceof CredentialItem.CharArrayType )
            {
                CredentialItem.CharArrayType charArrayItem = ( CredentialItem.CharArrayType ) item;
                String value = prompter.prompt( charArrayItem.getPromptText() );
                charArrayItem.setValue( value.toCharArray() );
            }
            else
            {
                throw new UnsupportedCredentialItem( uri, "This provider does not support items of type '"
                                                     + item.getClass() + "'" );
            }
        }
    }

    public void setInteractive( boolean interactive )
    {
        this.interactive = interactive;
    }

    @Override
    public boolean isInteractive()
    {
        return interactive;
    }

    @Override
    public boolean supports( CredentialItem... items )
    {
        return true;
    }

}
