/**
 * 
 */
package org.apache.maven.scm.provider.git.gitexe.command.info;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Olivier Lamy
 *
 */
public class GitInfoConsumer
    extends AbstractConsumer
{

    //$ git show
    //commit cd3c0dfacb65955e6fbb35c56cc5b1bf8ce4f767

    private List<InfoItem> infoItems = new ArrayList<InfoItem>( 1 );

    private ScmFileSet scmFileSet;
    public GitInfoConsumer( ScmLogger logger, ScmFileSet scmFileSet )
    {
        super( logger );
        this.scmFileSet = scmFileSet;
    }
    
    /**
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "consume line " + line );
        }
        
        if ( infoItems.isEmpty() )
        {
            if ( !StringUtils.isEmpty( line ) && line.startsWith( "commit" ) )
            {
                InfoItem infoItem = new InfoItem();
                infoItem.setRevision( StringUtils.trim( line.substring( "commit".length() ) ) );
                infoItem.setURL( scmFileSet.getBasedir().getPath() );
                infoItems.add( infoItem );
            }
        }

    }

    public List<InfoItem> getInfoItems()
    {
        return infoItems;
    }

}
