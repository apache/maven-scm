package org.apache.maven.scm.provider.hg.command.info;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;

/**
 * @author Olivier Lamy
 * @since 1.5
 */
public class HgInfoCommand
    extends AbstractCommand
    implements Command
{

    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        String[] revCmd = new String[]{ HgCommandConstants.REVNO_CMD, "-i" };
        HgInfoConsumer consumer = new HgInfoConsumer( getLogger() );
        ScmResult scmResult = HgUtils.execute( consumer, getLogger(), fileSet.getBasedir(), revCmd );
        return new InfoScmResult( consumer.getInfoItems(), scmResult );
    }

}
