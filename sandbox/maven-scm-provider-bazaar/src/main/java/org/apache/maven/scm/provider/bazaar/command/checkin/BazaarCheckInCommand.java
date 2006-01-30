package org.apache.maven.scm.provider.bazaar.command.checkin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.apache.maven.scm.provider.bazaar.command.status.BazaarStatusCommand;
import org.apache.maven.scm.provider.bazaar.repository.BazaarScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarCheckInCommand
    extends AbstractCheckInCommand
{

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                     String tag )
        throws ScmException
    {

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        // Get files that will be committed (if not specified in fileSet)
        List commitedFiles = new ArrayList();
        File[] files = fileSet.getFiles();
        if ( files.length == 0 )
        { //Either commit all changes
            BazaarStatusCommand statusCmd = new BazaarStatusCommand();
            statusCmd.setLogger( getLogger() );
            StatusScmResult status = statusCmd.executeStatusCommand( repo, fileSet );
            List statusFiles = status.getChangedFiles();
            for ( Iterator it = statusFiles.iterator(); it.hasNext(); )
            {
                ScmFile file = (ScmFile) it.next();
                if ( file.getStatus() == ScmFileStatus.ADDED || file.getStatus() == ScmFileStatus.DELETED
                    || file.getStatus() == ScmFileStatus.MODIFIED )
                {
                    commitedFiles.add( new ScmFile( file.getPath(), ScmFileStatus.CHECKED_IN ) );
                }
            }

        }
        else
        { //Or commit spesific files
            for ( int i = 0; i < files.length; i++ )
            {
                commitedFiles.add( new ScmFile(files[i].getPath(), ScmFileStatus.CHECKED_IN) );
            }
        }

        // Commit to local branch
        String[] commitCmd = new String[] { BazaarCommand.COMMIT_CMD, BazaarCommand.MESSAGE_OPTION, message };
        commitCmd = BazaarUtils.expandCommandLine( commitCmd, fileSet );
        BazaarUtils.execute( new BazaarConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), commitCmd );

        // Push to parent branch if any
        BazaarScmProviderRepository repository = (BazaarScmProviderRepository) repo;
        if ( !repository.getURI().equals( fileSet.getBasedir().getAbsolutePath() ) )
        {
            String[] push_cmd = new String[] { BazaarCommand.PUSH_CMD, repository.getURI() };
            ScmResult result = BazaarUtils.execute( new BazaarConsumer( getLogger() ), getLogger(), fileSet
                .getBasedir(), push_cmd );
            return wrapResult( commitedFiles, result );
        }

        return new CheckInScmResult( commitCmd[0], commitedFiles );
    }

    private CheckInScmResult wrapResult( List files, ScmResult baseResult )
    {
        CheckInScmResult result;
        if ( baseResult.isSuccess() )
        {
            result = new CheckInScmResult( baseResult.getCommandLine(), files );
        }
        else
        {
            result = new CheckInScmResult( baseResult.getCommandLine(), baseResult.getProviderMessage(), baseResult
                .getCommandOutput(), baseResult.isSuccess() );
        }
        return result;
    }
}
