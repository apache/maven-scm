package org.apache.maven.scm.provider.perforce.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.ScmFileStatus;

/**
 * @author mperham
 * @version $Id$
 */
public class PerforceVerbMapper
{
    private static final Map map = new HashMap();

    static
    {
        map.put( "add", ScmFileStatus.ADDED );
        map.put( "delete", ScmFileStatus.DELETED );
        map.put( "edit", ScmFileStatus.MODIFIED );
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
        return stat;
    }

}
