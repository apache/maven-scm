package org.apache.maven.scm.provider.hg.command;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base consumer to do common parsing for all hg commands.
 * <p/>
 * More specific: log line each line if debug is enabled, get file status
 * and detect warnings from hg
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgConsumer
    extends AbstractConsumer
{

    /**
     * A list of known keywords from hg
     */
    private static final Map identifiers = new HashMap();

    /**
     * A list of known message prefixes from hg
     */
    private static final Map messages = new HashMap();

    /**
     * Number of lines to keep from Std.Err
     * This size is set to ensure that we capture enough info
     * but still keeps a low memory footprint.
     */
    private static final int MAX_STDERR_SIZE = 10;

    /**
     * A list of the MAX_STDERR_SIZE last errors or warnings.
     */
    private final List stderr = new ArrayList();

    static
    {
        /** Statuses from hg add
         */
        identifiers.put( "adding", ScmFileStatus.ADDED );
        identifiers.put( "unknown", ScmFileStatus.UNKNOWN );
        identifiers.put( "modified", ScmFileStatus.MODIFIED );
        identifiers.put( "removed", ScmFileStatus.DELETED );
        identifiers.put( "renamed", ScmFileStatus.MODIFIED );

        /** Statuses from hg status;
         *  TODO "! deleted but still tracked"
         *  d 
         */
        identifiers.put( "A", ScmFileStatus.ADDED);
        identifiers.put( "?", ScmFileStatus.UNKNOWN);
        identifiers.put( "M", ScmFileStatus.MODIFIED);
        identifiers.put( "R", ScmFileStatus.DELETED);
        identifiers.put( "I", ScmFileStatus.UNKNOWN); // not precisely the same, but i think semantics work? - rwd

        messages.put( "hg: WARNING:", "WARNING" );
        messages.put( "hg: ERROR:", "ERROR" );
        messages.put( "'hg' ", "ERROR" ); // hg isn't found in windows path
    }

    public HgConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        //override this
    }

    public void consumeLine( String line )
    {
        getLogger().debug( line );
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

        ScmFileStatus status = statusStr != null ? ( (ScmFileStatus) identifiers.get( statusStr.intern() ) ) : null;
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
        String str = "";
        for ( Iterator it = stderr.iterator(); it.hasNext(); )
        {
            str += it.next();
        }
        return str;
    }

    private static String processInputForKnownIdentifiers( String line )
    {
        for ( Iterator it = identifiers.keySet().iterator(); it.hasNext(); )
        {
            String id = (String) it.next();
            if ( line.startsWith( id ) )
            {
                return id;
            }
        }
        return null;
    }

    private boolean processInputForKnownMessages( String line )
    {
        for ( Iterator it = messages.keySet().iterator(); it.hasNext(); )
        {
            String prefix = (String) it.next();
            if ( line.startsWith( prefix ) )
            {
                stderr.add( line ); //Add line
                if ( stderr.size() > MAX_STDERR_SIZE )
                {
                    stderr.remove( 0 ); //Rotate list
                }
                String message = line.substring( prefix.length() );
                if ( messages.get( prefix ).equals( "WARNING" ) )
                {
                    getLogger().warn( message );
                }
                else
                {
                    getLogger().error( message );
                }
                return true;
            }
        }
        return false;
    }
}
