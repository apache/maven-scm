package org.apache.maven.scm.provider.bazaar.command.status;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarStatusCommand
    extends AbstractStatusCommand
    implements BazaarCommand
{

    public BazaarStatusCommand() {
        super();
    }

    public StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {

        File workingDir = fileSet.getBasedir();
        BazaarStatusConsumer consumer = new BazaarStatusConsumer(getLogger(), workingDir);
        String[] statusCmd = new String[] {STATUS_CMD};
        ScmResult result = BazaarUtils.execute(consumer, getLogger(), workingDir, statusCmd);

        return wrapResult(consumer.getStatus(), result);
    }

    private StatusScmResult wrapResult( List files, ScmResult baseResult )
    {
        StatusScmResult result;
        if ( baseResult.isSuccess() )
        {
            result = new StatusScmResult( baseResult.getCommandLine(), files );
        }
        else
        {
            result = new StatusScmResult( baseResult.getCommandLine(), baseResult.getProviderMessage(), baseResult
                .getCommandOutput(), baseResult.isSuccess() );
        }
        return result;
    }
}
