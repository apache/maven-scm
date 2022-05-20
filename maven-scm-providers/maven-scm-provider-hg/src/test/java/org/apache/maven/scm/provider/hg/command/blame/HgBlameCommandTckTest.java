package org.apache.maven.scm.provider.hg.command.blame;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.hg.HgRepoUtils;
import org.apache.maven.scm.tck.command.blame.BlameCommandTckTest;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Evgeny Mandrikov
 */
public class HgBlameCommandTckTest
    extends BlameCommandTckTest
{
    public String getScmUrl()
        throws Exception
    {
        return HgRepoUtils.getScmUrl();
    }

    public void initRepo()
        throws Exception
    {
        HgRepoUtils.initRepo();
    }

    protected void verifyResult( BlameScmResult result )
    {
        List<BlameLine> lines = result.getLines();
        assertEquals( "Expected 1 line in blame", 1, lines.size() );
        BlameLine line = lines.get( 0 );
        assertEquals( "0", line.getRevision() );
    }
}
