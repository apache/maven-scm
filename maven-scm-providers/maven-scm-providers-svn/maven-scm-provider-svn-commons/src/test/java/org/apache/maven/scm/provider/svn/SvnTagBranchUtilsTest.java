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
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnTagBranchUtilsTest
    extends ScmTestCase
{
    // ----------------------------------------------------------------------
    // appendPath
    // ----------------------------------------------------------------------

    @Test
    public void testAppendPath()
        throws Exception
    {
        assertEquals( "http://foo.com/svn/myproject/tags/foo",
                      SvnTagBranchUtils.appendPath( "http://foo.com/svn", "myproject/tags/foo" ) );
    }

    @Test
    public void testAppendPathNullAddlPath()
        throws Exception
    {
        assertEquals( "http://foo.com/svn", SvnTagBranchUtils.appendPath( "http://foo.com/svn", null ) );
    }

    @Test
    public void testAppendPathNullAddlTrailingSlash()
        throws Exception
    {
        assertEquals( "http://foo.com/svn", SvnTagBranchUtils.appendPath( "http://foo.com/svn/", null ) );
    }

    @Test
    public void testAppendPathTrailingSlash()
        throws Exception
    {
        assertEquals( "http://foo.com/svn/myproject/tags/foo",
                      SvnTagBranchUtils.appendPath( "http://foo.com/svn/", "myproject/tags/foo" ) );
    }

    @Test
    public void testAppendPathLeadingAndTrailingSlash()
        throws Exception
    {
        assertEquals( "http://foo.com/svn/myproject/tags/foo",
                      SvnTagBranchUtils.appendPath( "http://foo.com/svn/", "/myproject/tags/foo" ) );
    }

    // ----------------------------------------------------------------------
    // resolveTagBase
    // ----------------------------------------------------------------------

    @Test
    public void testResolveTagBase()
    {
        assertEquals( "http://foo.com/svn/myproject/tags",
                      SvnTagBranchUtils.resolveTagBase( "http://foo.com/svn/myproject/trunk" ) );
        assertEquals( "http://foo.com/svn/myproject/tags",
                      SvnTagBranchUtils.resolveTagBase( "http://foo.com/svn/myproject/trunk/" ) );
    }

    // ----------------------------------------------------------------------
    // getProjectRoot
    // ----------------------------------------------------------------------

    @Test
    public void testGetProjectRootTagBranchTrunk()
        throws Exception
    {
        // All of these should equate to the same project root
        String[] paths = new String[]{"scm:svn:http://foo.com/svn/tags/my-tag", "scm:svn:http://foo.com/svn/tags",
            "scm:svn:http://foo.com/svn/branches/my-branch", "scm:svn:http://foo.com/svn/branches",
            "scm:svn:http://foo.com/svn/trunk", "scm:svn:http://foo.com/svn/trunk/some/path/to/some/file"};

        for ( int i = 0; i < paths.length; i++ )
        {
            testGetProjectRoot( paths[i], "http://foo.com/svn" );
        }
    }

    @Test
    public void testGetProjectRootNoRootSpecifier()
        throws Exception
    {
        testGetProjectRoot( "scm:svn:http://foo.com/svn/", "http://foo.com/svn" );

        testGetProjectRoot( "scm:svn:http://foo.com/svn", "http://foo.com/svn" );

        testGetProjectRoot( "scm:svn:http://foo.com/svn/ntags", "http://foo.com/svn/ntags" );

        testGetProjectRoot( "scm:svn:http://foo.com/svn/nbranches", "http://foo.com/svn/nbranches" );
    }

    @Test
    public void testGetProjectRootLooksLikeRootSpecifier()
        throws Exception
    {
        testGetProjectRoot( "scm:svn:http://foo.com/svn/tagst", "http://foo.com/svn/tagst" );

        testGetProjectRoot( "scm:svn:http://foo.com/svn/tagst/tags", "http://foo.com/svn/tagst" );

        testGetProjectRoot( "scm:svn:http://foo.com/svn/branchess", "http://foo.com/svn/branchess" );

    }

    @Test
    public void testGetProjectRootDoubleProjectRoots()
        throws Exception
    {
        testGetProjectRoot( "scm:svn:http://foo.com/svn/tags/my-tag/tags/another-tag/",
                            "http://foo.com/svn/tags/my-tag" );
        testGetProjectRoot( "scm:svn:http://foo.com/svn/trunk/a_directory/trunk/",
                            "http://foo.com/svn/trunk/a_directory" );
    }

    // ----------------------------------------------------------------------
    // resolveTagUrl
    // ----------------------------------------------------------------------

    @Test
    public void testResolveTagRelative()
        throws Exception
    {
        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "my-tag", "http://foo.com/svn/tags/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/trunk", "my-tag", "http://foo.com/svn/tags/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/trunk/", "my-tag", "http://foo.com/svn/tags/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/branches", "my-tag", "http://foo.com/svn/tags/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/tags", "my-tag", "http://foo.com/svn/tags/my-tag" );
    }

    @Test
    public void testResolveTagAbsolute()
        throws Exception
    {
        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "http://foo.com/svn/branches/my-tag",
                           "http://foo.com/svn/branches/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "file://C://svn/some/crazy/path/my-tag",
                           "file://C://svn/some/crazy/path/my-tag" );

    }

    @Test
    public void testResolveTagWithSlashes()
        throws Exception
    {
        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "/my-tag/", "http://foo.com/svn/tags/my-tag" );

        testResolveBranchUrl( "scm:svn:http://foo.com/svn/", "/my-branch/", "http://foo.com/svn/branches/my-branch" );

        testResolveBranchUrl( "scm:svn:http://foo.com/svn/", "http://foo.com/svn/myproject/branches/", "/my-branch/",
                              "http://foo.com/svn/myproject/branches/my-branch" );
    }

    @Test
    public void testResolveTagWithTagOverwritingBase()
        throws Exception
    {
        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "branches/my-tag", "http://foo.com/svn/branches/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "tags/my-tag", "http://foo.com/svn/tags/my-tag" );

        // Not sure why you would ever specify a tag of /trunk/foo,
        // but create the test case to assure consistent behavior in the future
        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "trunk/my-tag", "http://foo.com/svn/trunk/my-tag" );

        testResolveTagUrl( "scm:svn:svn+ssh://foo.com/svn/trunk/my_path/to/my_dir", "my-tag",
                           "svn+ssh://foo.com/svn/tags/my-tag" );

        testResolveTagUrl( "scm:svn:svn+ssh://foo.com/svn/trunk/my_path/to/my_dir/trunk/mydir", "my-tag",
                           "svn+ssh://foo.com/svn/trunk/my_path/to/my_dir/tags/my-tag" );
        testResolveTagUrl( "scm:svn:file://localhost/C:/mydir/myproject/trunk/my-module/target/scm-src/trunk", "my-tag",
                           "file://localhost/C:/mydir/myproject/trunk/my-module/target/scm-src/tags/my-tag" );
    }

    @Test
    public void testResolveTagWithTagBaseSpecified()
        throws Exception
    {
        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "../tags", "my-tag", "../tags/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "http://foo.com/svn/non-standard/tag/dir/", "my-tag",
                           "http://foo.com/svn/non-standard/tag/dir/my-tag" );
    }

    @Test
    public void testResolveTagLooksLikeOverwriteTagBase()
        throws Exception
    {
        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "tagst/my-tag", "http://foo.com/svn/tags/tagst/my-tag" );

        testResolveTagUrl( "scm:svn:http://foo.com/svn/", "metatags/my-tag",
                           "http://foo.com/svn/tags/metatags/my-tag" );
    }

    @Test
    public void testResolveBranchSimple()
        throws Exception
    {
        testResolveBranchUrl( "scm:svn:http://foo.com/svn/", "my-branch", "http://foo.com/svn/branches/my-branch" );

        testResolveBranchUrl( "scm:svn:svn+ssh://foo.com/svn/trunk", "my-branch",
                              "svn+ssh://foo.com/svn/branches/my-branch" );

        testResolveBranchUrl( "scm:svn:svn+ssh://foo.com/svn/trunk/my_path/to/my_dir", "my-branch",
                              "svn+ssh://foo.com/svn/branches/my-branch" );

        testResolveBranchUrl( "scm:svn:http://foo.com/svn/trunk", "branches/my-branch",
                              "http://foo.com/svn/branches/my-branch" );

        testResolveBranchUrl( "scm:svn:http://foo.com/svn/", "subbranches/my-branch",
                              "http://foo.com/svn/branches/subbranches/my-branch" );

    }

    @Test
    public void testResolveBranchTagBase()
        throws Exception
    {
        testResolveBranchUrl( "scm:svn:http://foo.com/svn/", "../branches", "my-branch", "../branches/my-branch" );

        testResolveBranchUrl( "scm:svn:http://foo.com/svn/", "http://foo.com/svn/non-standard/branch/dir", "my-branch",
                              "http://foo.com/svn/non-standard/branch/dir/my-branch" );
    }

    // ----------------------------------------------------------------------
    // revisionArgument
    // ----------------------------------------------------------------------

    @Test
    public void testIsRevisionArgumentSimple()
    {
        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "12345" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "hEaD" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "bAsE" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "cOmMiTtEd" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "pReV" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "{2005-1-1}" ) ) );

    }

    @Test
    public void testIsRevisionArgumentRange()
    {
        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "12345:12345" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "1:2" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "hEaD:bAsE" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "BaSe:CoMmItTeD" ) ) );

        assertTrue( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "{2004-1-1}:{2005-1-1}" ) ) );

        assertFalse( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( "BASE:" ) ) );
        assertFalse( SvnTagBranchUtils.isRevisionSpecifier( new ScmRevision( ":BASE" ) ) );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private SvnScmProviderRepository getSvnRepository( String scmUrl )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        return (SvnScmProviderRepository) repository.getProviderRepository();
    }

    private void testGetProjectRoot( String scmUrl, String expected )
        throws Exception
    {
        assertEquals( expected, SvnTagBranchUtils.getProjectRoot( getSvnRepository( scmUrl ).getUrl() ) );
    }

    private void testResolveTagUrl( String scmUrl, String tag, String expected )
        throws Exception
    {
        testResolveTagUrl( scmUrl, null, tag, expected );
    }

    private void testResolveTagUrl( String scmUrl, String tagBase, String tag, String expected )
        throws Exception
    {
        SvnScmProviderRepository repository = getSvnRepository( scmUrl );

        if ( tagBase != null )
        {
            repository.setTagBase( tagBase );
        }

        if ( tagBase != null )
        {
            assertEquals( repository.getTagBase(), tagBase );
        }
        else
        {
            assertEquals( repository.getTagBase(), SvnTagBranchUtils.resolveTagBase( repository.getUrl() ) );
        }

        assertEquals( expected, SvnTagBranchUtils.resolveTagUrl( repository, new ScmTag( tag ) ) );
    }

    private void testResolveBranchUrl( String scmUrl, String branch, String expected )
        throws Exception
    {
        testResolveBranchUrl( scmUrl, null, branch, expected );
    }

    private void testResolveBranchUrl( String scmUrl, String branchBase, String branch, String expected )
        throws Exception
    {
        SvnScmProviderRepository repository = getSvnRepository( scmUrl );
        if ( branchBase != null )
        {
            repository.setBranchBase( branchBase );
        }

        if ( branchBase != null )
        {
            assertEquals( repository.getBranchBase(), branchBase );
        }
        else
        {
            assertEquals( repository.getBranchBase(), SvnTagBranchUtils.resolveBranchBase( repository.getUrl() ) );
        }

        assertEquals( expected, SvnTagBranchUtils.resolveBranchUrl( repository, new ScmBranch( branch ) ) );
    }

}
