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
package org.apache.maven.scm.provider.local.command.checkin;

import java.io.File;

import org.apache.maven.scm.tck.command.checkin.CheckInCommandTckTest;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class LocalCheckInCommandTckTest extends CheckInCommandTckTest {
    private static final String MODULE_NAME = "checkin-tck";

    public String getScmUrl() throws Exception {
        return "scm:local|" + getRepositoryRoot() + "|" + MODULE_NAME;
    }

    public void initRepo() throws Exception {
        makeRepo(getRepositoryRoot());
    }

    private void makeRepo(File workingDirectory) throws Exception {
        makeFile(workingDirectory, MODULE_NAME + "/pom.xml", "/pom.xml");

        makeFile(workingDirectory, MODULE_NAME + "/readme.txt", "/readme.txt");

        makeFile(workingDirectory, MODULE_NAME + "/src/main/java/Application.java", "/src/main/java/Application.java");

        makeFile(workingDirectory, MODULE_NAME + "/src/test/java/Test.java", "/src/test/java/Test.java");

        makeDirectory(workingDirectory, MODULE_NAME + "/src/test/resources");
    }
}
