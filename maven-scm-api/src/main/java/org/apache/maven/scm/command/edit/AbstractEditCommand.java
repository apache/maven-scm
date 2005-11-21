package org.apache.maven.scm.command.edit;

import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.*;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * 
 */
public abstract class AbstractEditCommand extends AbstractCommand
{
    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return executeEditCommand( repository, fileSet );
    }

    protected abstract ScmResult executeEditCommand( ScmProviderRepository repository, ScmFileSet fileSet ) throws ScmException;
}
