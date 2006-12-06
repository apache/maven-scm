package org.apache.maven.scm.provider.perforce.command.login;

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
import org.apache.regexp.RE;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogConsumer.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
 */
public class PerforceLoginConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{

    private static final String LOGIN_PATTERN = "^User [^ ]+ logged in.$";

    public static final int STATE_LOGIN = 1;

    public static final int STATE_ERROR = 2;

    private int currentState = STATE_LOGIN;

    public void consumeLine( String line )
    {
        if ( line.startsWith( "Enter password:" ) )
        {
            return;
        }
        if ( currentState != STATE_ERROR && new RE( LOGIN_PATTERN ).match( line ) )
        {
            return;
        }

        error( line );
    }

    private void error( String line )
    {
        currentState = STATE_ERROR;
        output.println( line );
    }

    public boolean isSuccess()
    {
        return currentState == STATE_LOGIN;
    }
}
