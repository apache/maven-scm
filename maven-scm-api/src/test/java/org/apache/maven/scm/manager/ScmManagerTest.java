package org.maven.apache.scm.manager;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ScmManagerTest
    extends ScmTestCase
{
    protected String getRepositoryName()
    {
        return "scm-manager";
    }

    public void testNonExistingScmType()
    	throws Exception
    {
        ScmManager scmManager = getScmManager();

        try
        {
            scmManager.checkOut( scmManager.makeScmRepository( "scm:non-existing-scm:" ), getTestFile( "" ), null );

            fail( "Expected NoSuchScmTypeException" );
        }
        catch( NoSuchScmProviderException ex )
        {
            // expected
        }
    }

    // TODO: Add a non-existing command test
}
