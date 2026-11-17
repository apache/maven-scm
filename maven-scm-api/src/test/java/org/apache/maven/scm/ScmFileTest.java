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
package org.apache.maven.scm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScmFileTest {
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testPathUsesForwardSlashOnWindows() {
        ScmFile file = new ScmFile("src\\main\\Foo.java", ScmFileStatus.ADDED);

        assertEquals("src/main/Foo.java", file.getPath());
        assertEquals(new ScmFile("src/main/Foo.java", ScmFileStatus.ADDED), file);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testBackslashIsKeptOnUnix() {
        ScmFile file = new ScmFile("a\\b", ScmFileStatus.ADDED);

        assertEquals("a\\b", file.getPath());
    }
}
