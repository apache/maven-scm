package org.apache.maven.scm.command.unedit;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * 
 */
public abstract class AbstractUnEditCommand
    extends AbstractCommand
{
    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        return executeUnEditCommand( repository, fileSet );
    }

    protected abstract ScmResult executeUnEditCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException;
}
