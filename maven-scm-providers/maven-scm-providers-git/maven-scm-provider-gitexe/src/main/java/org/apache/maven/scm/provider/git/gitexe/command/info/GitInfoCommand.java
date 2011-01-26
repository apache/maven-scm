package org.apache.maven.scm.provider.git.gitexe.command.info;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Olivier Lamy
 *
 */
public class GitInfoCommand
 extends AbstractCommand
    implements GitCommand
{

    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        Commandline cli = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "show" );
        GitInfoConsumer consumer = new GitInfoConsumer( getLogger(), fileSet );
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        
        int exitCode = GitCommandLineUtils.execute( cli, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            throw new UnsupportedOperationException();
        }
        return new InfoScmResult( cli.toString(), consumer.getInfoItems() );
    }

    

}
