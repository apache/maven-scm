package org.apache.maven.scm.provider.perforce.command.tag;

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
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogConsumer.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
 */
public class PerforceTagConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{

    private static final String LABEL_PATTERN = "^Label ([^ ]+) saved.$";

    private static final String SYNC_PATTERN = "^([^#]+)#\\d+ - (.*)";

    public static final int STATE_CREATE = 1;

    public static final int STATE_SYNC = 2;

    public static final int STATE_ERROR = 3;

    private int currentState = STATE_CREATE;

    private List tagged = new ArrayList();

    private RE syncRegexp;

    public PerforceTagConsumer()
    {
        try
        {
            syncRegexp = new RE( SYNC_PATTERN );
        }
        catch ( RESyntaxException ignored )
        {
            ignored.printStackTrace();
        }
    }

    /**
     * Return a list of Strings formatted like:
     * <p/>
     * <pre>
     * //depot/modules/cordoba/runtime-ear/pom.xml
     * //depot/modules/cordoba/runtime-ear/.runtime
     * </pre>
     */
    public List getTagged()
    {
        return tagged;
    }

    /*
     * We consume the output from 'p4 label -i' and 'p4 labelsync -l <tag>
     * <files...>'
     */
    /*
     * Label maven-scm-test saved.
     */
    /*
     * //depot/modules/cordoba/runtime-ear/pom.xml#4 - added
     * //depot/modules/cordoba/runtime-ear/.runtime#1 - added
     */
    public void consumeLine( String line )
    {
        switch ( currentState )
        {
            case STATE_CREATE:
                if ( !new RE( LABEL_PATTERN ).match( line ) )
                {
                    error( line );
                    break;
                }
                currentState = STATE_SYNC;
                break;
            case STATE_SYNC:
                if ( !syncRegexp.match( line ) )
                {
                    error( line );
                    break;
                }
                tagged.add( syncRegexp.getParen( 1 ) );
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
        return currentState == STATE_SYNC;
    }

}
