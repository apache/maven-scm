package org.apache.maven.scm.provider.svn.command.tag;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.tag.TagCommandTckTest;

import java.io.File;

/**
 * This test tests the tag command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnTagCommandTckTest extends TagCommandTckTest
{
    public String getScmUrl()
        throws Exception
    {
        return SvnScmTestUtils.getScmUrl( new File( getRepositoryRoot(), "trunk" ) );
    }

    protected ScmRepository getScmRepository( ScmManager scmManager )
        throws Exception
    {
        ScmRepository repo = super.getScmRepository( scmManager );
        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo.getProviderRepository();
        repository.setTagBase( SvnScmTestUtils.getScmUrl( new File( getRepositoryRoot(), "tags" ) ).substring( "scm:svn:".length() ) );
        return repo;
    }

    public void initRepo()
        throws Exception
    {
        SvnScmTestUtils.initializeRepository( getRepositoryRoot(), getTestFile( "src/test/resources/tck/tck.dump" ) );
    }
}
