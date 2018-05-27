package org.apache.maven.scm.provider.perforce.command.status;

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

import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Perham
 */
public class PerforceStatusConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{
    static final int STATE_FILES = 1;

    static final int STATE_ERROR = 2;

    private int currentState = STATE_FILES;

    private List<String> depotfiles = new ArrayList<String>();

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( line.indexOf( "not opened" ) != -1 )
        {
            // User has no files open at all, just return
            return;
        }
        switch ( currentState )
        {
            /*
//depot/sandbox/mperham/scm-test/Foo.java#1 - add default change (text)
//depot/sandbox/mperham/scm-test/bar/Bar.xml#1 - add default change (text)
             */
            case STATE_FILES:
                if ( line.startsWith( "//" ) )
                {
                    depotfiles.add( line.trim() );
                }
                break;
            default:
                error( line );
                break;
        }
    }

    private void error( String line )
    {
        currentState = STATE_ERROR;
        output.println( line );
    }

    public boolean isSuccess()
    {
        return currentState != STATE_ERROR;
    }

    public List<String> getDepotfiles()
    {
        return depotfiles;
    }
}
