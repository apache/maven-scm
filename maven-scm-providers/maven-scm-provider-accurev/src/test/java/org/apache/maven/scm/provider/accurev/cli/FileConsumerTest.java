package org.apache.maven.scm.provider.accurev.cli;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.cli.StreamConsumer;
import org.junit.Test;

public class FileConsumerTest
{

    @Test
    public void testConsumeAdd()
    {
        List<File> extractedFiles = new ArrayList<File>();

        StreamConsumer consumer = new FileConsumer( extractedFiles, FileConsumer.ADD_PATTERN );

        consumer.consumeLine( "Added and kept element /./src/main/java/Application.java" );
        consumer.consumeLine( "Added and kept element \\.\\src\\main\\java\\Windows.java" );

        assertThat( extractedFiles.size(), is( 2 ) );
        assertThat( extractedFiles, hasItem( new File( "src/main/java/Application.java" ) ) );
        assertThat( extractedFiles, hasItem( new File( "src\\main\\java\\Windows.java" ) ) );
    }

    @Test
    public void testConsumeUpdate()
    {
        List<File> extractedFiles = new ArrayList<File>();
        StreamConsumer consumer = new FileConsumer( extractedFiles, FileConsumer.UPDATE_PATTERN );

        consumer.consumeLine( "Content (1 K) of \"readme.txt\" - ok" );
        consumer.consumeLine( "Creating dir \"src/main/java/org\" ." );
        consumer.consumeLine( "Updating (creating) dir /./src/test/java" );
        consumer.consumeLine( "Updating element \\.\\src\\main\\java\\Application.java" );

        assertThat( extractedFiles.size(), is( 2 ) );
        assertThat( extractedFiles, hasItem( new File( "readme.txt" ) ) );
        assertThat( extractedFiles, hasItem( new File( "src\\main\\java\\Application.java" ) ) );
    }

    @Test
    public void testConsumePromoted()
    {
        List<File> extractedFiles = new ArrayList<File>();

        StreamConsumer consumer = new FileConsumer( extractedFiles, FileConsumer.PROMOTE_PATTERN );

        consumer.consumeLine( "Promoted element /./src/main/java/Application.java" );
        consumer.consumeLine( "Promoted element \\.\\src\\main\\java\\Windows.java" );

        assertThat( extractedFiles.size(), is( 2 ) );
        assertThat( extractedFiles, hasItem( new File( "src/main/java/Application.java" ) ) );
        assertThat( extractedFiles, hasItem( new File( "src\\main\\java\\Windows.java" ) ) );
    }

    @Test
    public void testConsumeRemoved()
    {
        List<File> extractedFiles = new ArrayList<File>();
        StreamConsumer consumer = new FileConsumer( extractedFiles, FileConsumer.DEFUNCT_PATTERN );

        consumer.consumeLine( "Recursively removing \"tcktests/src\" ." );
        consumer.consumeLine( "Removing \"tcktests/src/main/java/Application.java\" ." );
        consumer.consumeLine( "Removing \"tcktests/src/main/java\" ." );
        consumer.consumeLine( "Removing \"tcktests/src/main\" ." );
        consumer.consumeLine( "Removing \"tcktests/src\" ." );

        assertThat( extractedFiles.size(), is( 4 ) );
        assertThat( extractedFiles, hasItem( new File( "tcktests/src" ) ) );
        assertThat( extractedFiles, hasItem( new File( "tcktests/src/main/java/Application.java" ) ) );
    }
}
