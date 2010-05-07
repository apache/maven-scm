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

import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class InfoConsumer
    implements StreamConsumer
{

    private AccuRevInfo info;

    public InfoConsumer( AccuRevInfo info )
    {
        this.info = info;
    }

    public void consumeLine( String line )
    {
        String[] tokens = line.split( "\\s*:\\s*", 2 );
        if ( tokens[0].equals( "Principal" ) )
        {
            info.setUser( tokens[1] );
        }
        else if ( tokens[0].equals( "Basis" ) )
        {
            info.setBasis( tokens[1] );
        }
        else if ( tokens[0].startsWith( "Workspace" ) )
        {
            info.setWorkSpace( tokens[1] );
        }
        else if ( tokens[0].equals( "Top" ) )
        {
            info.setTop( tokens[1] );
        }

        else if ( tokens[0].equals( "Server name" ) )
        {
            info.setServer( tokens[1] );
        }
        else if ( tokens[0].equals( "Port" ) )
        {
            info.setPort( Integer.parseInt( tokens[1] ) );
        }

    }

}
