package org.apache.maven.scm.provider.bazaar.command.diff;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.codehaus.plexus.util.StringUtils;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarDiffCommand
    extends AbstractDiffCommand
    implements BazaarCommand
{

    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, String startRevision,
                                               String endRevision )
        throws ScmException
    {

        String[] diffCmd;
        if ( !StringUtils.isEmpty( startRevision ) )
        {
            String revArg = startRevision;
            if ( !StringUtils.isEmpty( endRevision ) )
            {
                revArg += ".." + endRevision;
            }
            diffCmd = new String[] { DIFF_CMD, REVISION_OPTION, revArg };
        }
        else
        {
            diffCmd = new String[] { DIFF_CMD };
        }

        diffCmd = BazaarUtils.expandCommandLine( diffCmd, fileSet );
        BazaarDiffConsumer consumer = new BazaarDiffConsumer( getLogger(), fileSet.getBasedir() );

        ScmResult result = BazaarUtils.execute( consumer, getLogger(), fileSet.getBasedir(), diffCmd );

        return wrapResult( consumer, result );
    }

    private DiffScmResult wrapResult( BazaarDiffConsumer consumer, ScmResult result )
    {
        DiffScmResult diffResult;
        if ( result.isSuccess() )
        {
            diffResult = new DiffScmResult( result.getCommandLine(), consumer.getChangedFiles(), consumer
                .getDifferences(), consumer.getPatch() );
        }
        else
        {
            diffResult = new DiffScmResult( result.getCommandLine(), result.getProviderMessage(), result
                .getCommandOutput(), result.isSuccess() );
        }
        return diffResult;
    }
}
