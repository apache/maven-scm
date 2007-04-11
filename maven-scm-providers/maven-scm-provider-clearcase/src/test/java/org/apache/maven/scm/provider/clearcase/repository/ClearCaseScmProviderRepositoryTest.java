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
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.providers.clearcase.settings.Settings;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @author <a href="mailto:antoine.veret@gmail.com">Antoine Veret</a>
 */
public class ClearCaseScmProviderRepositoryTest
    extends TestCase
{
    public void testParsingUrlWithPipe()
        throws ScmRepositoryException, IOException
    {
        Settings settings = new Settings();
        String viewName = "my_module_view";
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String url = viewName + "|" + configSpecPath;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings);
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertEquals( viewName, repository.getViewName( "bla" ) );
        assertNull( repository.getLoadDirectory() );
        assertNull(repository.getStreamName());
        assertNull(repository.getVobName());
    }

    public void testParsingUrlWithColon()
        throws ScmRepositoryException
    {
        Settings settings = new Settings();
        String viewName = "my_module_view";
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String url = viewName + ":" + configSpecPath;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertEquals( viewName, repository.getViewName( "bla" ) );
        assertNull( repository.getLoadDirectory() );
        assertNull(repository.getStreamName());
        assertNull(repository.getVobName());
    }

    public void testParsingUrlWithoutViewName()
        throws ScmRepositoryException
    {
        Settings settings = new Settings();
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String url = configSpecPath;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertNotNull( repository.getViewName( "15" ) );
        assertTrue( repository.getViewName( "15" ).indexOf( "15" ) != -1 );
        assertNull( repository.getLoadDirectory() );
        assertNull(repository.getStreamName());
        assertNull(repository.getVobName());
    }

    public void testAutoConfigSpecWithColon()
        throws Exception
    {
        Settings settings = new Settings();
        String url = "my_view_name:load /VOB/some/dir";
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertNull( repository.getConfigSpec() );
        assertTrue( repository.isAutoConfigSpec() );
        assertEquals( "my_view_name", repository.getViewName( "bla" ) );
        assertEquals( "/VOB/some/dir", repository.getLoadDirectory() );
        assertNull(repository.getStreamName());
        assertNull(repository.getVobName());
    }

    public void testAutoConfigSpecWithPipe()
        throws Exception
    {
        Settings settings = new Settings();
        String url = "my_view_name|load /VOB/some/dir";
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertNull( repository.getConfigSpec() );
        assertTrue( repository.isAutoConfigSpec() );
        assertEquals( "my_view_name", repository.getViewName( "bla" ) );
        assertEquals( "/VOB/some/dir", repository.getLoadDirectory() );
        assertNull(repository.getStreamName());
        assertNull(repository.getVobName());
    }

    public void testAutoConfigSpecWithoutViewName()
        throws Exception
    {
        Settings settings = new Settings();
        String url = "load /VOB/some/dir";
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertNull( repository.getConfigSpec() );
        assertTrue( repository.isAutoConfigSpec() );
        assertNotNull( repository.getViewName( "15" ) );
        assertTrue( repository.getViewName( "15" ).indexOf( "15" ) != -1 );
        assertEquals( "/VOB/some/dir", repository.getLoadDirectory() );
        assertNull(repository.getStreamName());
        assertNull(repository.getVobName());
    }    
    
    public void testParsingUrlClearCaseUCMWithPipe()
        throws ScmRepositoryException
    {
        Settings settings = new Settings();
        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_UCM);
        String delimiter = "|";
        String viewName = "my_module_view";
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String vobName = "pvob_alliance";
        String streamName = "INT_COMMUN_V1.0";
        String url = viewName + delimiter + configSpecPath + delimiter + vobName + delimiter + streamName;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertEquals( viewName, repository.getViewName( "bla" ) );
        assertNull( repository.getLoadDirectory() );
        assertEquals(streamName, repository.getStreamName());
        assertEquals(vobName, repository.getVobName());
    }
    
    public void testParsingUrlClearCaseUCMWithoutViewnameWithColon()
        throws ScmRepositoryException
    {
        Settings settings = new Settings();
        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_UCM);
        String delimiter = ":";
        String configSpecPath = "//myserver/ClearCase/ConfigSpecs/mymodule.txt";
        String vobName = "pvob_alliance";
        String streamName = "INT_COMMUN_V1.0";
        String url = configSpecPath + delimiter + vobName + delimiter + streamName;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertEquals( new File( configSpecPath ).getPath(), repository.getConfigSpec().getPath() );
        assertNotNull( repository.getViewName( "bla" ) );
        assertNull( repository.getLoadDirectory() );
        assertEquals(streamName, repository.getStreamName());
        assertEquals(vobName, repository.getVobName());
    }
    
    public void testParsingUrlClearCaseUCMAutoConfig()
        throws ScmRepositoryException
    {
        Settings settings = new Settings();
        settings.setClearcaseType(ClearCaseScmProviderRepository.CLEARCASE_UCM);
        String delimiter = "|";
        String loadPath = "/ua/sub/project";
        String vobName = "pvob_alliance";
        String streamName = "INT_COMMUN_V1.0";
        String url = "load " + loadPath + delimiter + vobName + delimiter + streamName;
        ClearCaseScmProviderRepository repository = new ClearCaseScmProviderRepository( new DefaultLog(), url, settings );
        assertNull( repository.getConfigSpec() );
        assertTrue( repository.isAutoConfigSpec() );
        assertNotNull( repository.getViewName( "bla" ) );
        assertEquals( loadPath, repository.getLoadDirectory() );
        assertEquals(streamName, repository.getStreamName());
        assertEquals(vobName, repository.getVobName());
    }
}
