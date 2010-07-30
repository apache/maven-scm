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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevCapability;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevVersion;
import org.apache.maven.scm.provider.accurev.FileDifference;
import org.apache.maven.scm.provider.accurev.Stream;
import org.apache.maven.scm.provider.accurev.Transaction;
import org.apache.maven.scm.provider.accurev.Transaction.Version;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;
import org.codehaus.plexus.util.StringUtils;

/**
 * TODO filter results based on project_path Find appropriate start and end transaction ids from parameters. Streams
 * must be the same. Diff on stream start to end - these are the upstream changes Hist on the stream start+1 to end
 * remove items from the upstream set if they appear in the history For workspaces diff doesn't work. So we would not
 * pickup any upstream changes, just the "keep" transactions which is not very useful. Hist on the workspace Then diff /
 * hist on the basis stream, skipping any transactions that are coming from the workspace.
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
            // if no end version supplied then use same basis as startVersion
            AccuRevVersion toVersion =
                endVersion == null ? new AccuRevVersion( fromVersion.getBasisStream(), "now" )
                                : repository.getAccuRevVersion( endVersion );
                
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
            endDate = new Date( System.currentTimeMillis() + day );
        }

        if ( endDate != null && startDate == null )
        {
            throw new ScmException( "The end date is set but the start date isn't." );
        }

        // Date parameters override transaction ids in versions
        if ( startDate != null )
        {
            fromSpec = AccuRevScmProviderRepository.formatTimeSpec( startDate );
        }
        else if ( fromSpec == null )
        {
            fromSpec = "1";
        }

        // Convert the fromSpec to both a date AND a transaction id by looking up
        // the nearest transaction in the depot.
        Transaction fromTransaction = getDepotTransaction( repository, stream, fromSpec );

        long fromTranId = 1;
        if ( fromTransaction != null )
        {
            // This tran id is less than or equal to the date/tranid we requested.
            fromTranId = fromTransaction.getTranId();
            if ( startDate == null )
            {
                startDate = fromTransaction.getWhen();
            }
        }

        if ( endDate != null )
        {
            toSpec = AccuRevScmProviderRepository.formatTimeSpec( endDate );
        }
        else if ( toSpec == null )
        {
            toSpec = "highest";
        }

        Transaction toTransaction = getDepotTransaction( repository, stream, toSpec );
        long toTranId = 1;
        if ( toTransaction != null )
        {
            toTranId = toTransaction.getTranId();
            if ( endDate == null )
            {
                endDate = toTransaction.getWhen();
            }
        }
        startVersion = new ScmRevision( repository.getRevision( stream, fromTranId ) );
        endVersion = new ScmRevision( repository.getRevision( stream, toTranId ) );

        //TODO Split this method in two here. above to convert params to start and end (stream,tranid,date) and test independantly
        
        List<Transaction> streamHistory = Collections.emptyList();
        List<Transaction> workspaceHistory = Collections.emptyList();
        List<FileDifference> streamDifferences = Collections.emptyList();

        StringBuffer errorMessage = new StringBuffer();

        AccuRev accurev = repository.getAccuRev();

        Stream changelogStream = accurev.showStream( stream );
        if ( changelogStream == null )
        {
            errorMessage.append( "Unknown accurev stream -" ).append( stream ).append( "." );
        }
        else
        {

            String message =
                "Changelog on stream " + stream + "(" + changelogStream.getStreamType() + ") from " + fromTranId + " ("
                    + startDate + "), to " + toTranId + " (" + endDate + ")";

            if ( startDate != null && startDate.after( endDate ) || fromTranId >= toTranId )
            {
                getLogger().warn( "Skipping out of range " + message );
            }
            else
            {

                getLogger().info( message );

                // In 4.7.2 and higher we have a diff command that will list all the file differences in a stream
                // and thus can be used to detect upstream changes
                // Unfortunately diff -v -V -t does not work in workspaces.
                Stream diffStream = changelogStream;
                if ( changelogStream.isWorkspace() )
                {

                    workspaceHistory =
                        accurev.history( stream, Long.toString( fromTranId + 1 ), Long.toString( toTranId ), 0, false,
                                         false );

                    if ( workspaceHistory == null )
                    {
                        errorMessage.append( "history on workspace " + stream + " from " + fromTranId + 1 + " to "
                            + toTranId + " failed." );

                    }

                    // do the diff/hist on the basis stream instead.
                    stream = changelogStream.getBasis();
                    diffStream = accurev.showStream( stream );

                }

                if ( AccuRevCapability.DIFF_BETWEEN_STREAMS.isSupported( accurev.getClientVersion() ) )
                {
                    if ( startDate.before( diffStream.getStartDate() ) )
                    {
                        getLogger().warn( "Skipping diff of " + stream + " due to start date out of range" );
                    }
                    else
                    {
                        streamDifferences =
                            accurev.diff( stream, Long.toString( fromTranId ), Long.toString( toTranId ) );
                        if ( streamDifferences == null )
                        {
                            errorMessage.append( "Diff " + stream + "- " + fromTranId + " to " + toTranId + "failed." );
                        }
                    }
                }

                // History needs to start from the transaction after our starting transaction

                streamHistory =
                    accurev.history( stream, Long.toString( fromTranId + 1 ), Long.toString( toTranId ), 0, false,
                                     false );
                if ( streamHistory == null )
                {
                    errorMessage.append( "history on stream " + stream + " from " + fromTranId + 1 + " to " + toTranId
                        + " failed." );
                }

            }
        }

        String errorString = errorMessage.toString();
        if ( StringUtils.isBlank( errorString ) )
        {
            ChangeLogSet changeLog =
                getChangeLog( changelogStream, streamDifferences, streamHistory, workspaceHistory, startDate, endDate );

            changeLog.setEndVersion( endVersion );
            changeLog.setStartVersion( startVersion );

            return new ChangeLogScmResult( accurev.getCommandLines(), changeLog );
        }
        else
        {
            return new ChangeLogScmResult( accurev.getCommandLines(), "AccuRev errors: " + errorMessage,
                                           accurev.getErrorOutput(), false );
        }

    }

    private Transaction getDepotTransaction( AccuRevScmProviderRepository repo, String stream, String tranSpec )
        throws AccuRevException
    {
        return repo.getDepotTransaction( stream, tranSpec );

    }

    private ChangeLogSet getChangeLog( Stream stream, List<FileDifference> streamDifferences,
                                       List<Transaction> streamHistory, List<Transaction> workspaceHistory,
                                       Date startDate, Date endDate )
    {

        // Collect all the "to" versions from the streamDifferences into a Map by element id
        // If that version is seen in the promote/keep history then we move it from the map
        // At the end we create a pseudo ChangeSet for any remaining entries in the map as
        // representing "upstream changes"
        Map<Long, FileDifference> differencesMap = new HashMap<Long, FileDifference>();
        for ( FileDifference fileDifference : streamDifferences )
        {
            differencesMap.put( fileDifference.getElementId(), fileDifference );
        }

        List<Transaction> mergedHistory = new ArrayList<Transaction>( streamHistory );
        // will never match a version
        String streamPrefix = "/";

        mergedHistory.addAll( workspaceHistory );
        streamPrefix = stream.getId() + "/";

        List<ChangeSet> entries = new ArrayList<ChangeSet>( streamHistory.size() );
        for ( Transaction t : mergedHistory )
        {
            if ( ( startDate != null && t.getWhen().before( startDate ) )
                || ( endDate != null && t.getWhen().after( endDate ) ) )
            {
                // This is possible if dates and transactions are mixed in the time spec.
                continue;
            }

            // Needed to make Tck test pass against accurev > 4.7.2 - the changelog only expects to deal with
            // files. Stream changes and cross links are important entries in the changelog.
            // However we should only see mkstream once and it is irrelevant given we are interrogating
            // the history of this stream.
            if ( "mkstream".equals( t.getTranType() ) )
            {
                continue;
            }

            Collection<Version> versions = t.getVersions();
            List<ChangeFile> files = new ArrayList<ChangeFile>( versions.size() );

            for ( Version v : versions )
            {

                // Remove diff representing this promote
                FileDifference difference = differencesMap.get( v.getElementId() );
                // TODO: how are defuncts shown in the version history?
                if ( difference != null )
                {
                    String newVersionSpec = difference.getNewVersionSpec();
                    if ( newVersionSpec != null && newVersionSpec.equals( v.getRealSpec() ) )
                    {
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Removing difference for " + v );
                        }
                        differencesMap.remove( v.getElementId() );
                    }
                }

                // Add this file, unless the virtual version indicates this is the basis stream, and the real
                // version came from our workspace stream (ie, this transaction is a promote from the workspace
                // to its basis stream, and is therefore NOT a change
                if ( v.getRealSpec().startsWith( streamPrefix ) && !v.getVirtualSpec().startsWith( streamPrefix ) )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Skipping workspace to basis stream promote " + v );
                    }
                }
                else
                {
                    ChangeFile f =
                        new ChangeFile( v.getElementName(), v.getVirtualSpec() + " (" + v.getRealSpec() + ")" );
                    files.add( f );
                }

            }

            if ( versions.isEmpty() || !files.isEmpty() )
            {
                ChangeSet changeSet = new ChangeSet( t.getWhen(), t.getComment(), t.getAuthor(), files );

                entries.add( changeSet );
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "All versions removed for " + t );
                }
            }

        }

        // Anything left in the differencesMap represents a change from a higher stream
        // We don't have details on who or where these came from, but it is important to
        // detect these for CI tools like Continuum
        if ( !differencesMap.isEmpty() )
        {
            List<ChangeFile> upstreamFiles = new ArrayList<ChangeFile>();
            for ( FileDifference difference : differencesMap.values() )
            {
                if ( difference.getNewVersionSpec() != null )
                {
                    upstreamFiles.add( new ChangeFile( difference.getNewFile().getPath(),
                                                       difference.getNewVersionSpec() ) );
                }
                else
                {
                    // difference is a deletion
                    upstreamFiles.add( new ChangeFile( difference.getOldFile().getPath(), null ) );
                }
            }
            entries.add( new ChangeSet( endDate, "Upstream changes", "various", upstreamFiles ) );
        }

        return new ChangeLogSet( entries, startDate, endDate );
    }

    public ChangeLogScmResult changelog( ScmProviderRepository repo, ScmFileSet testFileSet, CommandParameters params )
        throws ScmException
    {

        return (ChangeLogScmResult) execute( repo, testFileSet, params );
    }

}
