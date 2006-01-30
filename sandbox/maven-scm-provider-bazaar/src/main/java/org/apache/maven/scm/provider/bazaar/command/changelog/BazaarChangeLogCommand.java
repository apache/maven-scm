package org.apache.maven.scm.provider.bazaar.command.changelog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a> */
public class BazaarChangeLogCommand
    extends AbstractChangeLogCommand
    implements BazaarCommand
{
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                         Date startDate, Date endDate, int numDays, String branch )
        throws ScmException
    {
        String[] cmd = new String[] { LOG_CMD, VERBOSE_OPTION };
        BazaarChangeLogConsumer consumer = new BazaarChangeLogConsumer( getLogger(), fileSet.getBasedir() );
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), fileSet.getBasedir(), cmd );

        List logEntries = consumer.getModifications();
        List inRange = new ArrayList();
        startDate = startDate == null ? new Date(0) : startDate; //From 1. Jan 1970
        endDate = endDate == null ? new Date() : endDate; //Upto now
        if (numDays > 0) { //numDays takes precedence to start date
            Calendar rightNow = Calendar.getInstance();
            rightNow.add(Calendar.DATE, -numDays);
            startDate = rightNow.getTime();
        }
        for (Iterator it = logEntries.iterator(); it.hasNext(); ) {
            ChangeSet change = (ChangeSet)it.next();
            if (startDate != null) {
                if (!change.getDate().before(startDate) && !change.getDate().after(endDate)) {
                    inRange.add(change);
                }
            }
        }

        return wrapResult( inRange, result );
    }

    private ChangeLogScmResult wrapResult( List files, ScmResult result )
    {
        ChangeLogScmResult diffResult;
        if ( result.isSuccess() )
        {
            diffResult = new ChangeLogScmResult( result.getCommandLine(), files );
        }
        else
        {
            diffResult = new ChangeLogScmResult( result.getCommandLine(), result.getProviderMessage(), result
                .getCommandOutput(), result.isSuccess() );
        }
        return diffResult;
    }
}
