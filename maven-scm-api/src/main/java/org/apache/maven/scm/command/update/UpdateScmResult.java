package org.apache.maven.scm.command.update;

/*
 * LICENSE
 */

import java.util.List;

import org.apache.maven.scm.ScmResult;


/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UpdateScmResult
    extends ScmResult
{
    private List updatedFiles;

    public UpdateScmResult( String message, String longMessage )
    {
        super( message, longMessage );
    }

    public UpdateScmResult( List updatedFiles )
    {
        this.updatedFiles = updatedFiles;
    }

    public List getUpdatedFiles()
    {
        return updatedFiles;
    }
}
