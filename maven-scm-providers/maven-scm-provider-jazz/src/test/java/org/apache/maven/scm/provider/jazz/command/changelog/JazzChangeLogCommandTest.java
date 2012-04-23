package org.apache.maven.scm.provider.jazz.command.changelog;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.ArrayList;
import java.util.Calendar;
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
public class JazzChangeLogCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;

    List<ChangeSet> changeSets;

    private JazzHistoryConsumer historyConsumer;

    private JazzListChangesetConsumer listChangesetConsumer;

    private static final String userDateFormat = "";

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repo = getScmProviderRepository();
        changeSets = new ArrayList<ChangeSet>();
        historyConsumer = new JazzHistoryConsumer( repo, new DefaultLog(), changeSets );
        listChangesetConsumer = new JazzListChangesetConsumer( repo, new DefaultLog(), changeSets, userDateFormat );

        // Simulate the output of the parsing of the "scm status" command.
        // IE, fill in the workspace details
        // Needed for the workspace for the list changesets command.
        repo.setWorkspace( "Dave's Repository Workspace" );
    }

    public void testCreateHistoryCommand()
        throws Exception
    {
        Commandline cmd = new JazzChangeLogCommand().createHistoryCommand( repo, getScmFileSet() ).getCommandline();
        String expected =
            "scm history --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword --maximum 10000000";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCreateListChangesetCommand()
        throws Exception
    {
        // Dummy up two change sets.
        ChangeSet changeSet = new ChangeSet();
        changeSet.setRevision( "1010" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1012" );
        changeSets.add( changeSet );

        // Now test them
        Commandline cmd =
            new JazzChangeLogCommand().createListChangesetCommand( repo, getScmFileSet(), changeSets ).getCommandline();
        String expected =
            "scm list changesets --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword --workspace \"Dave's Repository Workspace\" 1010 1012";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testHistoryConsumer()
        throws Exception
    {
        historyConsumer.consumeLine( "Change sets:" );
        historyConsumer.consumeLine(
            "  (1589)  ---$ Deb \"[maven-release-plugin] prepare for next development itera...\"" );
        historyConsumer.consumeLine( "  (1585)  ---$ Deb \"[maven-release-plugin] prepare release GPDB-1.0.21\"" );
        historyConsumer.consumeLine( "  (1584)  ---$ Deb \"This is my first changeset (2)\"" );
        historyConsumer.consumeLine( "  (1583)  ---$ Deb \"This is my first changeset (1)\"" );
        historyConsumer.consumeLine( "  (1323)  ---$ Deb <No comment>" );
        historyConsumer.consumeLine( "  (1319)  ---$ Deb <No comment>" );

        assertEquals( "Wrong number of change sets parsed!", 6, changeSets.size() );
        // The order needs to be preserved.
        assertEquals( "Parsing sequence error (1)", "1589", changeSets.get( 0 ).getRevision() );
        assertEquals( "Parsing sequence error (2)", "1585", changeSets.get( 1 ).getRevision() );
        assertEquals( "Parsing sequence error (3)", "1584", changeSets.get( 2 ).getRevision() );
        assertEquals( "Parsing sequence error (4)", "1583", changeSets.get( 3 ).getRevision() );
        assertEquals( "Parsing sequence error (5)", "1323", changeSets.get( 4 ).getRevision() );
        assertEquals( "Parsing sequence error (6)", "1319", changeSets.get( 5 ).getRevision() );
    }

    public void testListChangesetConsumer()
        throws Exception
    {
        // Dummy up our changeset list, as parsed from the previous "scm history" command
        ChangeSet changeSet = new ChangeSet();
        changeSet.setRevision( "1589" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1585" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1584" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1583" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1323" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1319" );
        changeSets.add( changeSet );

        listChangesetConsumer.consumeLine( "Change sets:" );
        listChangesetConsumer.consumeLine(
            "  (1589)  ---$ Deb \"[maven-release-plugin] prepare for next development iteration\"" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: Feb 25, 2012 10:15 PM (Yesterday)" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1170) \\GPDB\\GPDBEAR\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1171) \\GPDB\\GPDBResources\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1167) \\GPDB\\GPDBWeb\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1165) \\GPDB\\pom.xml" );
        listChangesetConsumer.consumeLine(
            "  (1585)  ---$ Deb \"[maven-release-plugin] prepare release GPDB-1.0.21\"" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: Feb 25, 2012 10:13 PM (Yesterday)" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1170) \\GPDB\\GPDBEAR\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1171) \\GPDB\\GPDBResources\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1167) \\GPDB\\GPDBWeb\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1165) \\GPDB\\pom.xml" );
        listChangesetConsumer.consumeLine( "  (1584)  ---$ Deb \"This is my first changeset (2)\"" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: Feb 25, 2012 10:13 PM (Yesterday)" );
        listChangesetConsumer.consumeLine( "  (1583)  ---$ Deb \"This is my first changeset (1)\"" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: Feb 25, 2012 10:13 PM (Yesterday)" );
        listChangesetConsumer.consumeLine( "  (1323)  ---$ Deb <No comment>" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: Feb 24, 2012 11:04 PM (Last Week)" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1170) \\GPDB\\GPDBEAR\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1171) \\GPDB\\GPDBResources\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1167) \\GPDB\\GPDBWeb\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1165) \\GPDB\\pom.xml" );
        listChangesetConsumer.consumeLine( "  (1319)  ---$ Deb <No comment>" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: Feb 24, 2012 11:03 PM (Last Week)" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1170) \\GPDB\\GPDBEAR\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1171) \\GPDB\\GPDBResources\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1167) \\GPDB\\GPDBWeb\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1165) \\GPDB\\pom.xml" );

        assertEquals( "Wrong number of change sets parsed!", 6, changeSets.size() );
        // The order needs to be preserved.
        // Check revisions
        assertEquals( "Parsing sequence error (1)", "1589", changeSets.get( 0 ).getRevision() );
        assertEquals( "Parsing sequence error (2)", "1585", changeSets.get( 1 ).getRevision() );
        assertEquals( "Parsing sequence error (3)", "1584", changeSets.get( 2 ).getRevision() );
        assertEquals( "Parsing sequence error (4)", "1583", changeSets.get( 3 ).getRevision() );
        assertEquals( "Parsing sequence error (5)", "1323", changeSets.get( 4 ).getRevision() );
        assertEquals( "Parsing sequence error (6)", "1319", changeSets.get( 5 ).getRevision() );
        // Check Author
        assertEquals( "Parsing error - Author (1)", "Deb", changeSets.get( 0 ).getAuthor() );
        assertEquals( "Parsing error - Author (2)", "Deb", changeSets.get( 1 ).getAuthor() );
        assertEquals( "Parsing error - Author (3)", "Deb", changeSets.get( 2 ).getAuthor() );
        assertEquals( "Parsing error - Author (4)", "Deb", changeSets.get( 3 ).getAuthor() );
        assertEquals( "Parsing error - Author (5)", "Deb", changeSets.get( 4 ).getAuthor() );
        assertEquals( "Parsing error - Author (6)", "Deb", changeSets.get( 5 ).getAuthor() );
        // Check Comments
        assertEquals( "Parsing error - Comment (1)", "[maven-release-plugin] prepare for next development iteration",
                      changeSets.get( 0 ).getComment() );
        assertEquals( "Parsing error - Comment (2)", "[maven-release-plugin] prepare release GPDB-1.0.21",
                      changeSets.get( 1 ).getComment() );
        assertEquals( "Parsing error - Comment (3)", "This is my first changeset (2)",
                      changeSets.get( 2 ).getComment() );
        assertEquals( "Parsing error - Comment (4)", "This is my first changeset (1)",
                      changeSets.get( 3 ).getComment() );
        assertEquals( "Parsing error - Comment (5)", "No comment", changeSets.get( 4 ).getComment() );
        assertEquals( "Parsing error - Comment (6)", "No comment", changeSets.get( 5 ).getComment() );
        // Check Dates
        assertEquals( "Parsing error - Date (1)", getDate( 2012, 01, 25, 22, 15, 0, null ),
                      changeSets.get( 0 ).getDate() );
        assertEquals( "Parsing error - Date (2)", getDate( 2012, 01, 25, 22, 13, 0, null ),
                      changeSets.get( 1 ).getDate() );
        assertEquals( "Parsing error - Date (3)", getDate( 2012, 01, 25, 22, 13, 0, null ),
                      changeSets.get( 2 ).getDate() );
        assertEquals( "Parsing error - Date (4)", getDate( 2012, 01, 25, 22, 13, 0, null ),
                      changeSets.get( 3 ).getDate() );
        assertEquals( "Parsing error - Date (5)", getDate( 2012, 01, 24, 23, 04, 0, null ),
                      changeSets.get( 4 ).getDate() );
        assertEquals( "Parsing error - Date (6)", getDate( 2012, 01, 24, 23, 03, 0, null ),
                      changeSets.get( 5 ).getDate() );
        // Check files
        List<ChangeFile> files;
        files = changeSets.get( 0 ).getFiles();
        assertEquals( "Parsing error - Files (1)", 4, files.size() );
        assertEquals( "Parsing error - Files (1) (1)", "\\GPDB\\GPDBEAR\\pom.xml", files.get( 0 ).getName() );
        assertEquals( "Parsing error - Files (1) (2)", "\\GPDB\\GPDBResources\\pom.xml", files.get( 1 ).getName() );
        assertEquals( "Parsing error - Files (1) (3)", "\\GPDB\\GPDBWeb\\pom.xml", files.get( 2 ).getName() );
        assertEquals( "Parsing error - Files (1) (4)", "\\GPDB\\pom.xml", files.get( 3 ).getName() );
        files = changeSets.get( 1 ).getFiles();
        assertEquals( "Parsing error - Files (2)", 4, files.size() );
        assertEquals( "Parsing error - Files (2) (1)", "\\GPDB\\GPDBEAR\\pom.xml", files.get( 0 ).getName() );
        assertEquals( "Parsing error - Files (2) (2)", "\\GPDB\\GPDBResources\\pom.xml", files.get( 1 ).getName() );
        assertEquals( "Parsing error - Files (2) (3)", "\\GPDB\\GPDBWeb\\pom.xml", files.get( 2 ).getName() );
        assertEquals( "Parsing error - Files (2) (4)", "\\GPDB\\pom.xml", files.get( 3 ).getName() );
        files = changeSets.get( 2 ).getFiles();
        assertEquals( "Parsing error - Files (3)", 0, files.size() );   // Yes Virginia, an empty ChangeSet is valid
        files = changeSets.get( 3 ).getFiles();
        assertEquals( "Parsing error - Files (4)", 0, files.size() );   // Yes Virginia, an empty ChangeSet is valid
        files = changeSets.get( 4 ).getFiles();
        assertEquals( "Parsing error - Files (5)", 4, files.size() );
        assertEquals( "Parsing error - Files (5) (1)", "\\GPDB\\GPDBEAR\\pom.xml", files.get( 0 ).getName() );
        assertEquals( "Parsing error - Files (5) (2)", "\\GPDB\\GPDBResources\\pom.xml", files.get( 1 ).getName() );
        assertEquals( "Parsing error - Files (5) (3)", "\\GPDB\\GPDBWeb\\pom.xml", files.get( 2 ).getName() );
        assertEquals( "Parsing error - Files (5) (4)", "\\GPDB\\pom.xml", files.get( 3 ).getName() );
        files = changeSets.get( 5 ).getFiles();
        assertEquals( "Parsing error - Files (6)", 4, files.size() );
        assertEquals( "Parsing error - Files (6) (1)", "\\GPDB\\GPDBEAR\\pom.xml", files.get( 0 ).getName() );
        assertEquals( "Parsing error - Files (6) (2)", "\\GPDB\\GPDBResources\\pom.xml", files.get( 1 ).getName() );
        assertEquals( "Parsing error - Files (6) (3)", "\\GPDB\\GPDBWeb\\pom.xml", files.get( 2 ).getName() );
        assertEquals( "Parsing error - Files (6) (4)", "\\GPDB\\pom.xml", files.get( 3 ).getName() );
        // Check file actions = ScmFileStatus
        files = changeSets.get( 0 ).getFiles();
        assertEquals( "Parsing error - File Status (1) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
        assertEquals( "Parsing error - File Status (1) (2)", ScmFileStatus.MODIFIED, files.get( 1 ).getAction() );
        assertEquals( "Parsing error - File Status (1) (3)", ScmFileStatus.MODIFIED, files.get( 2 ).getAction() );
        assertEquals( "Parsing error - File Status (1) (4)", ScmFileStatus.MODIFIED, files.get( 3 ).getAction() );
        files = changeSets.get( 1 ).getFiles();
        assertEquals( "Parsing error - File Status (2) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
        assertEquals( "Parsing error - File Status (2) (2)", ScmFileStatus.MODIFIED, files.get( 1 ).getAction() );
        assertEquals( "Parsing error - File Status (2) (3)", ScmFileStatus.MODIFIED, files.get( 2 ).getAction() );
        assertEquals( "Parsing error - File Status (2) (4)", ScmFileStatus.MODIFIED, files.get( 3 ).getAction() );
        files = changeSets.get( 4 ).getFiles();
        assertEquals( "Parsing error - File Status (5) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
        assertEquals( "Parsing error - File Status (5) (2)", ScmFileStatus.MODIFIED, files.get( 1 ).getAction() );
        assertEquals( "Parsing error - File Status (5) (3)", ScmFileStatus.MODIFIED, files.get( 2 ).getAction() );
        assertEquals( "Parsing error - File Status (5) (4)", ScmFileStatus.MODIFIED, files.get( 3 ).getAction() );
        files = changeSets.get( 5 ).getFiles();
        assertEquals( "Parsing error - File Status (6) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
        assertEquals( "Parsing error - File Status (6) (2)", ScmFileStatus.MODIFIED, files.get( 1 ).getAction() );
        assertEquals( "Parsing error - File Status (6) (3)", ScmFileStatus.MODIFIED, files.get( 2 ).getAction() );
        assertEquals( "Parsing error - File Status (6) (4)", ScmFileStatus.MODIFIED, files.get( 3 ).getAction() );
    }

    public void testListChangesetConsumerWithTimeOnly()
        throws Exception
    {
        // Dummy up our changeset list, as parsed from the previous "scm history" command
        ChangeSet changeSet = new ChangeSet();
        changeSet.setRevision( "1809" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1801" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1799" );
        changeSets.add( changeSet );
        changeSet = new ChangeSet();
        changeSet.setRevision( "1764" );
        changeSets.add( changeSet );

        listChangesetConsumer.consumeLine( "Change sets:" );
        listChangesetConsumer.consumeLine(
            "  (1809)  ---$ Deb \"[maven-release-plugin] prepare for next development iteration\"" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: 6:20 PM (5 minutes ago)" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1170) \\GPDB\\GPDBEAR\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1171) \\GPDB\\GPDBResources\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1167) \\GPDB\\GPDBWeb\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1165) \\GPDB\\pom.xml" );
        listChangesetConsumer.consumeLine(
            "  (1801)  ---$ Deb \"[maven-release-plugin] prepare release GPDB-1.0.26\"" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: 6:18 PM (10 minutes ago)" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1170) \\GPDB\\GPDBEAR\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1171) \\GPDB\\GPDBResources\\pom.xml" );
        listChangesetConsumer.consumeLine( "      ---c- (1167) \\GPDB\\GPDBWeb\\pom.xml" );
        listChangesetConsumer.consumeLine( "  (1799)  ---$ Deb <No comment>" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: 6:18 PM (10 minutes ago)" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1165) \\GPDB\\pom.xml" );
        listChangesetConsumer.consumeLine( "  (1764)  ---$ Deb <No comment>" );
        listChangesetConsumer.consumeLine( "    Component: (1158) \"GPDB\"" );
        listChangesetConsumer.consumeLine( "    Modified: Mar 1, 2012 2:34 PM" );
        listChangesetConsumer.consumeLine( "    Changes:" );
        listChangesetConsumer.consumeLine( "      ---c- (1165) \\GPDB\\pom.xml" );

        assertEquals( "Wrong number of change sets parsed!", 4, changeSets.size() );
        // The order needs to be preserved.
        // Check revisions
        assertEquals( "Parsing sequence error (1)", "1809", changeSets.get( 0 ).getRevision() );
        assertEquals( "Parsing sequence error (2)", "1801", changeSets.get( 1 ).getRevision() );
        assertEquals( "Parsing sequence error (3)", "1799", changeSets.get( 2 ).getRevision() );
        assertEquals( "Parsing sequence error (4)", "1764", changeSets.get( 3 ).getRevision() );
        // Check Author
        assertEquals( "Parsing error - Author (1)", "Deb", changeSets.get( 0 ).getAuthor() );
        assertEquals( "Parsing error - Author (2)", "Deb", changeSets.get( 1 ).getAuthor() );
        assertEquals( "Parsing error - Author (3)", "Deb", changeSets.get( 2 ).getAuthor() );
        assertEquals( "Parsing error - Author (4)", "Deb", changeSets.get( 3 ).getAuthor() );
        // Check Comments
        assertEquals( "Parsing error - Comment (1)", "[maven-release-plugin] prepare for next development iteration",
                      changeSets.get( 0 ).getComment() );
        assertEquals( "Parsing error - Comment (2)", "[maven-release-plugin] prepare release GPDB-1.0.26",
                      changeSets.get( 1 ).getComment() );
        assertEquals( "Parsing error - Comment (3)", "No comment", changeSets.get( 2 ).getComment() );
        assertEquals( "Parsing error - Comment (4)", "No comment", changeSets.get( 3 ).getComment() );
        // Check Dates
        Calendar today = Calendar.getInstance();
        assertEquals( "Parsing error - Date (1)", getDate( today.get( Calendar.YEAR ), today.get( Calendar.MONTH ),
                                                           today.get( Calendar.DAY_OF_MONTH ), 18, 20, 0, null ),
                      changeSets.get( 0 ).getDate() );
        assertEquals( "Parsing error - Date (2)", getDate( today.get( Calendar.YEAR ), today.get( Calendar.MONTH ),
                                                           today.get( Calendar.DAY_OF_MONTH ), 18, 18, 0, null ),
                      changeSets.get( 1 ).getDate() );
        assertEquals( "Parsing error - Date (3)", getDate( today.get( Calendar.YEAR ), today.get( Calendar.MONTH ),
                                                           today.get( Calendar.DAY_OF_MONTH ), 18, 18, 0, null ),
                      changeSets.get( 2 ).getDate() );
        assertEquals( "Parsing error - Date (4)", getDate( 2012, 02, 01, 14, 34, 0, null ),
                      changeSets.get( 3 ).getDate() );
        // Check files
        List<ChangeFile> files;
        files = changeSets.get( 0 ).getFiles();
        assertEquals( "Parsing error - Files (1)", 4, files.size() );
        assertEquals( "Parsing error - Files (1) (1)", "\\GPDB\\GPDBEAR\\pom.xml", files.get( 0 ).getName() );
        assertEquals( "Parsing error - Files (1) (2)", "\\GPDB\\GPDBResources\\pom.xml", files.get( 1 ).getName() );
        assertEquals( "Parsing error - Files (1) (3)", "\\GPDB\\GPDBWeb\\pom.xml", files.get( 2 ).getName() );
        assertEquals( "Parsing error - Files (1) (4)", "\\GPDB\\pom.xml", files.get( 3 ).getName() );
        files = changeSets.get( 1 ).getFiles();
        assertEquals( "Parsing error - Files (2)", 3, files.size() );
        assertEquals( "Parsing error - Files (2) (1)", "\\GPDB\\GPDBEAR\\pom.xml", files.get( 0 ).getName() );
        assertEquals( "Parsing error - Files (2) (2)", "\\GPDB\\GPDBResources\\pom.xml", files.get( 1 ).getName() );
        assertEquals( "Parsing error - Files (2) (3)", "\\GPDB\\GPDBWeb\\pom.xml", files.get( 2 ).getName() );
        files = changeSets.get( 2 ).getFiles();
        assertEquals( "Parsing error - Files (3)", 1, files.size() );
        assertEquals( "Parsing error - Files (3) (1)", "\\GPDB\\pom.xml", files.get( 0 ).getName() );
        files = changeSets.get( 3 ).getFiles();
        assertEquals( "Parsing error - Files (4)", 1, files.size() );
        assertEquals( "Parsing error - Files (4) (1)", "\\GPDB\\pom.xml", files.get( 0 ).getName() );
        // Check file actions = ScmFileStatus
        files = changeSets.get( 0 ).getFiles();
        assertEquals( "Parsing error - File Status (1) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
        assertEquals( "Parsing error - File Status (1) (2)", ScmFileStatus.MODIFIED, files.get( 1 ).getAction() );
        assertEquals( "Parsing error - File Status (1) (3)", ScmFileStatus.MODIFIED, files.get( 2 ).getAction() );
        assertEquals( "Parsing error - File Status (1) (4)", ScmFileStatus.MODIFIED, files.get( 3 ).getAction() );
        files = changeSets.get( 1 ).getFiles();
        assertEquals( "Parsing error - File Status (2) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
        assertEquals( "Parsing error - File Status (2) (2)", ScmFileStatus.MODIFIED, files.get( 1 ).getAction() );
        assertEquals( "Parsing error - File Status (2) (3)", ScmFileStatus.MODIFIED, files.get( 2 ).getAction() );
        files = changeSets.get( 2 ).getFiles();
        assertEquals( "Parsing error - File Status (3) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
        files = changeSets.get( 3 ).getFiles();
        assertEquals( "Parsing error - File Status (4) (1)", ScmFileStatus.MODIFIED, files.get( 0 ).getAction() );
    }

    public void testStripDelimiters()
        throws Exception
    {
        assertEquals( "stripDelimiters() is broken! (1)", "Plain Text",
                      listChangesetConsumer.stripDelimiters( "Plain Text" ) );
        assertEquals( "stripDelimiters() is broken! (2)", "Plain Text",
                      listChangesetConsumer.stripDelimiters( "\"Plain Text" ) );
        assertEquals( "stripDelimiters() is broken! (3)", "Plain Text",
                      listChangesetConsumer.stripDelimiters( "Plain Text\"" ) );
        assertEquals( "stripDelimiters() is broken! (4)", "Plain Text",
                      listChangesetConsumer.stripDelimiters( "\"Plain Text\"" ) );
        assertEquals( "stripDelimiters() is broken! (5)", "Plain Text",
                      listChangesetConsumer.stripDelimiters( "<Plain Text" ) );
        assertEquals( "stripDelimiters() is broken! (6)", "Plain Text",
                      listChangesetConsumer.stripDelimiters( "Plain Text>" ) );
        assertEquals( "stripDelimiters() is broken! (7)", "Plain Text",
                      listChangesetConsumer.stripDelimiters( "<Plain Text>" ) );
    }

}
