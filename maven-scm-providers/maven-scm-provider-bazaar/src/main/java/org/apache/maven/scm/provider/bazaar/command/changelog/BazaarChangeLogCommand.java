package org.apache.maven.scm.provider.bazaar.command.changelog;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarChangeLogCommand
    extends AbstractChangeLogCommand
    implements BazaarCommand
{
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, String branch )
        throws ScmException
    {
        String[] cmd = new String[]{LOG_CMD, VERBOSE_OPTION};
        BazaarChangeLogConsumer consumer = new BazaarChangeLogConsumer( getLogger(), fileSet.getBasedir() );
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), fileSet.getBasedir(), cmd );

        List logEntries = consumer.getModifications();
        List inRange = new ArrayList();
        startDate = startDate == null ? new Date( 0 ) : startDate; //From 1. Jan 1970
        endDate = endDate == null ? new Date() : endDate; //Upto now

        for ( Iterator it = logEntries.iterator(); it.hasNext(); )
        {
            ChangeSet change = (ChangeSet) it.next();
            if ( startDate != null )
            {
                if ( !change.getDate().before( startDate ) && !change.getDate().after( endDate ) )
                {
                    inRange.add( change );
                }
            }
        }

        return wrapResult( new ChangeLogSet( inRange, startDate, endDate ), result );
    }

    private ChangeLogScmResult wrapResult( ChangeLogSet changeLogSet, ScmResult result )
    {
        ChangeLogScmResult diffResult;
        if ( result.isSuccess() )
        {
            diffResult = new ChangeLogScmResult( result.getCommandLine(), changeLogSet );
        }
        else
        {
            diffResult = new ChangeLogScmResult( result.getCommandLine(), result.getProviderMessage(), result
                .getCommandOutput(), result.isSuccess() );
        }
        return diffResult;
    }
}
