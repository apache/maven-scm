package org.apache.maven.scm.provider.jazz.command.status;

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

import static org.junit.Assert.*;

import java.io.File;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.log.ScmLogger;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * 
 * @author Robert Scholte
 */
public class JazzStatusConsumerTest
{
    private JazzStatusConsumer statusConsumer;

    @Mock
    private ScmLogger scmLogger;

    @Before
    public void initMocks()
    {
        MockitoAnnotations.initMocks( this );
    }

    @Ignore
    // @todo fix JazzStatusConsumer to match the ScmFile restrictions
    public void testScmFilePath()
    {
        statusConsumer = new JazzStatusConsumer( null, scmLogger );
        statusConsumer.consumeLine( "      d-- /BogusTest/release.properties" );
        assertNotNull( statusConsumer.getChangedFiles() );
        assertEquals( 1, statusConsumer.getChangedFiles().size() );
        ScmFile changedFile = statusConsumer.getChangedFiles().get( 0 );
        assertEquals( "BogusTest" + File.separator + "release.properties", changedFile.getPath() );
    }
}
