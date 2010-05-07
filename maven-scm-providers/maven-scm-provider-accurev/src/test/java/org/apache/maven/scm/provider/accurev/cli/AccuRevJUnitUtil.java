package org.apache.maven.scm.provider.accurev.cli;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.manager.plexus.PlexusLogger;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class AccuRevJUnitUtil
{

    public static void consume( String resource, StreamConsumer consumer )
        throws IOException
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( consumer.getClass()
            .getResourceAsStream( resource ) ) );

        String line = reader.readLine();
        while ( line != null )
        {
            consumer.consumeLine( line );
            line = reader.readLine();
        }

        if ( consumer instanceof XppStreamConsumer )
        {
            ( (XppStreamConsumer) consumer ).waitComplete();
        }
    }

    public static ScmLogger getLogger( PlexusContainer plexusContainer )
        throws ComponentLookupException
    {
        LoggerManager loggerManager = (LoggerManager) plexusContainer.lookup( LoggerManager.ROLE );
        Logger logger = loggerManager.getLoggerForComponent( ScmManager.ROLE );
        return new PlexusLogger( logger );
    }

    public static InputStream getPlexusConfiguration()
    {
        return AccuRevJUnitUtil.class.getResourceAsStream( "/PlexusTestContainerConfig.xml" );
    }

}
