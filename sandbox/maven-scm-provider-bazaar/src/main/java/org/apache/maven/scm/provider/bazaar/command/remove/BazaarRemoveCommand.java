package org.apache.maven.scm.provider.bazaar.command.remove;

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarRemoveCommand
    extends AbstractRemoveCommand
    implements BazaarCommand
{
    protected ScmResult executeRemoveCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {

        String[] command = new String[] { REMOVE_CMD };
        BazaarUtils.expandCommandLine( command, fileSet );
        BazaarRemoveConsumer consumer = new BazaarRemoveConsumer( getLogger() );
        File workingDir = fileSet.getBasedir();

        ScmResult result = BazaarUtils.execute( consumer, getLogger(), workingDir, command );
        if ( result.isSuccess() )
        {
            return new RemoveScmResult( result.getCommandLine(), consumer.getRemovedFiles() );
        }

        return result;
    }
}
