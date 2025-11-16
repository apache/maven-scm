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
package org.apache.maven.scm.plugin;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@MojoTest
class ValidateMojoTest {

    @Test
    @InjectMojo(goal = "validate", pom = "classpath:/mojos/validate/validateWithoutScmUrl.xml")
    void testValidateWithoutScmUrl(ValidateMojo mojo) {
        assertDoesNotThrow(mojo::execute);
    }

    @Test
    @InjectMojo(goal = "validate", pom = "classpath:/mojos/validate/validateWithValidScmUrls.xml")
    void testValidateWithValidScmUrls(ValidateMojo mojo) {
        assertDoesNotThrow(mojo::execute);
    }

    @Test
    @InjectMojo(goal = "validate", pom = "classpath:/mojos/validate/validateWithInvalidScmUrls.xml")
    void testValidateWithInvalidScmUrls(ValidateMojo mojo) throws Exception {
        MojoExecutionException exception = assertThrows(MojoExecutionException.class, mojo::execute);
        assertNotNull(exception.getMessage());
    }
}
