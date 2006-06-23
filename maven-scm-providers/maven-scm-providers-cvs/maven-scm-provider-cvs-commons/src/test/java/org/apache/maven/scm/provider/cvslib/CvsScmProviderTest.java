package org.apache.maven.scm.provider.cvslib;

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

import junit.framework.TestCase;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.cvslib.repository.TestCvsScmProvider;

/**
 * @author <a href="richardv@mxtelecom.com">Richard van der Hoff</a>
 * @version $Id$
 */
public class CvsScmProviderTest
    extends TestCase
{
    ScmProvider cvsProvider;

    public void setUp()
    {
        cvsProvider = new TestCvsScmProvider();
    }

    public void testGetScmSpecificFilename()
    {
        assertEquals( "CVS", cvsProvider.getScmSpecificFilename() );
    }

    /**
     * Tests for validateTagName() and sanitizeTagName()
     */
    public void testTagValidation()
    {
        /* check the two corner-cases fail */
        testTag( "HEAD", false );
        testTag( "BASE", false );

        /* check things which start with non-alphanumerics */
        testTag( "-abcd", false );
        testTag( "1234abcd", false );
        testTag( "", false );

        /* check a selection of correctable tags */
        testCorrectableTag( "abc-1.0.2", "abc-1_0_2" );
        testCorrectableTag( "abc\0def ghi\u00ff", "abc_def_ghi_" );

        /* check a selection of valid tags */
        testTag( "ABCD", true );
        testTag( "a", true );
        testTag( "abc-1_0_2", true );
    }

    /**
     * test a tag verifies correctly
     *
     * @param tag   tag to check
     * @param valid true if this tag should be valid
     */
    protected void testTag( String tag, boolean valid )
    {
        assertEquals( "Check tag '" + tag + "'", valid, cvsProvider.validateTagName( tag ) );
    }

    /**
     * test a tag which is invalid but can be corrected
     *
     * @param badtag  tag to test
     * @param goodtag what tag should be corrected to
     */
    protected void testCorrectableTag( String badtag, String goodtag )
    {
        testTag( badtag, false );
        assertEquals( goodtag, cvsProvider.sanitizeTagName( badtag ) );
    }
}
