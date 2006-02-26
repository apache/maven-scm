package org.apache.maven.scm.provider.svn.svnjava.command;

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

import org.apache.maven.scm.provider.svn.svnjava.SvnJavaTestUtils;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;

/**
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 */
public class SvnJavaTckTestCommand
{
    private final String name;

    public SvnJavaTckTestCommand( Class clazz )
    {
        super();
        this.name = getSimpleName( clazz );
    }

    // Returns the name of the class without the package.
    protected static String getSimpleName( Class clazz )
    {
        String simpleName = clazz.getName();
        return simpleName.substring( simpleName.lastIndexOf( "." ) + 1 ); // strip the package name
    }

    public String getScmUrl()
        throws Exception
    {
        return "scm:svn:" + SvnJavaTestUtils.getBaseURL() + "/tck-" + name + "/trunk";
    }

    public void initRepo()
        throws Exception
    {
        SvnJavaTestUtils.initializeRepository( new File( SvnJavaTestUtils.getSvnRoot(), "tck-" + name ),
                                               PlexusTestCase.getTestFile( "src/test/resources/tck/tck.dump" ) );
    }
}
