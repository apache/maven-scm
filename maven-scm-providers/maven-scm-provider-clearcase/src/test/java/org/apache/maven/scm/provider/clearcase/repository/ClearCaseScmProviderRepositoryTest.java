package org.apache.maven.scm.provider.clearcase.repository;

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

import junit.framework.TestCase;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 */
public class ClearCaseScmProviderRepositoryTest
    extends TestCase
{
    public ClearCaseScmProviderRepositoryTest()
    {
    }

    public void testParsingUrlWithPipe()
        throws ScmRepositoryException, IOException
    {
        String viewName = "my_module_view";
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String url = viewName + "|" + configSpecPath;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( url );
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertEquals( viewName, repository.getViewName( "bla" ) );
    }

    public void testParsingUrlWithColon()
        throws ScmRepositoryException
    {
        String viewName = "my_module_view";
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String url = viewName + ":" + configSpecPath;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( url );
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertEquals( viewName, repository.getViewName( "bla" ) );
    }

    public void testParsingUrlWithoutViewName()
        throws ScmRepositoryException
    {
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String url = configSpecPath;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( url );
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertNotNull( repository.getViewName( "15" ) );
        assertTrue( repository.getViewName( "15" ).indexOf( "15" ) != -1 );
    }
}
