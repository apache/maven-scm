package org.apache.maven.scm.provider.jazz.command.checkout;

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

import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzCheckOutCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;

    private JazzCheckOutConsumer checkOutConsumer;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repo = getScmProviderRepository();
        checkOutConsumer = new JazzCheckOutConsumer( getScmProviderRepository(), new DefaultLog() );
    }

    public void testCreateJazzLoadCommand()
        throws Exception
    {
        ScmRevision rev = new ScmRevision( "revision" );
        // TODO figure out what Jazz SCM does in terms of branch/tag/revision
        // TODO figure out how/when to load specific files
        Commandline cmd = new JazzCheckOutCommand().createJazzLoadCommand( repo, getScmFileSet(), rev ).getCommandline();
        String expected = "scm load --force --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword --dir "
                + getScmFileSet().getBasedir().getAbsolutePath() + " \"Dave's Repository Workspace\"";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testConsumer()
    {
        checkOutConsumer.consumeLine( "Downloading /maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/EmptyFile.txt  (0 B)" );
        checkOutConsumer.consumeLine( "Downloading /maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/folder with spaces/test.java  (123 KB)" );
        checkOutConsumer.consumeLine( "Downloading /maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/file with spaces.java  (12.34 KB)" );
        checkOutConsumer.consumeLine( "Downloading /maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/folder with spaces/file with spaces.txt  (12.3 B)" );
        checkOutConsumer.consumeLine( "" );

        List<ScmFile> checkedOutFiles = checkOutConsumer.getCheckedOutFiles();
        assertNotNull( checkedOutFiles );
        assertEquals( 4, checkedOutFiles.size() );
        assertTrue( checkedOutFiles.contains( new ScmFile(
                                                           "/maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/EmptyFile.txt",
                                                           ScmFileStatus.CHECKED_OUT ) ) );
        assertTrue( checkedOutFiles.contains( new ScmFile(
                                                           "/maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/folder with spaces/test.java",
                                                           ScmFileStatus.CHECKED_OUT ) ) );
        assertTrue( checkedOutFiles.contains( new ScmFile(
                                                           "/maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/file with spaces.java",
                                                           ScmFileStatus.CHECKED_OUT ) ) );
        assertTrue( checkedOutFiles.contains( new ScmFile(
                                                           "/maven-checkout-test/src/main/java/org/apache/maven/scm/provider/jazz/folder with spaces/file with spaces.txt",
                                                           ScmFileStatus.CHECKED_OUT ) ) );
    }
}
