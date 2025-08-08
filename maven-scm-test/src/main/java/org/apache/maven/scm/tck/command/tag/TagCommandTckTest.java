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
package org.apache.maven.scm.tck.command.tag;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * This test tests the tag command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public abstract class TagCommandTckTest extends ScmTckTestCase {

    protected String getTagName() {
        return "test-tag";
    }

    @Test
    public void testTagCommandTest() throws Exception {
        String tag = getTagName();

        @SuppressWarnings("deprecation")
        TagScmResult tagResult = getScmManager()
                .getProviderByUrl(getScmUrl())
                .tag(getScmRepository(), new ScmFileSet(getWorkingCopy()), tag);

        assertResultIsSuccess(tagResult);

        // see https://issues.apache.org/jira/browse/SCM-754
        // assertEquals( "check all 4 files tagged", 4, tagResult.getTaggedFiles().size() );

        File readmeTxt = new File(getWorkingCopy(), "readme.txt");

        assertEquals("check readme.txt contents", "/readme.txt", FileUtils.fileRead(readmeTxt));

        this.edit(getWorkingCopy(), "readme.txt", null, getScmRepository());
        changeReadmeTxt(readmeTxt.toPath());

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, "Commit message");

        CheckInScmResult checkinResult =
                getScmManager().checkIn(getScmRepository(), new ScmFileSet(getWorkingCopy()), commandParameters);

        assertResultIsSuccess(checkinResult);

        CheckOutScmResult checkoutResult =
                getScmManager().checkOut(getScmRepository(), new ScmFileSet(getAssertionCopy()));

        assertResultIsSuccess(checkoutResult);

        readmeTxt = new File(getAssertionCopy(), "readme.txt");

        assertEquals("check readme.txt contents", "changed file", FileUtils.fileRead(readmeTxt));

        deleteDirectory(getAssertionCopy());

        assertFalse("check previous assertion copy deleted", getAssertionCopy().exists());

        checkoutResult = getScmManager()
                .getProviderByUrl(getScmUrl())
                .checkOut(getScmRepository(), new ScmFileSet(getAssertionCopy()), new ScmTag(tag));

        assertResultIsSuccess(checkoutResult);

        assertEquals("check readme.txt contents is from tagged version", "/readme.txt", FileUtils.fileRead(readmeTxt));
    }

    private void changeReadmeTxt(Path readmeTxt) throws Exception {
        try (Writer output = Files.newBufferedWriter(readmeTxt)) {
            output.write("changed file");
        }
    }
}
