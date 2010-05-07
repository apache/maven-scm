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

import java.util.List;
import java.util.Map;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.WorkSpace;

public class WorkSpaceConsumer
    extends XppStreamConsumer
{

    private Map<String, WorkSpace> workSpaces;

    public WorkSpaceConsumer( ScmLogger logger, Map<String, WorkSpace> workSpaces )
    {
        super( logger );
        this.workSpaces = workSpaces;
    }

    @Override
    protected void startTag( List<String> tagPath, Map<String, String> attributes )
    {
        if ( "Element".equals( getTagName( tagPath ) ) )
        {
            String name = attributes.get( "Name" );
            long transactionId = Long.valueOf( attributes.get( "Trans" ) );
            WorkSpace ws = new WorkSpace( name, transactionId );
            workSpaces.put( name, ws );
        }
    }

}
