package org.apache.maven.scm.provider.perforce.command;

import org.apache.maven.scm.ScmFileStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mperham
 * @version $Id$
 */
public class PerforceVerbMapper
{
    private static final Map map = new HashMap();

    static
    {
        // Perforce uses different tenses sometimes so we need to map
        // the different tenses to the same status.
        map.put( "add", ScmFileStatus.ADDED );
        map.put( "added", ScmFileStatus.ADDED );
        map.put( "delete", ScmFileStatus.DELETED );
        map.put( "deleted", ScmFileStatus.DELETED );
        map.put( "edit", ScmFileStatus.MODIFIED );
        map.put( "edited", ScmFileStatus.MODIFIED );
        map.put( "updating", ScmFileStatus.UPDATED );
        map.put( "updated", ScmFileStatus.UPDATED );
        // UNKNOWN means we just ignore this verb
        map.put( "refreshing", ScmFileStatus.UNKNOWN );
    }

    public static ScmFileStatus toStatus( String verb )
    {
        ScmFileStatus stat = (ScmFileStatus) map.get( verb );
        if ( stat == null )
        {
            // XXX testing only
            System.err.println( "No such verb: " + verb );
            return ScmFileStatus.UNKNOWN;
        }
        if ( stat == ScmFileStatus.UNKNOWN )
        {
            // Return a null status in cases where the verb does not indicate a status change.
            stat = null;
        }
        return stat;
    }

}
