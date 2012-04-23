package org.apache.maven.scm.provider.jazz.command.blame;

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.jazz.command.JazzTckUtil;
import org.apache.maven.scm.tck.command.blame.BlameCommandTckTest;

import java.io.File;
import java.util.List;

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

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzBlameCommandTckTest
    extends BlameCommandTckTest
{
    // Easy access to our Tck Test Helper class.
    private JazzTckUtil jazzTckUtil = new JazzTckUtil();

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.scm.ScmTckTestCase#initRepo()
     */
    @Override
    public void initRepo()
        throws Exception
    {
        // Create a unique repository workspace for this test.
        jazzTckUtil.initRepo( getScmRepository() );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.scm.ScmTckTestCase#removeRepo()
     */
    @Override
    public void removeRepo()
        throws Exception
    {
        super.removeRepo();
        jazzTckUtil.removeRepo();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.scm.ScmTckTestCase#getScmUrl()
     */
    @Override
    public String getScmUrl()
        throws Exception
    {
        return jazzTckUtil.getScmUrl();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.scm.tck.command.blame.BlameCommandTckTest#verifyResult(org.apache.maven.scm.command.blame.BlameScmResult)
     */
    @Override
    protected void verifyResult( BlameScmResult result )
    {
        List<BlameLine> lines = result.getLines();
        assertEquals( "Expected 1 line in blame!", 1, lines.size() );
        BlameLine line = lines.get( 0 );
        assertNotSame( "The revision can not be zero!", "0", line.getRevision() );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.scm.tck.command.blame.BlameCommandTckTest#isTestDateTime()
     */
    @Override
    protected boolean isTestDateTime()
    {
        // The scm annotate command does not return the time, only the date, so we turn the comparison off
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.scm.ScmTestCase#getWorkingCopy()
     */
    @Override
    protected File getWorkingCopy()
    {
        return jazzTckUtil.getWorkingCopy();
    }
}
