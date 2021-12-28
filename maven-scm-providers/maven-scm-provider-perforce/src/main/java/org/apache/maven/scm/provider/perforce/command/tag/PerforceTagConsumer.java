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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Mike Perham
 * @author Olivier Lamy
 *
 */
public class PerforceTagConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{

    private static final Pattern LABEL_PATTERN = Pattern.compile( "^Label ([^ ]+) saved.$" );

    private static final Pattern SYNC_PATTERN = Pattern.compile( "^([^#]+)#\\d+ - (.*)" );

    public static final int STATE_CREATE = 1;

    public static final int STATE_SYNC = 2;

    public static final int STATE_ERROR = 3;

    private int currentState = STATE_CREATE;

    private List<ScmFile> tagged = new ArrayList<ScmFile>();

    /**
     * Return a list of Strings formatted like:
     * <p>
     * <pre>
     * //depot/modules/cordoba/runtime-ear/pom.xml
     * //depot/modules/cordoba/runtime-ear/.runtime
     * </pre>
     */
    public List<ScmFile> getTagged()
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
    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        switch ( currentState )
        {
            case STATE_CREATE:
                if ( !LABEL_PATTERN.matcher( line ).matches() )
                {
                    error( line );
                    break;
                }
                currentState = STATE_SYNC;
                break;
            case STATE_SYNC:
                Matcher matcher = SYNC_PATTERN.matcher( line );
                if ( !matcher.matches() )
                {
                    error( line );
                    break;
                }
                tagged.add( new ScmFile( matcher.group( 1 ), ScmFileStatus.TAGGED ) );
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
