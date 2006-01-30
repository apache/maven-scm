package org.apache.maven.scm.provider.bazaar.command.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarAddConsumer
    extends BazaarConsumer
{
    private final File workingDir;

    private final List addedFiles = new ArrayList();

    public BazaarAddConsumer( ScmLogger logger, File workingDir )
    {
        super( logger );
        this.workingDir = workingDir;
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        if ( status != null && status == ScmFileStatus.ADDED )
        {
            //Only include real files (not directories)
            File tmpFile = new File( workingDir, trimmedLine );
            if ( !tmpFile.exists() )
            {
                logger.warn( "Not a file: " + tmpFile + ". Ignored" );
            }
            else
            {
                ScmFile scmFile = new ScmFile( trimmedLine, ScmFileStatus.ADDED );
                logger.info( scmFile.toString() );
                addedFiles.add( scmFile );
            }
        }
    }

    public List getAddedFiles()
    {
        return addedFiles;
    }
}
