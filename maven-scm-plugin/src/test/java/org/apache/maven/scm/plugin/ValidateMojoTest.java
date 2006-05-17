package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ValidateMojoTest
    extends AbstractMojoTestCase
{
    public void testValidateWithoutScmUrl()
        throws Exception
    {
        ValidateMojo mojo = (ValidateMojo) lookupMojo( "validate", getTestFile(
            "src/test/resources/mojos/validate/validateWithoutScmUrl.xml" ) );
        mojo.execute();
    }

    public void testValidateWithValidScmUrls()
        throws Exception
    {
        ValidateMojo mojo = (ValidateMojo) lookupMojo( "validate", getTestFile(
            "src/test/resources/mojos/validate/validateWithValidScmUrls.xml" ) );
        mojo.execute();
    }

    public void testValidateWithInvalidScmUrls()
        throws Exception
    {
        ValidateMojo mojo = (ValidateMojo) lookupMojo( "validate", getTestFile(
            "src/test/resources/mojos/validate/validateWithInvalidScmUrls.xml" ) );
        try
        {
            mojo.execute();

            fail( "mojo execution must fail." );
        }
        catch ( MojoExecutionException e )
        {
            assertTrue( true );
        }
    }
}
