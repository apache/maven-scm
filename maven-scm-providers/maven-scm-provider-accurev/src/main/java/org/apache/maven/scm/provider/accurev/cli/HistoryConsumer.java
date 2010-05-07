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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.Transaction;

public class HistoryConsumer
    extends XppStreamConsumer
{

    private List<Transaction> transactions;

    private Transaction currentTran;

    private Long elementId;

    private String elementName;

    public HistoryConsumer( ScmLogger logger, List<Transaction> transactions )
    {
        super( logger );
        this.transactions = transactions;
    }

    @Override
    protected void startTag( List<String> tagPath, Map<String, String> attributes )
    {
        String tagName = getTagName( tagPath );
        if ( "transaction".equals( tagName ) )
        {
            Long id = Long.parseLong( attributes.get( "id" ) );
            Date when = new Date( Long.parseLong( attributes.get( "time" ) ) * 1000 );
            String tranType = attributes.get( "type" );
            String user = attributes.get( "user" );
            currentTran = new Transaction( id, when, tranType, user );
            transactions.add( currentTran );

        }
        else if ( "version".equals( tagName ) )
        {
            if ( currentTran != null )
            {

                if ( attributes.containsKey( "eid" ) )
                {
                    elementId = Long.parseLong( attributes.get( "eid" ) );
                    elementName = attributes.get( "path" );
                }

                String virtualSpec = attributes.get( "virtual" );
                String realSpec = attributes.get( "real" );
                String ancestor = attributes.get( "ancestor" );

                currentTran.addVersion( elementId, elementName, virtualSpec, realSpec, ancestor );
            }
        }
        else if ( "element".equals( tagName ) )
        {
            elementId = Long.parseLong( attributes.get( "eid" ) );
            elementName = attributes.get( "name" );
        }
    }

    @Override
    protected void endTag( List<String> tagPath )
    {
        String tagName = getTagName( tagPath );
        if ( "element".equals( tagName ) )
        {
            elementId = null;
            elementName = null;
        }
        else if ( "transaction".equals( tagName ) )
        {
            currentTran = null;
        }

    }

    @Override
    protected void text( List<String> tagPath, String text )
    {
        String tagName = getTagName( tagPath );
        if ( currentTran != null && "comment".equals( tagName ) )
        {
            currentTran.setComment( text );
        }

    }

}
