package org.apache.maven.scm.command.changelog;

/*
 * LICENSE
 */

import java.util.List;

import org.apache.maven.scm.ScmResult;


/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ChangeLogScmResult
    extends ScmResult
{
    private List changeLog;

    public ChangeLogScmResult( String message, String longMessage )
    {
        super( message, longMessage );
    }

    public ChangeLogScmResult( List changeLog )
    {
        this.changeLog = changeLog;
    }

    public List getChangeLog()
    {
        return changeLog;
    }
}
