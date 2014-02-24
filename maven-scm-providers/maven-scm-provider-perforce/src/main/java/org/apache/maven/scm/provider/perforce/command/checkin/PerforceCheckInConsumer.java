package org.apache.maven.scm.provider.perforce.command.checkin;

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

import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogConsumer.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
 */
public class PerforceCheckInConsumer
    implements StreamConsumer
{
    private static final Pattern CREATED_PATTERN = Pattern.compile( "^Change \\d+ created .+$" );

    private static final Pattern SUBMITTING_PATTERN = Pattern.compile( "^Submitting change \\d+\\.$" );

    private static final Pattern LOCKING_PATTERN = Pattern.compile( "^Locking \\d+ files \\.\\.\\.$" );

    private static final Pattern OPERATION_PATTERN = Pattern.compile( "^[a-z]+ //[^#]+#\\d+$" );

    // SCM-181 Two possible messages:
    // "Change 94821 renamed change 94823 and submitted."
    // "Change 94821 submitted."
    private static final Pattern COMPLETE_PATTERN = Pattern.compile( "^Change \\d+ .*submitted.$" );

    public static final int STATE_CREATED = 1;

    public static final int STATE_SUBMITTING = 2;

    public static final int STATE_LOCKING = 3;

    public static final int STATE_OP = 4;

    public static final int STATE_COMPLETE = 5;

    public static final int STATE_ERROR = 6;

    private StringWriter errors = new StringWriter();

    private PrintWriter errorOutput = new PrintWriter( errors );

    private int currentState = STATE_CREATED;

    /*
     * Change 80835 created with 1 open file(s). Submitting change 80835.
     * Locking 1 files ... add //depot/modules/cordoba/runtime-ear/foo.xml#1
     * Change 80835 submitted.
     */
    /*
     * Submitting change 80837. Locking 1 files ... edit
     * //depot/modules/cordoba/runtime-ear/Foo.java#2 Submit validation failed --
     * fix problems then use 'p4 submit -c 80837'. 'checkstyle' validation
     * failed:
     *
     * depot/modules/cordoba/runtime-ear/Foo.java:3:1: Got an exception -
     * expecting EOF, found '}'
     */
    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( line.startsWith( "... " ) )
        {
            //TODO log this somehow?
            //System.out.println("Perforce: " + line);
            return;
        }

        switch ( currentState )
        {
            case STATE_CREATED:
                boolean created = CREATED_PATTERN.matcher( line ).matches();
                if ( created )
                {
                    currentState++;
                    break;
                }
                error( line );
                break;
            case STATE_SUBMITTING:
                boolean submitting = SUBMITTING_PATTERN.matcher( line ).matches();
                if ( submitting )
                {
                    currentState++;
                    break;
                }
                error( line );
                break;
            case STATE_LOCKING:
                boolean locked = LOCKING_PATTERN.matcher( line ).matches();
                if ( locked )
                {
                    currentState++;
                    break;
                }
                error( line );
                break;
            case STATE_OP:
                boolean operation = OPERATION_PATTERN.matcher( line ).matches();
                if ( operation )
                {
                    break;
                }
                else if ( COMPLETE_PATTERN.matcher( line ).matches() )
                {
                    currentState++;
                    break;
                }
                error( line );
                break;
            case STATE_ERROR:
                error( line );
                break;
            default:
        }
    }

    private void error( String line )
    {
        //        if (currentState != STATE_ERROR) {
        //            System.out.println("Unable to match: " + line + " State: " +
        // currentState);
        //            new Exception().printStackTrace();
        //        }
        currentState = STATE_ERROR;
        errorOutput.println( line );
    }

    public boolean isSuccess()
    {
        return currentState == STATE_COMPLETE;
    }

    public String getOutput()
    {
        errorOutput.flush();
        errors.flush();
        return errors.toString();
    }
}
