package org.apache.maven.scm.provider.svn.command.checkout;

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

import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;

import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnCheckOutCommandTckTest
    extends CheckOutCommandTckTest
{
    public String getScmUrl()
        throws Exception
    {
        String repositoryRoot = getRepositoryRoot().getAbsolutePath();

        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
        {
            repositoryRoot = "/" + StringUtils.replace( repositoryRoot, "\\", "/" );
        }

        return "scm:svn:file://" + repositoryRoot;
    }

    public void initRepo()
        throws Exception
    {
        SvnScmTestUtils.initializeRepository( getRepositoryRoot(), getTestFile( "src/test/resources/tck/checkout.dump" ) );
    }
}
