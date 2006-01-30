package org.apache.maven.scm.provider.bazaar.command.remove;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarRemoveConsumer
    extends BazaarConsumer
{
    private final List removedFiles = new ArrayList();

    public BazaarRemoveConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        //TODO
    }

    public List getRemovedFiles()
    {
        return removedFiles;
    }
}
