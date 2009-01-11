package org.apache.maven.scm.plugin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * @version $Id: ExportMojoTest.java 687713 2008-08-21 11:12:33Z vsiveton $
 */
public class ExportMojoTest
    extends AbstractMojoTestCase
{
    File exportDir;

    File repository;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        exportDir = getTestFile( "target/export" );

        repository = getTestFile( "target/repository" );

        FileUtils.forceDelete( exportDir );
    }

    public void testExport()
        throws Exception
    {
        SvnScmTestUtils.initializeRepository( repository );

        ExportMojo mojo = (ExportMojo) lookupMojo( "export", getTestFile( "src/test/resources/mojos/export/export.xml" ) );

        mojo.setExportDirectory( exportDir.getAbsoluteFile() );

        mojo.execute();

        assertTrue( exportDir.listFiles().length > 0  );
        assertFalse( new File( exportDir, ".svn" ).exists() );
    }
    
    public void testSkipExportIfExists()
        throws Exception
    {
        exportDir.mkdirs();

        ExportMojo mojo = (ExportMojo) lookupMojo( "export", getTestFile(
            "src/test/resources/mojos/export/exportWhenExportDirectoryExistsAndSkip.xml" ) );

        mojo.setExportDirectory( exportDir );

        mojo.execute();

        assertEquals( 0, exportDir.listFiles().length );        
    }

}
