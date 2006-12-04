package org.apache.maven.scm.provider.perforce.command;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the 'p4 info' command which can be very useful in determining
 * the runtime environment.  Use <code>getEntry(String key)</code> to query
 * the info set for a particular property.  The data from p4 info looks like this:
 *
 * <pre>
   User name: mperham
   Client name: mikeperham-dt
   Client host: mikeperham-dt
   Client root: d:\perforce
   </pre>
 *
 * where the key is the content before the first colon and the value is the data after
 * the first colon, trimmed.  For example:
 * <code>PerforceInfoCommand.getInfo( this, repo ).getEntry( "User name" )</code>
 * <p>
 * Note that this is not a traditional SCM command.  This uses the Command class
 * simply because it needs a logger for error handling and the current repository data for
 * command line creation.
 *
 *
 * @author mperham
 */
public class PerforceInfoCommand extends AbstractCommand implements PerforceCommand
{
    private static PerforceInfoCommand singleton = null;
    private Map entries = null;

    public static PerforceInfoCommand getInfo( AbstractCommand cmd, PerforceScmProviderRepository repo )
    {
        return getSingleton( cmd, repo );
    }

    public String getEntry( String key )
    {
        return (String) entries.get( key );
    }

    private static synchronized PerforceInfoCommand getSingleton( AbstractCommand cmd, PerforceScmProviderRepository repo )
    {
        if (singleton == null)
        {
            PerforceInfoCommand pic = new PerforceInfoCommand();
            if ( cmd != null )
            {
                pic.setLogger( cmd.getLogger() );
            }
            try
            {
                pic.executeCommand( repo, null, null );
                singleton = pic;
            }
            catch ( ScmException e )
            {
                pic.getLogger().error( e );
            }
        }
        return singleton;
    }

    protected ScmResult executeCommand( ScmProviderRepository repo, ScmFileSet scmFileSet,
                                        CommandParameters commandParameters )
        throws ScmException
    {
        if ( !PerforceScmProvider.isLive() )
        {
            return null;
        }

        boolean log = getLogger() != null;
        try
        {
            Commandline command = PerforceScmProvider.createP4Command( (PerforceScmProviderRepository) repo, null );
            command.createArgument().setValue( "info" );
            if (log)
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing: " + command.toString() ) );
            }
            Process proc = command.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
            entries = new HashMap();
            while ( ( line = br.readLine() ) != null )
            {
                int idx = line.indexOf( ':' );
                if ( idx == -1 )
                {
                    throw new IllegalStateException( "Unexpected results from 'p4 info' command: " + line );
                }
                String key = line.substring( 0, idx );
                String value = line.substring( idx + 1 ).trim();
                entries.put(key, value);
            }
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( e.getLocalizedMessage() );
        }
        catch ( IOException e )
        {
            throw new ScmException( e.getLocalizedMessage() );
        }
        return null;
    }
}
