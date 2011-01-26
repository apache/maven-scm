package org.apache.maven.scm.provider.hg.command.info;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

/**
 * @author Olivier Lamy
 * @since 1.5
 *
 */
public class HgInfoConsumer
    extends HgConsumer
{
    
    private List<InfoItem> infoItems = new ArrayList<InfoItem>( 1 );
    
    public HgInfoConsumer(ScmLogger scmLogger)
    {
        super(scmLogger);
    }
    

    /**
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        // hg id -i returns only one line so we are safe
        InfoItem infoItem = new InfoItem();
        infoItem.setRevision( line );
        this.infoItems.add( infoItem );
    }


    public List<InfoItem> getInfoItems()
    {
        return infoItems;
    }

}
