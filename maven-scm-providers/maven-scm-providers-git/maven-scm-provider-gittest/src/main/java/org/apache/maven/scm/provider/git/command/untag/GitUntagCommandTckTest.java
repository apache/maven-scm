package org.apache.maven.scm.provider.git.command.untag;

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

import java.io.File;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.untag.UntagCommandTckTest;

public abstract class GitUntagCommandTckTest
    extends UntagCommandTckTest
{
    /** {@inheritDoc} */
    public void initRepo()
        throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory() );
    }

    @Override
    protected CheckOutScmResult checkOut( File workingDirectory, ScmRepository repository ) throws Exception
    {
        try
        {
            return super.checkOut( workingDirectory, repository );
        }
        finally
        {
            GitScmTestUtils.setDefaultUser( workingDirectory );
        }
    }
}
