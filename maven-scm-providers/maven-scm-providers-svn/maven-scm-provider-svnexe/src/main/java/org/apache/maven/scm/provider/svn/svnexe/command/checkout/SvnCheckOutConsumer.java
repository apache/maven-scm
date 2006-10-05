package org.apache.maven.scm.provider.svn.svnexe.command.checkout;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.svn.svnexe.command.AbstractFileCheckingConsumer;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnCheckOutConsumer
    extends AbstractFileCheckingConsumer
{
    private final static String CHECKED_OUT_REVISION_TOKEN = "Checked out revision";

    public SvnCheckOutConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger, workingDirectory );
    }

    protected void parseLine( String line )
    {
        String statusString = line.substring( 0, 1 );

        String file = line.substring( 3 ).trim();

        ScmFileStatus status;

        if ( line.startsWith( CHECKED_OUT_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( CHECKED_OUT_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( statusString.equals( "U" ) )
        {
            status = ScmFileStatus.UPDATED;
        }
        else
        {
            logger.info( "Unknown file status: '" + statusString + "'." );

            return;
        }

        addFile( new ScmFile( file, status ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List getCheckedOutFiles()
    {
        return getFiles();
    }
}
