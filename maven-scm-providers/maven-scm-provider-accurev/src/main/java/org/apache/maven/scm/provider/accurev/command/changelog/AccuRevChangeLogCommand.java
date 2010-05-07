package org.apache.maven.scm.provider.accurev.command.changelog;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevVersion;
import org.apache.maven.scm.provider.accurev.Transaction;
import org.apache.maven.scm.provider.accurev.Transaction.Version;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;
import org.codehaus.plexus.util.StringUtils;

/**
 * TODO filter results based on project_path
 * 
 * @author ggardner
 */
public class AccuRevChangeLogCommand
    extends AbstractAccuRevCommand
{

    public AccuRevChangeLogCommand( ScmLogger logger )
    {
        super( logger );
    }

    @Override
    protected ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException, AccuRevException
    {

        // Do we have a supplied branch. If not we default to the URL stream.
        ScmBranch branch = (ScmBranch) parameters.getScmVersion( CommandParameter.BRANCH, null );
        AccuRevVersion branchVersion = repository.getAccuRevVersion( branch );
        String stream = branchVersion.getBasisStream();
        String fromSpec = branchVersion.getTimeSpec();
        String toSpec = "highest";

        // Versions
        ScmVersion startVersion = parameters.getScmVersion( CommandParameter.START_SCM_VERSION, null );
        ScmVersion endVersion = parameters.getScmVersion( CommandParameter.END_SCM_VERSION, null );

        if ( startVersion != null && StringUtils.isNotEmpty( startVersion.getName() ) )
        {
            AccuRevVersion fromVersion = repository.getAccuRevVersion( startVersion );
            AccuRevVersion toVersion = repository.getAccuRevVersion( endVersion );

            if ( !StringUtils.equals( fromVersion.getBasisStream(), toVersion.getBasisStream() ) )
            {
                throw new AccuRevException( "Not able to provide change log between different streams " + fromVersion
                    + "," + toVersion );
            }

            stream = fromVersion.getBasisStream();
            fromSpec = fromVersion.getTimeSpec();
            toSpec = toVersion.getTimeSpec();

        }

        Date startDate = parameters.getDate( CommandParameter.START_DATE, null );
        Date endDate = parameters.getDate( CommandParameter.END_DATE, null );
        int numDays = parameters.getInt( CommandParameter.NUM_DAYS, 0 );

        if ( numDays > 0 )
        {
            if ( ( startDate != null || endDate != null ) )
            {
                throw new ScmException( "Start or end date cannot be set if num days is set." );
            }
            // Last x days.
            int day = 24 * 60 * 60 * 1000;
            startDate = new Date( System.currentTimeMillis() - (long) numDays * day );
            endDate = new Date( System.currentTimeMillis() + (long) day );
        }

        if ( endDate != null && startDate == null )
        {
            throw new ScmException( "The end date is set but the start date isn't." );
        }

        if ( startDate == null )
        {
            if ( fromSpec == null )
            {
                fromSpec = "1";
            }
            startDate = getSpecAsDate( repository, stream, fromSpec );
        }
        else
        {
            fromSpec = formatTimeSpec( startDate );
        }

        if ( endDate == null )
        {
            if ( toSpec == null )
            {
                toSpec = "highest";
            }
            endDate = getSpecAsDate( repository, stream, toSpec );
        }
        else
        {
            toSpec = formatTimeSpec( endDate );
        }

        List<Transaction> streamHistory = new ArrayList<Transaction>();

        AccuRev accurev = repository.getAccuRev();

        String message = "Changelog: from " + fromSpec + " (" + startDate + "), to " + toSpec + " (" + endDate + ")";
        boolean success = true;
        if ( startDate != null && startDate.after( endDate ) )
        {
            getLogger().warn( "Skipping " + message );
        }
        else
        {
            getLogger().info( message );
            success = accurev.history( stream, fromSpec, toSpec, 0, streamHistory );
        }

        if ( success )
        {
            return new ChangeLogScmResult( accurev.getCommandLines(), getChangeLog( streamHistory, startDate, endDate ) );
        }
        else
        {
            return new ChangeLogScmResult( accurev.getCommandLines(), "AccuRev Error", accurev.getErrorOutput(), false );
        }

    }

    public static String formatTimeSpec( Date when )
    {
        if ( when == null )
        {
            return "now";
        }

        return AccuRev.ACCUREV_TIME_SPEC.format( when );

    }

    private Date getSpecAsDate( AccuRevScmProviderRepository repo, String stream, String timeSpec )
        throws AccuRevException
    {

        if ( "now".equals( timeSpec ) || "highest".equals( timeSpec ) )
        {
            return new Date();
        }

        if ( StringUtils.isNumeric( timeSpec ) )
        {
            AccuRev accuRev = repo.getAccuRev();
            List<Transaction> transactions = new ArrayList<Transaction>();
            accuRev.history( stream, timeSpec, null, 1, transactions );
            if ( transactions.size() > 0 )
            {
                return transactions.get( 0 ).getWhen();
            }
            else
            {
                return null;
            }
        }

        try
        {
            return AccuRev.ACCUREV_TIME_SPEC.parse( timeSpec );
        }
        catch ( ParseException e )
        {
            throw new AccuRevException( "Invalid timespec " + timeSpec, e );
        }

    }

    private ChangeLogSet getChangeLog( List<Transaction> streamHistory, Date startDate, Date endDate )
    {

        List<ChangeSet> entries = new ArrayList<ChangeSet>( streamHistory.size() );
        for ( Transaction t : streamHistory )
        {
            if ( ( startDate != null && t.getWhen().before( startDate ) )
                || ( endDate != null && t.getWhen().after( endDate ) ) )
            {
                // This is possible if dates and transactions are mixed in the time spec.
                continue;
            }

            Collection<Version> versions = t.getVersions();
            List<ChangeFile> files = new ArrayList<ChangeFile>( versions.size() );
            for ( Version v : versions )
            {
                ChangeFile f = new ChangeFile( v.getElementName(), v.getVirtualSpec() + " (" + v.getRealSpec() + ")" );
                files.add( f );
            }
            ChangeSet changeSet = new ChangeSet( t.getWhen(), t.getComment(), t.getAuthor(), files );

            entries.add( changeSet );
        }

        return new ChangeLogSet( entries, startDate, endDate );
    }

    public ChangeLogScmResult changelog( ScmProviderRepository repo, ScmFileSet testFileSet, CommandParameters params )
        throws ScmException
    {
        return (ChangeLogScmResult) execute( repo, testFileSet, params );
    }

}
