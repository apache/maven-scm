package org.apache.maven.scm.provider.local.command.checkout;

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

import java.io.File;

import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class LocalCheckOutCommandTckTest
    extends CheckOutCommandTckTest
{
    private String module = "check-out";

    public String getScmUrl()
        throws Exception
    {
        return "scm:local|" + getRepositoryRoot().getAbsolutePath() + "|" + module;
    }

    public void initRepo()
		throws Exception
	{
        File root = new File( getRepositoryRoot() + "/" + module );

        makeFile( root, "/pom.xml" );

        makeFile( root, "/readme.txt" );

        makeFile( root, "/src/main/java/Application.java" );

        makeFile( root, "/src/test/java/Test.java" );

        makeDirectory( root, "/src/test/resources" );
    }
}
