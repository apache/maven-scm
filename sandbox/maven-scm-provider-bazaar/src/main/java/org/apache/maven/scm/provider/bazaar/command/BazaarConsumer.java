package org.apache.maven.scm.provider.bazaar.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Base consumer to do common parsing for all bazaar commands.
 * <p>
 * More spesific: log line on debug, get file status
 * and detect warnings from bazaar
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarConsumer
    implements StreamConsumer
{

    /** A list of known keywords from bazaar */
    private static final Map identifiers = new HashMap();

    /** A list of known message prefixes from bazaar */
    private static final Map messages = new HashMap();

    static
    {
        identifiers.put( "added".intern(), ScmFileStatus.ADDED );
        identifiers.put( "unknown".intern(), ScmFileStatus.UNKNOWN );
        identifiers.put( "modified".intern(), ScmFileStatus.MODIFIED );
        identifiers.put( "removed".intern(), ScmFileStatus.DELETED );
        identifiers.put( "renamed".intern(), ScmFileStatus.MODIFIED );
        messages.put( "bzr: WARNING:", "WARNING" );
        messages.put( "bzr: ERROR:", "ERROR" );
    }

    /** Shared logger with all consumer implementations */
    protected final ScmLogger logger;

    public BazaarConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        //override this
    }

    public void consumeLine( String line )
    {
        logger.debug( line );
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
                String message = line.substring( prefix.length() );
                if ( messages.get( prefix ).equals( "WARNING" ) )
                {
                    logger.warn( message );
                }
                else
                {
                    logger.error( message );
                }
                return true;
            }
        }
        return false;
    }
}
