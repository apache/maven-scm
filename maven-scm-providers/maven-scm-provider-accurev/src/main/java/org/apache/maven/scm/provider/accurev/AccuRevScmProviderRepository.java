package org.apache.maven.scm.provider.accurev;

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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.accurev.util.WorkspaceUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * 
 */
public class AccuRevScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    public static final String DEFAULT_TAG_FORMAT = "%s";

    private AccuRev accurev;

    private String streamName;

    private String projectPath;

    private String tagFormat = DEFAULT_TAG_FORMAT;

    private ScmLogger logger;

    public AccuRevScmProviderRepository()
    {
        super();
        // True is a more sensible default (ie for tck tests)
        // TODO raise jira so tck tests properly handle setPersist
        setPersistCheckout( true );

        setShouldUseExportForNonPersistentCheckout( true );
    }

    public String getTagFormat()
    {
        return tagFormat;
    }

    public void setTagFormat( String tagFormat )
    {
        if ( tagFormat == null || !tagFormat.contains( "%s" ) )
        {
            throw new IllegalArgumentException( "tagFormat must contain '%s' to be replaced" );
        }
        this.tagFormat = tagFormat;
    }

    public String getStreamName()
    {
        return streamName;
    }

    public void setStreamName( String streamName )
    {
        this.streamName = streamName;
    }

    public String getProjectPath()
    {
        return projectPath;
    }

    public void setProjectPath( String projectPath )
    {
        this.projectPath = projectPath;
        setCheckoutRelativePath( projectPath );
    }

    public AccuRev getAccuRev()
    {
        return this.accurev;
    }

    public void setAccuRev( AccuRev accurev )
    {
        this.accurev = accurev;
    }

    /**
     * @param info
     * @return true if info indicates a root of the workspace.
     */
    public boolean isWorkSpaceRoot( AccuRevInfo info )
    {
        String p = getProjectPath();
        return ( p != null && WorkspaceUtils.isSameFile( info.getBasedir(), new File( info.getTop(), p ) ) )
            || isWorkSpaceTop( info );
    }

    public boolean isWorkSpaceTop( AccuRevInfo info )
    {
        return info.isWorkSpaceTop();      

    }

    String tagToStream( String tagName )
    {
        return String.format( getTagFormat(), tagName );
    }

    String streamToTag( String streamName )
    {
        tagFormat = getTagFormat();
        // TODO - strictly we should quote either side of the %s
        String tagPatternString = tagToStream( "(.*)" );
        Pattern tagPattern = Pattern.compile( tagPatternString );

        Matcher tagMatcher = tagPattern.matcher( streamName );
        if ( tagMatcher.matches() )
        {
            return tagMatcher.group( 1 );
        }
        else
        {
            return streamName;
        }

    }

    public void setLogger( ScmLogger logger )
    {
        this.logger = logger;
    }

    // TODO raise JIRA to pull up these methods to ScmProviderRepository

    private String checkoutRelativePath;

    private boolean shouldUseExportForNonPersistentCheckout = true;

    /**
     * The relative path of the directory of the checked out project in comparison to the checkout directory, or an
     * empty String in case the checkout directory equals the project directory.
     * <p>
     * With most SCMs, this is just an empty String, meaning that the checkout directory equals the project directory.
     * But there are cases (e.g. ClearCase) where within the checkout directory, the directory structure of the SCM
     * system is repeated. E.g. if you check out the project "my/project" to "/some/dir", the project sources are
     * actually checked out to "some/dir/my/project". In this example, relativePathProjectDirectory would contain
     * "my/project".
     */
    public String getCheckoutRelativePath()
    {
        if ( this.checkoutRelativePath == null )
        {
            return "";
        }
        return this.checkoutRelativePath;
    }

    public void setCheckoutRelativePath( String checkoutRelativePath )
    {
        this.checkoutRelativePath = checkoutRelativePath;
    }

    /**
     * Relative project path for export
     * 
     * @return default same as {@link #getCheckoutRelativePath()}
     */
    public String getExportRelativePath()
    {
        return getCheckoutRelativePath();
    }

    /**
     * When checkout is not expected to be refreshed or committed, should export be used instead? Perforce, Clearcase
     * and AccuRev store their meta-data about file status within the server rather than files in the source tree. This
     * makes checkouts within checkouts (eg release:perform) difficult. Typically there is a way to do a lightweight
     * export instead which can be implemented as the "export" command. This is a hint to downstream applications that
     * "export" is available and should be used in preference to "checkout" in cases where "update" and "commit" are not
     * intended to be used. (ie release:perform)
     * 
     * @return false by default
     */
    public boolean shouldUseExportForNonPersistentCheckout()
    {
        return this.shouldUseExportForNonPersistentCheckout;
    }

    public void setShouldUseExportForNonPersistentCheckout( boolean shouldUseExportForNonPersistentCheckout )
    {
        this.shouldUseExportForNonPersistentCheckout = shouldUseExportForNonPersistentCheckout;
    }

    public String getDepotRelativeProjectPath()
    {
        return "/./" + ( projectPath == null ? "" : projectPath );
    }

    public AccuRevVersion getAccuRevVersion( ScmVersion scmVersion )
    {

        String tran = null;
        String basisStream = null;

        if ( scmVersion == null )
        {
            basisStream = getStreamName();
        }
        else
        {
            String name = StringUtils.clean( scmVersion.getName() );

            String[] versionComponents = name.split( "[/\\\\]", 2 );
            basisStream = versionComponents[0];
            if ( basisStream.length() == 0 )
            {
                // Use the default stream from the URL
                basisStream = getStreamName();
            }
            else
            {
                // name is a tag name - convert to a stream.
                basisStream = tagToStream( basisStream );
            }

            if ( versionComponents.length == 2 && versionComponents[1].length() > 0 )
            {
                tran = versionComponents[1];
            }
        }

        return new AccuRevVersion( basisStream, tran );
    }

    public String getSnapshotName( String tagName )
    {
        return tagToStream( tagName );
    }

    public String getRevision( String streamName, Date date )
    {
        return getRevision( streamName, AccuRev.ACCUREV_TIME_SPEC.format( date == null ? new Date() : date ) );
    }

    public String getRevision( String stream, long fromTranId )
    {
        return getRevision( stream, Long.toString( fromTranId ) );
    }

    public String getRevision( String streamName, String transaction )
    {
        return streamToTag( streamName ) + "/" + transaction;
    }

    public String getWorkSpaceRevision( String workspace )
        throws AccuRevException
    {
        return getRevision( workspace, Long.toString( getCurrentTransactionId( workspace ) ) );
    }

    public Transaction getDepotTransaction( String stream, String tranSpec )
        throws AccuRevException
    {

        if ( tranSpec == null )
        {
            tranSpec = "now";
        }

        List<Transaction> transactions = getAccuRev().history( stream, tranSpec, null, 1, true, true );

        if ( transactions == null || transactions.isEmpty() )
        {
            logger.warn( "Unable to find transaction for tranSpec=" + tranSpec );
            return null;
        }
        else
        {
            return transactions.get( 0 );
        }

    }

    public String getDepotTransactionId( String stream, String tranSpec )
        throws AccuRevException
    {
        Transaction t = getDepotTransaction( stream, tranSpec );

        return t == null ? tranSpec : Long.toString( t.getTranId() );
    }

    private long getCurrentTransactionId( String workSpaceName )
        throws AccuRevException
    {
        // AccuRev does not have a way to get at this workspace info by name.
        // So we have to do it the hard way...

        AccuRev accuRev = getAccuRev();

        Map<String, WorkSpace> workSpaces = accuRev.showWorkSpaces();

        WorkSpace workspace = workSpaces.get( workSpaceName );

        if ( workspace == null )
        {
            // Must be a reftree
            workSpaces = accuRev.showRefTrees();
            workspace = workSpaces.get( workSpaceName );
        }

        if ( workspace == null )
        {
            throw new AccuRevException( "Can't find workspace " + workSpaceName );
        }
        return workspace.getTransactionId();
    }

    public String toString()
    {
        StringBuilder buff = new StringBuilder( "AccuRevScmProviderRepository" );
        buff.append( " user=" );
        buff.append( getUser() );
        buff.append( " pass=" );
        buff.append( getPassword() == null ? "null" : StringUtils.repeat( "*", getPassword().length() ) );
        buff.append( " host=" );
        buff.append( getHost() );
        buff.append( " port=" );
        buff.append( getPort() );
        buff.append( " stream=" );
        buff.append( getStreamName() );
        buff.append( " projectPath=" );
        buff.append( getProjectPath() );

        return buff.toString();
    }

    public static String formatTimeSpec( Date when )
    {

        if ( when == null )
        {
            return "now";
        }

        return AccuRev.ACCUREV_TIME_SPEC.format( when );

    }

}
