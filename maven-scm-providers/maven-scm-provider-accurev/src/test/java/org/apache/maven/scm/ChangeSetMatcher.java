package org.apache.maven.scm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.maven.scm.ChangeFileMatcher.changeFile;
import static org.apache.maven.scm.CollectionSizeMatcher.size;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ChangeSetMatcher
    extends TypeSafeMatcher<ChangeSet>
{

    private String comment;

    private Matcher<Iterable<ChangeFile>> changeFilesMatcher;

    @SuppressWarnings( "unchecked" )
    public ChangeSetMatcher( String comment, String... fileNames )
    {
        this.comment = comment;

        Matcher<? extends ChangeFile> elementMatchers[] = new ChangeFileMatcher[fileNames.length];
        for ( int i = 0; i < elementMatchers.length; i++ )
        {
            elementMatchers[i] = changeFile( fileNames[i], any( String.class ) );
        }
        this.changeFilesMatcher = allOf( hasItems( elementMatchers ), size( fileNames.length, ChangeFile.class ) );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public boolean matchesSafely( ChangeSet changeSet )
    {
        List<ChangeFile> files = changeSet.getFiles();
        return is( comment ).matches( changeSet.getComment() ) && changeFilesMatcher.matches( files );
    }

    public void describeTo( Description desc )
    {
        desc.appendText( "ChangeSet with comment=" );
        desc.appendValue( comment );
        desc.appendText( " and files matching " );
        desc.appendDescriptionOf( changeFilesMatcher );
    }

    public static Matcher<ChangeSet> changeSet( String comment, String... fileNames )
    {
        return new ChangeSetMatcher( comment, fileNames );
    }
}
