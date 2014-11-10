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

import java.util.List;
import java.util.Map;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.FileDifference;

/**
 * 
 */
public class DiffConsumer
    extends XppStreamConsumer
{

    private List<FileDifference> results;

    private FileDifference currentDifference;

    public DiffConsumer( ScmLogger logger, List<FileDifference> results )
    {
        super( logger );
        this.results = results;
    }

    @Override
    protected void startTag( List<String> tagPath, Map<String, String> attributes )
    {
        String tagName = getTagName( tagPath );
        if ( "Element".equals( tagName ) )
        {
            currentDifference = new FileDifference();
        }
        else if ( "Stream2".equals( tagName ) && attributes.get( "Name" ) != null )
        {
            currentDifference.setElementId( Long.parseLong( attributes.get( "eid" ) ) );
            currentDifference.setNewVersion( attributes.get( "Name" ), attributes.get( "Version" ) );
        }
        else if ( "Stream1".equals( tagName ) && attributes.get( "Name" ) != null )
        {
            currentDifference.setElementId( Long.parseLong( attributes.get( "eid" ) ) );
            currentDifference.setOldVersion( attributes.get( "Name" ), attributes.get( "Version" ) );
        }

    }

    @Override
    protected void endTag( List<String> tagPath )
    {
        String tagName = getTagName( tagPath );
        if ( "Element".equals( tagName ) )
        {
            if ( currentDifference.getNewFile() != null || currentDifference.getOldFile() != null )
            {
                results.add( currentDifference );
            }
        }
    }

}
