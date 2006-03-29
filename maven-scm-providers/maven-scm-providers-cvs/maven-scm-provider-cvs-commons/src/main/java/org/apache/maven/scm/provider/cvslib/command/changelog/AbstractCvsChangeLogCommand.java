package org.apache.maven.scm.provider.cvslib.command.changelog;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCvsChangeLogCommand
    extends AbstractChangeLogCommand
    implements CvsCommand
{
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          String startTag, String endTag, String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, null, null, null, startTag, endTag, datePattern );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, String branch,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, null, null, datePattern );
    }

    private ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet, Date startDate,
                                                        Date endDate, String branch, String startTag, String endTag,
                                                        String datePattern )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "log", repository, fileSet );

        if ( startDate != null )
        {
            SimpleDateFormat outputDate = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );

            String dateRange;

            if ( endDate == null )
            {
                dateRange = ">" + outputDate.format( startDate );
            }
            else
            {
                dateRange = outputDate.format( startDate ) + "<" + outputDate.format( endDate );
            }

            cl.createArgument().setValue( "-d" );

            cl.createArgument().setValue( "\"" + dateRange + "\"" );
        }

        if ( branch != null )
        {
            cl.createArgument().setValue( "-r" + branch );
        }

        if ( startTag != null )
        {
            String param = "-r" + startTag + "::" + ( endTag != null ? endTag : "" );

            cl.createArgument().setValue( param );
        }

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        return executeCvsCommand( cl, startDate, endDate, datePattern );
    }

    protected abstract ChangeLogScmResult executeCvsCommand( Commandline cl, Date startDate, Date endDate,
                                                             String datePattern )
        throws ScmException;
}
