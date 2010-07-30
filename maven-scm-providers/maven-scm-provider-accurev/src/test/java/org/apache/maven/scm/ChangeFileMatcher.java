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

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ChangeFileMatcher extends TypeSafeMatcher<ChangeFile> {

    private String fileName;

    private Matcher<String> versionMatcher;

    public ChangeFileMatcher(String fileName, Matcher<String> versionMatcher) {
	this.fileName = new File(fileName).getPath();
	this.versionMatcher = versionMatcher;
    }

    @Override
    public boolean matchesSafely(ChangeFile changeFile) {
	return is(fileName).matches(new File(changeFile.getName()).getPath());
    }

    public void describeTo(Description desc) {
	desc.appendText("ChangeFile with name=");
	desc.appendValue(fileName);
	desc.appendText(" and version matching ");
	desc.appendDescriptionOf(versionMatcher);

    }

    public static Matcher<ChangeFile> changeFile(String fileName) {
	return new ChangeFileMatcher(fileName, any(String.class));
    }

    public static Matcher<ChangeFile> changeFile(String fileName, Matcher<String> versionMatcher) {
	return new ChangeFileMatcher(fileName, versionMatcher);
    }
}
