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
package org.apache.maven.scm.tck.command.checkout;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * This test tests the check out command.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public abstract class CheckOutCommandTckTest extends ScmTckTestCase {
    @Test
    public void testCheckOutCommandTest() throws Exception {
        deleteDirectory(getWorkingCopy());

        CheckOutScmResult result = checkOut(getWorkingCopy(), getScmRepository());

        assertResultIsSuccess(result);

        List<ScmFile> checkedOutFiles = result.getCheckedOutFiles();

        if (checkedOutFiles.size() != 4) {
            SortedSet<ScmFile> files = new TreeSet<>(checkedOutFiles);

            int i = 0;

            for (Iterator<ScmFile> it = files.iterator(); it.hasNext(); i++) {
                ScmFile scmFile = it.next();

                System.out.println(i + ": " + scmFile);
            }

            fail("Expected 4 files in the updated files list, was " + checkedOutFiles.size());
        }
    }
}
