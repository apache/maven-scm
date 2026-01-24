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
package org.apache.maven.scm.tck.command.info;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test tests the info command.
 */
public abstract class InfoCommandTckTest extends ScmTckTestCase {

    @Test
    void testInfoCommandWithJustBasedir() throws Exception {
        ScmProvider scmProvider = getScmManager().getProviderByUrl(getScmUrl());
        InfoScmResult result = scmProvider.info(getScmRepository().getProviderRepository(), getScmFileSet(), null);
        assertResultIsSuccess(result);
        assertEquals(1, result.getInfoItems().size());
        InfoItem item = result.getInfoItems().get(0);
        assertEquals("Mark Struberg <struberg@yahoo.de>", item.getLastChangedAuthor());
        assertEquals("92f139dfec4d1dfb79c3cd2f94e83bf13129668b", item.getRevision());
        assertEquals(
                OffsetDateTime.of(2009, 3, 15, 19, 14, 2, 0, ZoneOffset.ofHours(1)), item.getLastChangedDateTime());
    }

    @Test
    void testInfoCommandFromBasedirDifferentFromWorkingCopyDirectory() throws Exception {
        ScmProvider scmProvider = getScmManager().getProviderByUrl(getScmUrl());
        ScmFileSet fileSet = new ScmFileSet(new File(getWorkingCopy(), "src/main"), new File("java/Application.java"));
        InfoScmResult result = scmProvider.info(getScmRepository().getProviderRepository(), fileSet, null);
        assertResultIsSuccess(result);
        assertEquals(1, result.getInfoItems().size());
        InfoItem item = result.getInfoItems().get(0);
        assertEquals("Mark Struberg <struberg@yahoo.de>", item.getLastChangedAuthor());
        assertEquals("92f139dfec4d1dfb79c3cd2f94e83bf13129668b", item.getRevision());
        assertEquals(
                OffsetDateTime.of(2009, 3, 15, 19, 14, 2, 0, ZoneOffset.ofHours(1)), item.getLastChangedDateTime());
    }
}
