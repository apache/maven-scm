package org.apache.maven.scm.provider.cvslib.command.changelog;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsChangeLogCommand
    extends AbstractChangeLogCommand
    implements CvsCommand
{
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, File workingDirectory, Date startDate, Date endDate, int numDays, String branch )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        if ( numDays > 0 )
        {
            startDate = new Date( System.currentTimeMillis() - (long) numDays * 24 * 60 * 60 * 1000 );

            endDate = new Date( System.currentTimeMillis() + (long) 1 * 24 * 60 * 60 * 1000 );
        }

        Commandline cl = new Commandline();

        cl.setExecutable( "cvs" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        cl.createArgument().setValue( "-d" );

        cl.createArgument().setValue( repository.getCvsRoot() );

        cl.createArgument().setValue( "-q" );

        cl.createArgument().setValue( "log" );

        if ( startDate != null )
        {
            SimpleDateFormat outputDate = new SimpleDateFormat( "yyyy-MM-dd" );

            String dateRange;

            if ( endDate == null )
            {
                dateRange = ">" + outputDate.format( startDate );
            }
            else
            {
                dateRange = outputDate.format( startDate ) + "<" + outputDate.format( endDate );
            }

            cl.createArgument().setValue( "-d " + dateRange );
        }

        if ( branch != null )
        {
            cl.createArgument().setValue( "-r" + branch );
        }

        CvsChangeLogConsumer consumer = new CvsChangeLogConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch( CommandLineException ex )
        {
            throw new ScmException( "Error while executing cvs command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( "The cvs command failed.", stderr.getOutput(), false );
        }

        return new ChangeLogScmResult( consumer.getModifications() );
    }
}
