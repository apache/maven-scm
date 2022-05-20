package org.apache.maven.scm.provider.hg.command;

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

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.util.AbstractConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base consumer to do common parsing for all hg commands.
 * <p>
 * More specific: log line each line if debug is enabled, get file status
 * and detect warnings from hg
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 *
 */
public class HgConsumer
    extends AbstractConsumer
{

    /**
     * A list of known keywords from hg
     */
    private static final Map<String, ScmFileStatus> IDENTIFIERS = new HashMap<>();

    /**
     * A list of known message prefixes from hg
     */
    private static final Map<String, String> MESSAGES = new HashMap<>();

    /**
     * Number of lines to keep from Std.Err
     * This size is set to ensure that we capture enough info
     * but still keeps a low memory footprint.
     */
    private static final int MAX_STDERR_SIZE = 10;

    /**
     * A list of the MAX_STDERR_SIZE last errors or warnings.
     */
    private final List<String> stderr = new ArrayList<>();

    static
    {
        /* Statuses from hg add
         */
        IDENTIFIERS.put( "adding", ScmFileStatus.ADDED );
        IDENTIFIERS.put( "unknown", ScmFileStatus.UNKNOWN );
        IDENTIFIERS.put( "modified", ScmFileStatus.MODIFIED );
        IDENTIFIERS.put( "removed", ScmFileStatus.DELETED );
        IDENTIFIERS.put( "renamed", ScmFileStatus.MODIFIED );

        /* Statuses from hg status;
         */
        IDENTIFIERS.put( "A", ScmFileStatus.ADDED );
        IDENTIFIERS.put( "?", ScmFileStatus.UNKNOWN );
        IDENTIFIERS.put( "M", ScmFileStatus.MODIFIED );
        IDENTIFIERS.put( "R", ScmFileStatus.DELETED );
        IDENTIFIERS.put( "C", ScmFileStatus.CHECKED_IN );
        IDENTIFIERS.put( "!", ScmFileStatus.MISSING );
        IDENTIFIERS.put( "I", ScmFileStatus.UNKNOWN ); // not precisely the same, but i think semantics work? - rwd

        MESSAGES.put( "hg: WARNING:", "WARNING" );
        MESSAGES.put( "hg: ERROR:", "ERROR" );
        MESSAGES.put( "'hg' ", "ERROR" ); // hg isn't found in windows path
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        //override this
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( line );
        }
        String trimmedLine = line.trim();

        String statusStr = processInputForKnownIdentifiers( trimmedLine );

        //If its not a status report - then maybe its a message?
        if ( statusStr == null )
        {
            boolean isMessage = processInputForKnownMessages( trimmedLine );
            //If it is then its already processed and we can ignore futher processing
            if ( isMessage )
            {
                return;
            }
        }
        else
        {
            //Strip away identifier
            trimmedLine = trimmedLine.substring( statusStr.length() );
            trimmedLine = trimmedLine.trim(); //one or more spaces
        }

        ScmFileStatus status = statusStr != null ? ( IDENTIFIERS.get( statusStr.intern() ) ) : null;
        doConsume( status, trimmedLine );
    }

    /**
     * Warnings and errors is usually printed out in Std.Err, thus for derived consumers
     * operating on Std.Out this would typically return an empty string.
     *
     * @return Return the last lines interpreted as an warning or an error
     */
    public String getStdErr()
    {
        StringBuilder str = new StringBuilder();
        for ( Iterator<String> it = stderr.iterator(); it.hasNext(); )
        {
            str.append( it.next() );
        }
        return str.toString();
    }

    private static String processInputForKnownIdentifiers( String line )
    {
        for ( Iterator<String> it = IDENTIFIERS.keySet().iterator(); it.hasNext(); )
        {
            String id = it.next();
            if ( line.startsWith( id ) )
            {
                return id;
            }
        }
        return null;
    }

    private boolean processInputForKnownMessages( String line )
    {
        for ( Iterator<String> it = MESSAGES.keySet().iterator(); it.hasNext(); )
        {
            String prefix = it.next();
            if ( line.startsWith( prefix ) )
            {
                stderr.add( line ); //Add line
                if ( stderr.size() > MAX_STDERR_SIZE )
                {
                    stderr.remove( 0 ); //Rotate list
                }
                String message = line.substring( prefix.length() );
                if ( MESSAGES.get( prefix ).equals( "WARNING" ) )
                {
                    if ( logger.isWarnEnabled() )
                    {
                        logger.warn( message );
                    }
                }
                else
                {
                    if ( logger.isErrorEnabled() )
                    {
                        logger.error( message );
                    }
                }
                return true;
            }
        }
        return false;
    }
}
