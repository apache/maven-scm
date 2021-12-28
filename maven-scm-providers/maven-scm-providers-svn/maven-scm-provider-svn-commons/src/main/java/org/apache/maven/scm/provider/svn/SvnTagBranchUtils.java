package org.apache.maven.scm.provider.svn;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 */
public final class SvnTagBranchUtils
{

    private SvnTagBranchUtils()
    {
    }

    public static final String[] REVISION_SPECIFIERS = new String[]{"HEAD", "BASE", "COMMITTED", "PREV"};

    public static final String SVN_TRUNK = "trunk";

    public static final String SVN_BRANCHES = "branches";

    public static final String SVN_TAGS = "tags";

    public static final String[] SVN_BASE_DIRS = new String[]{SVN_TRUNK, SVN_BRANCHES, SVN_TAGS};

    /**
     * Simple helper function to concatenate two paths together with a "/".
     * Handles trailing / on basePath.
     * Returns no trailing "/" if the addlPath is null
     */
    static String appendPath( String basePath, String addlPath )
    {
        basePath = StringUtils.stripEnd( basePath, "/" );

        if ( StringUtils.isEmpty( addlPath ) )
        {
            return basePath;
        }
        else
        {
            return basePath + "/" + StringUtils.stripStart( addlPath, "/" );
        }
    }

    /**
     * Returns the project root for the given repository url,
     * where "project root" is the root of the /trunk, /branches, /tags
     * directories
     *
     * @param repoPath Repository path/url to be searched
     * @return TODO
     */
    public static String getProjectRoot( String repoPath )
    {
        for ( int i = 0; i < SVN_BASE_DIRS.length; i++ )
        {
            String base = "/" + SVN_BASE_DIRS[i];
            int pos = repoPath.lastIndexOf( base + "/" );
            if ( repoPath.endsWith( base ) )
            {
                return repoPath.substring( 0, repoPath.length() - base.length() );
            }
            else if ( pos >= 0 )
            {
                return repoPath.substring( 0, pos );
            }
        }

        // At this point we were unable to locate the project root of this url
        // so assume that the repository url specified is the project root
        return appendPath( repoPath, null );
    }

    public static String resolveTagBase( SvnScmProviderRepository repository )
    {
        return resolveTagBase( repository.getUrl() );
    }

    public static String resolveTagBase( String repositoryUrl )
    {
        return appendPath( getProjectRoot( repositoryUrl ), SVN_TAGS );
    }

    public static String resolveBranchBase( SvnScmProviderRepository repository )
    {
        return resolveBranchBase( repository.getUrl() );
    }

    public static String resolveBranchBase( String repositoryUrl )
    {
        return appendPath( getProjectRoot( repositoryUrl ), SVN_BRANCHES );
    }

    /**
     * Resolves a tag to a repository url.
     * By supplying the repository to this function (rather than calling {@link #resolveTagUrl(String,ScmTag)}
     * the resolution can use the repository's tagBase to override the default tag location.
     *
     * @param repository the repository to use as a base for tag resolution
     * @param tag        tag name
     * @return TODO
     * @see #resolveUrl(String,String,String,ScmBranch)
     */
    public static String resolveTagUrl( SvnScmProviderRepository repository, ScmTag tag )
    {
        return resolveUrl( repository.getUrl(), repository.getTagBase(), SVN_TAGS, tag );
    }

    /**
     * Resolves a tag to a repository url.
     * Will not use the {@link SvnScmProviderRepository#getTagBase()} during resolution.
     *
     * @param repositoryUrl string url for the repository
     * @param tag           tag name
     * @return TODO
     * @see #resolveUrl(String,String,String,ScmBranch)
     */
    public static String resolveTagUrl( String repositoryUrl, ScmTag tag )
    {
        return resolveUrl( repositoryUrl, null, SVN_TAGS, tag );
    }

    /**
     * Resolves a branch name to a repository url.
     * By supplying the repository to this function (rather than calling {@link #resolveBranchUrl(String,ScmBranch)}
     * the resolution can use the repository's tagBase to override the default tag location.
     *
     * @param repository the repository to use as a base for tag resolution
     * @param branch     tag name
     * @return TODO
     * @see #resolveUrl(String,String,String,ScmBranch)
     */
    public static String resolveBranchUrl( SvnScmProviderRepository repository, ScmBranch branch )
    {
        return resolveUrl( repository.getUrl(), repository.getBranchBase(), SVN_BRANCHES, branch );
    }

    /**
     * Resolves a branch name to a repository url.
     * Will not use the {@link SvnScmProviderRepository#getTagBase()} during resolution.
     *
     * @param repositoryUrl string url for the repository
     * @param branch        branch name
     * @return TODO
     * @see #resolveUrl(String,String,String,ScmBranch)
     */
    public static String resolveBranchUrl( String repositoryUrl, ScmBranch branch )
    {
        return resolveUrl( repositoryUrl, resolveBranchBase( repositoryUrl ), SVN_BRANCHES, branch );
    }

