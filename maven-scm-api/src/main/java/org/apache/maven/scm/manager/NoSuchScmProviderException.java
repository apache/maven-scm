package org.apache.maven.scm.manager;

/*
 * LICENSE
 */

import org.apache.maven.scm.ScmException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class NoSuchScmProviderException
	extends ScmException
{
    public NoSuchScmProviderException( String providerType )
    {
        super( "No such provider: '" + providerType + "'." );
    }
}
