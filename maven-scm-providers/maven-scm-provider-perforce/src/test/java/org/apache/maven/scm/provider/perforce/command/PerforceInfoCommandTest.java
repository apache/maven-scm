package org.apache.maven.scm.provider.perforce.command;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.PerforceScmProviderTest;
import org.apache.maven.scm.provider.perforce.command.tag.PerforceTagCommand;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PerforceScmProviderTest.java 372209 2006-01-25 13:30:01Z evenisse $
 */
public class PerforceInfoCommandTest
    extends ScmTestCase
{
    public void testPerforceInfo()
        throws Exception
    {
        ScmRepository repo = makeScmRepository( "scm:perforce://depot/projects/pathname" );

        PerforceScmProviderRepository p4Repo = (PerforceScmProviderRepository) repo.getProviderRepository();

        PerforceScmProvider prov = new PerforceScmProvider();
        PerforceTagCommand cmd = new PerforceTagCommand();
        cmd.setLogger( prov.getLogger() );

        if ( PerforceScmProviderTest.hasClientBinaries() )
        {
            assertNotNull( PerforceInfoCommand.getInfo( cmd, p4Repo ).getEntry( "User name" ) );
            assertNotNull( PerforceInfoCommand.getInfo( cmd, p4Repo ).getEntry( "Client root" ) );
            assertNotNull( PerforceInfoCommand.getInfo( cmd, p4Repo ).getEntry( "Client name" ) );
            assertNotNull( PerforceInfoCommand.getInfo( cmd, p4Repo ).getEntry( "Client host" ) );
            assertNull( PerforceInfoCommand.getInfo( cmd, p4Repo ).getEntry( "foobar" ) );
        }
    }
}
