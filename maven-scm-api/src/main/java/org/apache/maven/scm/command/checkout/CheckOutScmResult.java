package org.apache.maven.scm.command.checkout;

/*
 * LICENSE
 */

import java.util.List;

import org.apache.maven.scm.ScmResult;


/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckOutScmResult
    extends ScmResult
{
    private List checkedOutFiles;

    public CheckOutScmResult( String message, String longMessage )
    {
        super( message, longMessage );
    }

    public CheckOutScmResult( List checkedOutFiles )
    {
        this.checkedOutFiles = checkedOutFiles;
    }

    public List getCheckedOutFiles()
    {
        return checkedOutFiles;
    }
}
