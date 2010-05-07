package org.apache.maven.scm;

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

import java.io.File;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ScmFileMatcher
    extends TypeSafeMatcher<ScmFile>
{

    public static Matcher<ScmFile> scmFile( String fileName, ScmFileStatus status )
    {
        return new ScmFileMatcher( fileName, status );
    }

    @SuppressWarnings("unchecked")
    public static void assertHasScmFile( List<?> actualFiles, String fileName, ScmFileStatus status )
    {
        org.junit.Assert.assertThat( (List<ScmFile>) actualFiles, hasItem( scmFile( fileName, status ) ) );
    }

    private ScmFileStatus status;

    private String filePath;

    public ScmFileMatcher( String filePath, ScmFileStatus status )
    {
        // Convert to OS specific path...
        this.filePath = new File( filePath ).getPath();
        this.status = status;
    }

    public void describeTo( Description desc )
    {
        desc.appendValue( "ScmFile [" );
        desc.appendValue( filePath );
        desc.appendText( "," );
        desc.appendValue( status );
        desc.appendValue( "]" );
    }

    @Override
    public boolean matchesSafely( ScmFile scmFile )
    {
        return scmFile.getPath().equals( filePath ) && scmFile.getStatus().equals( status );
    }

}