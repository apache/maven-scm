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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.Stream;

public class StreamsConsumer
    extends XppStreamConsumer
{

    private List<Stream> streams;

    public StreamsConsumer( ScmLogger logger, List<Stream> streams )
    {
        super( logger );
        this.streams = streams;
    }

    @Override
    protected void startTag( List<String> tagPath, Map<String, String> attributes )
    {
        String tagName = getTagName( tagPath );
        if ( "stream".equals( tagName ) )
        {
            String name = attributes.get( "name" );
            long streamId = Long.parseLong( attributes.get( "streamNumber" ) );
            String basis = attributes.get( "basis" );
            String basisStreamNumber = attributes.get( "basisStreamNumber" );
            long basisStreamId = basisStreamNumber == null ? 0 : Long.parseLong( basisStreamNumber );
            String depot = attributes.get( "depotName" );
            Date startTime = new Date( Long.parseLong( attributes.get( "startTime" ) ) * 1000 );
            String streamType = attributes.get( "type" );
            streams.add( new Stream( name, streamId, basis, basisStreamId, depot, startTime, streamType ) );
        }
    }
}
