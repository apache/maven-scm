package org.apache.maven.scm.provider.bazaar.command.checkout;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.apache.maven.scm.provider.bazaar.repository.BazaarScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarCheckOutCommand
    extends AbstractCheckOutCommand
    implements BazaarCommand
{

    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        BazaarScmProviderRepository repository = (BazaarScmProviderRepository) repo;
        String url = repository.getURI();

        File checkoutDir = fileSet.getBasedir();
        try
        {
            getLogger().info( "Removing " + checkoutDir );
            FileUtils.deleteDirectory( checkoutDir );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Cannot remove " + checkoutDir );
        }

        // Do the actual checkout
        String[] checkout_cmd = new String[] { BRANCH_CMD, url, checkoutDir.getAbsolutePath() };
        BazaarConsumer checkout_consumer = new BazaarConsumer( getLogger() );
        BazaarUtils.execute( checkout_consumer, getLogger(), checkoutDir.getParentFile(), checkout_cmd );

        // Do inventory to find list of checkedout files
        String[] inventory_cmd = new String[] { INVENTORY_CMD };
        BazaarCheckOutConsumer consumer = new BazaarCheckOutConsumer( getLogger(), checkoutDir );
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), checkoutDir, inventory_cmd );

        return wrapResult( consumer.getCheckedOutFiles(), result );
    }

    private CheckOutScmResult wrapResult( List files, ScmResult baseResult )
    {
        CheckOutScmResult result;
        if ( baseResult.isSuccess() )
        {
            result = new CheckOutScmResult( baseResult.getCommandLine(), files );
        }
        else
        {
            result = new CheckOutScmResult( baseResult.getCommandLine(), baseResult.getProviderMessage(), baseResult
                .getCommandOutput(), baseResult.isSuccess() );
        }
        return result;
    }
}
