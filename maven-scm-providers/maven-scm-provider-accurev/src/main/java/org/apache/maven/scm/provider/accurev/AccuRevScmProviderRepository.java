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

import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.codehaus.plexus.util.StringUtils;

public class AccuRevScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    public AccuRevScmProviderRepository()
    {
        super();
        // True is a more sensible default (ie for tck tests)
        // TODO raise jira so tck tests properly handle setPersist
        setPersistCheckout( true );

        setShouldUseExportForNonPersistentCheckout( true );
    }

    /**
     * System property for prefixing tags
     */
    public static final String TAG_PREFIX = "accuRevTagPrefix";

    private String streamName;

    private String projectPath;

    private AccuRev accurev;

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

        return ( ( getProjectPath() != null && info.getBasedir().equals( new File( info.getTop(), getProjectPath() ) ) ) || isWorkSpaceTop( info ) );
    }

    public boolean isWorkSpaceTop( AccuRevInfo info )
    {
        return info.isWorkSpaceTop();

    }

    public String getExtendedTagName( String tagName )
    {
        return System.getProperty( TAG_PREFIX, "" ) + tagName;
    }

    // TODO raise JIRA to pull up these methods to ScmProviderRepository

    private String checkoutRelativePath;

    private boolean shouldUseExportForNonPersistentCheckout = true;

    public static final int DEFAULT_PORT = 5050;

    /**
     * The relative path of the directory of the checked out project in comparison to the checkout
     * directory, or an empty String in case the checkout directory equals the project directory.
     * <p/>
     * With most SCMs, this is just an empty String, meaning that the checkout directory equals the
     * project directory. But there are cases (e.g. ClearCase) where within the checkout directory,
     * the directory structure of the SCM system is repeated. E.g. if you check out the project
     * "my/project" to "/some/dir", the project sources are actually checked out to
     * "some/dir/my/project". In this example, relativePathProjectDirectory would contain
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
     * When checkout is not expected to be refreshed or committed, should export be used instead?
     * Perforce, Clearcase and AccuRev store their meta-data about file status within the server
     * rather than files in the source tree. This makes checkouts within checkouts (eg
     * release:perform) difficult. Typically there is a way to do a lightweight export instead which
     * can be implemented as the "export" command. This is a hint to downstream applications that
     * "export" is available and should be used in preference to "checkout" in cases where "update"
     * and "commit" are not intended to be used. (ie release:perform)
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

    public String toString()
    {
        StringBuffer buff = new StringBuffer( "AccuRevScmProviderRepository" );
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

    public AccuRevVersion getAccuRevVersion( ScmVersion scmVersion )
    {

        String tran = null;
        String basisStream;

        if ( scmVersion == null )
        {
            basisStream = getStreamName();
        }
        else
        {
            String name = scmVersion.getName();

            String[] versionComponents = name.split( "[/\\\\]", 2 );
            basisStream = versionComponents[0];
            if ( basisStream.length() == 0 )
            {
                // Use the default stream from the URL
                basisStream = getStreamName();
            }
            else
            {
                // Need to add the tag prefix
                basisStream = getExtendedTagName( basisStream );
            }

            if ( versionComponents.length == 2 && versionComponents[1].length() > 0 )
            {
                tran = versionComponents[1];
            }
        }

        return new AccuRevVersion( basisStream, tran );
    }

    public String getDepotRelativeProjectPath()
    {
        return "/./" + ( projectPath == null ? "" : projectPath );
    }

}
