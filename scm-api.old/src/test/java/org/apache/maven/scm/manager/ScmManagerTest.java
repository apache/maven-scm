package org.apache.maven.scm.manager;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ScmManagerTest
    extends PlexusTestCase
{
    public ScmManagerTest( String name )
    {
        super( name );
    }

    public void testScmManager()
        throws Exception
    {
        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );

        assertNotNull( scmManager );
    }
}