    private static String addSuffix( String baseString, String suffix )
    {
        return ( suffix != null ) ? baseString + suffix : baseString;
    }


    /**
     * Resolves a tag or branch name to a repository url.<br>
     * If the <code>branchTagName</code> is an absolute URL, that value is returned.
     * (i.e. http://foo.com/svn/myproject/tags/my-tag)<br>
     * <p>
     * If the repository has a {@link SvnScmProviderRepository#getTagBase()} specified,
     * the tag is simply appended to the tagBase value. Note that at this time, we are using
     * the tagBase as a base for both branches and tags.<br>
     * <p>
     * If the <code>branchTagName</code> contains a branch/tag specifier (i.e. "/branches", "/tags", "/trunk"),
     * the <code>branchTagName</code> is appended to the <code>projectRoot</code> without adding the subdir.<br>
     * Else, the result is in the format of <code>projectRoot/subdir/branchTagName</code> directory.<br>
     *
     * @param repositoryUrl string url for the repository
     * @param tagBase       tagBase to use.
     * @param subdir        Subdirectory to append to the project root
     *                      (for branching use "branches", tags use "tags")
     * @param branchTag     Name of the actual branch or tag. Can be an absolute url, simple tag/branch name,
     *                      or even contain a relative path to the root like "branches/my-branch"
     * @return TODO
     */
    public static String resolveUrl( String repositoryUrl, String tagBase, String subdir, ScmBranch branchTag )
    {
        String branchTagName = branchTag.getName();
        String projectRoot = getProjectRoot( repositoryUrl );
        branchTagName = StringUtils.strip( branchTagName, "/" );

        if ( StringUtils.isEmpty( branchTagName ) )
        {
            return null;
        }

        // Look for a query string as in ViewSVN urls
        String queryString = null;
        if ( repositoryUrl.indexOf( '?' ) >= 0 )
        {
            queryString = repositoryUrl.substring( repositoryUrl.indexOf( '?' ) );
            // if repositoryUrl contains a query string, remove it from repositoryUrlRoot; will be re-appended later
            projectRoot = StringUtils.replace( projectRoot, queryString, "" );
        }

        if ( branchTagName.indexOf( "://" ) >= 0 )
        {
            // branch/tag is already an absolute url so just return it.
            return branchTagName;
        }

        // User has a tagBase specified so just return the name appended to the tagBase
        if ( StringUtils.isNotEmpty( tagBase ) && !tagBase.equals( resolveTagBase( repositoryUrl ) )
            && !tagBase.equals( resolveBranchBase( repositoryUrl ) ) )
        {
            return appendPath( tagBase, branchTagName );
        }

        // Look for any "branches/" or "tags/" specifiers in the branchTagName. If one occurs,
        // don't append the subdir to the projectRoot when appending the name
        for ( int i = 0; i < SVN_BASE_DIRS.length; i++ )
        {
            if ( branchTagName.startsWith( SVN_BASE_DIRS[i] + "/" ) )
            {
                return addSuffix( appendPath( projectRoot, branchTagName ), queryString );
            }
        }

        return addSuffix( appendPath( appendPath( projectRoot, subdir ), branchTagName ), queryString );
    }

    /* Helper function that does the checking for {@link #isRevisionSpecifier}
     */
    private static boolean checkRevisionArg( String arg )
    {
        if ( StringUtils.isNumeric( arg ) || ( arg.startsWith( "{" ) && arg.endsWith( "}" ) ) )
        {
            return true;
        }

        for ( int i = 0; i < REVISION_SPECIFIERS.length; i++ )
        {
            if ( REVISION_SPECIFIERS[i].equalsIgnoreCase( arg ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the supplied tag refers to an actual revision or
     * is specifying a tag/branch url in the repository.
     * According to the subversion documentation, the following are valid revision specifiers:
     * NUMBER       revision number
     * "{" DATE "}" revision at start of the date
     * "HEAD"       latest in repository
     * "BASE"       base rev of item's working copy
     * "COMMITTED"  last commit at or before BASE
     * "PREV"
     * <p>
     * For command such as diff, the revision argument can be in the format of:
     * IDENTIFIER:IDENTIFIER   where IDENTIFIER is one of the args listed above
     */
    public static boolean isRevisionSpecifier( ScmVersion version )
    {
        if ( version == null )
        {
            return false;
        }

        String versionName = version.getName();

        if ( StringUtils.isEmpty( versionName ) )
        {
            return false;
        }

        if ( checkRevisionArg( versionName ) )
        {
            return true;
        }

        String[] parts = StringUtils.split( versionName, ":" );
        if ( parts.length == 2 && StringUtils.isNotEmpty( parts[0] ) && StringUtils.isNotEmpty( parts[1] ) )
        {
            return checkRevisionArg( parts[0] ) && checkRevisionArg( parts[1] );
        }

        return false;
    }
}
