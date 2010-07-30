package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.provider.accurev.Transaction.Version;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class VersionMatcher
    extends TypeSafeMatcher<Version>
{

    private Long elementId;

    private String path;

    private String virtual;

    private String real;

    public VersionMatcher( Long elementId, String path, String virtual, String real )
    {
        this.elementId = elementId;
        this.path = path;
        this.virtual = virtual;
        this.real = real;
    }

    @Override
    public boolean matchesSafely( Version v )
    {
        return elementId.equals( v.getElementId() ) && path.equals( v.getElementName() )
            && virtual.equals( v.getVirtualSpec() ) && real.equals( v.getRealSpec() );
    }

    public void describeTo( Description desc )
    {
        desc.appendText( "version with id=" + elementId + " virtual=" + virtual + " real=" + real + " path=" + path );

    }

    public static Matcher<? extends Version> version( Long elementId, String path, String virtual, String real )
    {
        return new VersionMatcher( elementId, path, virtual, real );
    }
}