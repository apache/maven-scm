package org.apache.maven.scm.provider.dimensionscm.util;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.scm.provider.dimensionscm.repository.DimensionsScmProviderRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for constructing Dimensions CM commands.
 */
public class Command
{

    private String command;
    private DimensionsScmProviderRepository providerRepository;

    private Command( String command, DimensionsScmProviderRepository providerRepository )
    {
        this.command = command;
        this.providerRepository = providerRepository;
    }

    /**
     * Builds Dimensions CM commands.
     */
    public static class Builder
    {

        private static final String BLANK_SPACE = " ";
        private DimensionsScmProviderRepository providerRepository;
        private StringBuilder command;

        public Builder( String commandName, DimensionsScmProviderRepository providerRepository )
        {
            this.command = new StringBuilder( commandName );
            this.providerRepository = providerRepository;
        }

        public Builder addParameter( String parameterName )
        {
            if ( StringUtils.isBlank( parameterName ) )
            {
                return this;
            }
            command.append( BLANK_SPACE ).append( noValueParameter( parameterName ) );
            return this;
        }

        public Builder addParameter( String parameterName, String parameterValue )
        {
            if ( StringUtils.isBlank( parameterName ) || StringUtils.isBlank( parameterValue ) )
            {
                return this;
            }
            command.append( BLANK_SPACE ).append( valueParameter( parameterName, parameterValue ) );
            return this;
        }

        public Command build()
        {
            return new Command( command.toString(), providerRepository );
        }

        private String valueParameter( String name, String value )
        {
            if ( SystemUtils.IS_OS_WINDOWS )
            {
                // os windows need \\\" to escape quotes. It will be parsed to \"
                return String.format( "/%s=\\\"%s\\\"", name, value );
            }
            else
            {
                return String.format( "/%s=\"%s\"", name, value );
            }
        }

        private String noValueParameter( String name )
        {
            return String.format( "/%s", name );
        }
    }

    public String getCommand()
    {
        return command;
    }

    public List<String> getCommandWithLogin()
    {
        List<String> cmd = loginCmd();
        cmd.add( "-cmd" );
        cmd.add( command );

        return cmd;
    }

    private List<String> loginCmd()
    {

        List<String> loginCmd = new ArrayList<>();

        loginCmd.add( "dmcli" );
        loginCmd.add( "-user" );
        loginCmd.add( providerRepository.getDmUser() );
        loginCmd.add( "-pass" );
        loginCmd.add( providerRepository.getDmPassword() );
        loginCmd.add( "-dbname" );
        loginCmd.add( providerRepository.getDmDatabaseName() );
        loginCmd.add( "-host" );
        loginCmd.add( providerRepository.getDmServer() );
        loginCmd.add( "-dsn" );
        loginCmd.add( providerRepository.getDmDatabaseConnection() );

        return loginCmd;
    }

}

