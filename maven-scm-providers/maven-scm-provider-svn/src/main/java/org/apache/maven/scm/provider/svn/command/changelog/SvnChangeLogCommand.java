package org.apache.maven.scm.provider.svn.command.changelog;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
import java.util.TimeZone;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogCommand
    extends AbstractChangeLogCommand
    implements SvnCommand
{
    private final static String DATE_FORMAT = "yyyy/MM/dd 'GMT'";

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, File workingDirectory, Date startDate, Date endDate, int numDays, String branch )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, workingDirectory, branch, startDate, endDate );

        SvnChangeLogConsumer consumer = new SvnChangeLogConsumer();

        // TODO: implement

        return new ChangeLogScmResult( consumer.getModifications() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory, String branch, Date startDate, Date endDate )
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );

        dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

        Commandline cl = new Commandline();

        cl.setExecutable( "svn" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        cl.createArgument().setValue( "log" );

        cl.createArgument().setValue( "--non-interactive" );

        cl.createArgument().setValue( "-v" );

        if ( startDate != null )
        {
            cl.createArgument().setValue( "-r" );

            if ( endDate != null )
            {
                cl.createArgument().setValue( "{" + dateFormat.format( startDate ) + "}" + ":" +
                                              "{" + dateFormat.format( endDate ) + "}" );
            }
            else
            {
                cl.createArgument().setValue( "{" + dateFormat.format( startDate ) + "}:HEAD" );
            }
        }
        else
        {
            if ( branch != null )
            {
                cl.createArgument().setValue( "-r" );
                cl.createArgument().setValue( branch );
            }
        }

        if ( repository.getUser() != null )
        {
            cl.createArgument().setValue( "--username" );

            cl.createArgument().setValue( repository.getUser() );
        }

        if ( repository.getPassword() != null )
        {
            cl.createArgument().setValue( "--password" );

            cl.createArgument().setValue( repository.getPassword() );
        }

        cl.createArgument().setValue( repository.getUrl() );

        return cl;
    }
}
