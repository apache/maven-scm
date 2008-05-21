package org.apache.maven.scm.provider.vss.commands.edit;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class VssEditCommandTest
    extends ScmTestCase
{
    private ScmManager scmManager;

    public void setUp()
        throws Exception
    {
        super.setUp();

        scmManager = getScmManager();
    }

    public void testCommandLine()
        throws Exception
    {
        ScmRepository repository =
            scmManager.makeScmRepository( "scm:vss|username|password@C:/Program File/Visual Source Safe|D:/myProject" );
        ScmFileSet fileSet = new ScmFileSet( getTestFile( "target" ) );
        VssEditCommand command = new VssEditCommand();
        Commandline cl = command.buildCmdLine( (VssScmProviderRepository) repository.getProviderRepository(), fileSet );
        String ssPath = VssCommandLineUtils.getSsDir().replace( '/', File.separatorChar );
        assertCommandLine( ssPath + "ss Checkout $D:/myProject -Yusername,password -R -I-", fileSet.getBasedir(), cl );
    }
}
