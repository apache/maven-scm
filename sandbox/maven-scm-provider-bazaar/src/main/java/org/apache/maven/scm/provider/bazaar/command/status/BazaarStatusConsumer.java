package org.apache.maven.scm.provider.bazaar.command.status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
class BazaarStatusConsumer
    extends BazaarConsumer
{
    private final List repositoryStatus = new ArrayList();

    private final File workingDir;

    /** State currently consuming (one of the identifieres or null) */
    private ScmFileStatus currentState = null;

    BazaarStatusConsumer( ScmLogger logger, File workingDir )
    {
        super( logger );
        this.workingDir = workingDir;
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        if ( status != null )
        {
            currentState = status;
            return;
        }

        if ( currentState == null )
        {
            return;
        }

        //Only include real files (not directories)
        File tmpFile = new File( workingDir, trimmedLine );
        if ( !tmpFile.exists() )
        {
            logger.info( "Not a file: " + tmpFile + ". Ignoring" );
        }
        else if ( tmpFile.isDirectory() )
        {
            logger.info( "New directory added: " + tmpFile );
        }
        else
        {
            ScmFile scmFile = new ScmFile( trimmedLine, currentState );
            logger.info( scmFile.toString() );
            repositoryStatus.add( scmFile );
        }
    }

    List getStatus()
    {
        return repositoryStatus;
    }
}
