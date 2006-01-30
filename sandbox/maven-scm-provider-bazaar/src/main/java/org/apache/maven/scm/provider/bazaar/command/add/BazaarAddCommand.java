package org.apache.maven.scm.provider.bazaar.command.add;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;

/**
 * Add no recursive.
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarAddCommand
    extends AbstractAddCommand
    implements BazaarCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                          boolean binary )
        throws ScmException
    {
        String[] addCmd = new String[] { ADD_CMD, NO_RECURSE_OPTION };
        addCmd = BazaarUtils.expandCommandLine( addCmd, fileSet );

        File workingDir = fileSet.getBasedir();
        BazaarAddConsumer consumer = new BazaarAddConsumer( getLogger(), workingDir );
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), workingDir, addCmd );

        return wrapResult(consumer.getAddedFiles(), result);
    }

    private AddScmResult wrapResult( List files, ScmResult baseResult )
    {
        AddScmResult result;
        if ( baseResult.isSuccess() )
        {
            result = new AddScmResult( baseResult.getCommandLine(), files );
        }
        else
        {
            result = new AddScmResult( baseResult.getCommandLine(), baseResult.getProviderMessage(), baseResult
                .getCommandOutput(), baseResult.isSuccess() );
        }
        return result;
    }
}
