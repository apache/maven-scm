package org.apache.maven.scm.command.checkin;

/*
 * LICENSE
 */

import java.util.List;

import org.apache.maven.scm.ScmResult;


/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckInScmResult
    extends ScmResult
{
    private List checkedInFiles;

    public CheckInScmResult( String message, String longMessage )
    {
        super( message, longMessage );
    }

    public CheckInScmResult( List checkedInFiles )
    {
        this.checkedInFiles = checkedInFiles;
    }

    public List getCheckedInFiles()
    {
        return checkedInFiles;
    }
}
