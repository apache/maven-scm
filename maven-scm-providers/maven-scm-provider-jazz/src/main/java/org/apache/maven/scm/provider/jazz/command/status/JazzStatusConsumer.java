package org.apache.maven.scm.provider.jazz.command.status;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consume the output of the scm command for the "status" operation.
 * <p/>
 * It is normally just used to build up a list of ScmFile objects that have
 * their ScmFileStatus set.
 * This class has been expanded so that the Workspace, Component and Baseline
 * are also collected and set back in the JazzScmProviderRepository.
 * The Workspace and Component names are needed for some other commands (list,
 * for example), so we can easily get this information here.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzStatusConsumer
    extends AbstractRepositoryConsumer
{
// We have have a workspace with no flow targets (it points to itself)
//
//  Workspace: (1000) "BogusRepositoryWorkspace" <-> (1000) "BogusRepositoryWorkspace"
//    Component: (1001) "BogusComponent"
//      Baseline: (1128) 27 "BogusTestJazz-3.0.0.40"
//      Unresolved:
//        d-- /BogusTest/pom.xml.releaseBackup
//        d-- /BogusTest/release.properties
//
// Or, we have have one that does have a flow target (ie a stream or another workspace).
//
//  Workspace: (1156) "GPDBWorkspace" <-> (1157) "GPDBStream"
//    Component: (1158) "GPDB" <-> (1157) "GPDBStream"
//      Baseline: (1159) 1 "Initial Baseline"
//
// Note the (%d) numbers are aliases and are only valid for the machine/instance that made the
// remote calls to the server. They are not to be shared across machines (ie don't make them global, public
// or persistent).
//
// We can also have a changeset with a work item associated with it:
//
//  Workspace: (1156) "GPDBWorkspace" <-> (1157) "GPDBStream"
//    Component: (1158) "GPDB"
//      Baseline: (2362) 48 "GPDB-1.0.50"
//        Outgoing:
//          Change sets:
//            (2366) *--@  62 "Release the next release of GPDB." - "Man Created Changeset: X.Y.Z" 28-Apr-2015 07:55 PM
//
// Or not:
//
//  Workspace: (1156) "GPDBWorkspace" <-> (1157) "GPDBStream"
//    Component: (1158) "GPDB"
//      Baseline: (2362) 48 "GPDB-1.0.50"
//        Outgoing:
//          Change sets:
//            (2365) ---@  "This is my changeset comment." 26-Apr-2015 09:36 PM
//
// We can also have a multiple changesets, although, if the correct build procedure has been followed, namely we
// start with a clean starting point, with nothing outstanding, then we should never see this (famous last words!)
//
//  Workspace: (1156) "GPDBWorkspace" <-> (1157) "GPDBStream"
//    Component: (1158) "GPDB"
//      Baseline: (2362) 48 "GPDB-1.0.50"
//        Outgoing:
//          Change sets:
//            (2366) *--@  62 "Release the next release of GPDB." - "Man Created Changeset: X.Y.Z" 28-Apr-2015 07:55 PM
//            (2365) ---@  "This is my changeset comment." 26-Apr-2015 09:36 PM
//
// Because the "Change sets:" line exists by itself, and it is followed by the changeset
// lines, we need to implement a state machine... (seenChangeSets)

    //  Workspace: (1000) "BogusRepositoryWorkspace" <-> (1000) "BogusRepositoryWorkspace"
    //  Workspace: (1156) "GPDBWorkspace" <-> (1157) "GPDBStream"
    private static final Pattern WORKSPACE_PATTERN =
        Pattern.compile( "\\((\\d+)\\) \"(.*)\" <-> \\((\\d+)\\) \"(.*)\"" );

    //  Component: (1001) "BogusComponent"
    private static final Pattern COMPONENT_PATTERN1 = Pattern.compile( "\\((\\d+)\\) \"(.*)\"" );

    //  Component: (1158) "GPDB" <-> (1157) "GPDBStream"
    //  Component: (1002) "FireDragon" <-> (1005) "MavenR3Stream Workspace" (outgoing addition)
    private static final Pattern COMPONENT_PATTERN2 = Pattern.compile( "\\((\\d+)\\) \"(.*)\" <.*>" );

    //  Baseline: (1128) 27 "BogusTestJazz-3.0.0.40"
    private static final Pattern BASELINE_PATTERN = Pattern.compile( "\\((\\d+)\\) (\\d+) \"(.*)\"" );

    // (2365) ---@  "This is my changeset comment." 26-Apr-2015 09:36 PM
    private static final Pattern CHANGESET_PATTERN = Pattern.compile( "\\((\\d+)\\) (.*)" );

    //
    // Additional data we collect. (eye catchers)
    //
    
    /**
     * The "Status" command output line that contains the "Workspace" name.
     */
    public static final String STATUS_CMD_WORKSPACE = "Workspace:";

    /**
     * The "Status" command output line that contains the "Component" name.
     */
    public static final String STATUS_CMD_COMPONENT = "Component:";

    /**
     * The "Status" command output line that contains the "Baseline" name.
     */
    public static final String STATUS_CMD_BASELINE = "Baseline:";

    /**
     * The "Status" command output line that contains the line "Change sets:".
     * This will be followed by the 
     */
    public static final String STATUS_CMD_CHANGE_SETS = "Change sets:";
    
    // File Status Commands (eye catchers)

    /**
     * The "Status" command status flag for a resource that has been added.
     */
    public static final String STATUS_CMD_ADD_FLAG = "a-";

    /**
     * The "Status" command status flag for when the content or properties of
     * a file have been modified, or the properties of a directory have changed.
     */
    public static final String STATUS_CMD_CHANGE_FLAG = "-c";

    /**
     * The "Status" command status flag for a resource that has been deleted.
     */
    public static final String STATUS_CMD_DELETE_FLAG = "d-";

    /**
     * The "Status" command status flag for a resource that has been renamed or moved.
     */
    public static final String STATUS_CMD_MOVED_FLAG = "m-";

    /**
     * A List of ScmFile objects that have their ScmFileStatus set.
     */
    private List<ScmFile> fChangedFiles = new ArrayList<ScmFile>();

    /**
     * Implement a simple state machine: Have we seen the "Change sets:" line or not?
     */
    private boolean seenChangeSets = false;

    /**
     * Constructor for our "scm status" consumer.
     *
     * @param repo   The JazzScmProviderRepository being used.
     * @param logger The ScmLogger to use.
     */
    public JazzStatusConsumer( ScmProviderRepository repo, ScmLogger logger )
    {
        super( repo, logger );
    }

    /**
     * Process one line of output from the execution of the "scm status" command.
     *
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        super.consumeLine( line );
        if ( containsWorkspace( line ) )
        {
            extractWorkspace( line );
        }
        if ( containsComponent( line ) )
        {
            extractComponent( line );
        }
        if ( containsBaseline( line ) )
        {
            extractBaseline( line );
        }
        if ( containsStatusFlag( line ) )
        {
            extractChangedFile( line );
        }
        if ( containsChangeSets( line ) )
        {
            seenChangeSets = true;
        }
        if ( seenChangeSets )
        {
            extractChangeSetAlias( line );
        }
    }

    private boolean containsWorkspace( String line )
    {
        return line.trim().startsWith( STATUS_CMD_WORKSPACE );
    }

    private void extractWorkspace( String line )
    {
        // With no stream (flow target):
        //   Workspace: (1000) "BogusRepositoryWorkspace" <-> (1000) "BogusRepositoryWorkspace"
        // With a stream:
        //   Workspace: (1156) "GPDBWorkspace" <-> (1157) "GPDBStream"

        Matcher matcher = WORKSPACE_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            JazzScmProviderRepository jazzRepository = (JazzScmProviderRepository) getRepository();

            int workspaceAlias = Integer.parseInt( matcher.group( 1 ) );
            String workspace = matcher.group( 2 );
            int streamAlias = Integer.parseInt( matcher.group( 3 ) );
            String stream = matcher.group( 4 );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully parsed \"Workspace:\" line:" );
                getLogger().debug( "  workspaceAlias = " + workspaceAlias );
                getLogger().debug( "  workspace      = " + workspace );
                getLogger().debug( "  streamAlias    = " + streamAlias );
                getLogger().debug( "  stream         = " + stream );
            }
            jazzRepository.setWorkspaceAlias( workspaceAlias );
            jazzRepository.setWorkspace( workspace );
            jazzRepository.setFlowTargetAlias( streamAlias );
            jazzRepository.setFlowTarget( stream );
        }
    }

    private boolean containsComponent( String line )
    {
        return line.trim().startsWith( STATUS_CMD_COMPONENT );
    }

    private void extractComponent( String line )
    {
        // With no stream (flow target):
        //     Component: (1001) "BogusComponent"
        // With a stream:
        //     Component: (1158) "GPDB" <-> (1157) "GPDBStream"
        // With some additional information:
        //     Component: (1002) "FireDragon" <-> (1005) "MavenR3Stream Workspace" (outgoing addition)

        Matcher matcher = COMPONENT_PATTERN1.matcher( line );
        if ( matcher.find() )
        {
            //     Component: (1001) "BogusComponent"
            JazzScmProviderRepository jazzRepository = (JazzScmProviderRepository) getRepository();
            int componentAlias = Integer.parseInt( matcher.group( 1 ) );
            String component = matcher.group( 2 );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully parsed \"Component:\" line:" );
                getLogger().debug( "  componentAlias = " + componentAlias );
                getLogger().debug( "  component      = " + component );
            }
            jazzRepository.setComponent( component );
        }

        matcher = COMPONENT_PATTERN2.matcher( line );
        if ( matcher.find() )
        {
            //     Component: (1158) "GPDB" <-> (1157) "GPDBStream"
            JazzScmProviderRepository jazzRepository = (JazzScmProviderRepository) getRepository();
            int componentAlias = Integer.parseInt( matcher.group( 1 ) );
            String component = matcher.group( 2 );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully parsed \"Component:\" line:" );
                getLogger().debug( "  componentAlias = " + componentAlias );
                getLogger().debug( "  component      = " + component );
            }
            jazzRepository.setComponent( component );
        }
    }

    private boolean containsBaseline( String line )
    {
        return line.trim().startsWith( STATUS_CMD_BASELINE );
    }

    private void extractBaseline( String line )
    {
        // Baseline: (1128) 27 "BogusTestJazz-3.0.0.40"

        Matcher matcher = BASELINE_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            JazzScmProviderRepository jazzRepository = (JazzScmProviderRepository) getRepository();

            int baselineAlias = Integer.parseInt( matcher.group( 1 ) );
            int baselineId = Integer.parseInt( matcher.group( 2 ) );
            String baseline = matcher.group( 3 );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully parsed \"Baseline:\" line:" );
                getLogger().debug( "  baselineAlias  = " + baselineAlias );
                getLogger().debug( "  baselineId     = " + baselineId );
                getLogger().debug( "  baseline       = " + baseline );
            }
            jazzRepository.setBaseline( baseline );
        }
    }

    private boolean containsStatusFlag( String line )
    {
        boolean containsStatusFlag = false;

        if ( line.trim().length() > 2 )
        {
            String flag = line.trim().substring( 0, 2 );
            if ( STATUS_CMD_ADD_FLAG.equals( flag ) || STATUS_CMD_CHANGE_FLAG.equals( flag )
                || STATUS_CMD_DELETE_FLAG.equals( flag ) )
            {
                containsStatusFlag = true;
            }
        }
        return containsStatusFlag;
    }

    private void extractChangedFile( String line )
    {
        String flag = line.trim().substring( 0, 2 );
        String filePath = line.trim().substring( 3 ).trim();
        ScmFileStatus status = ScmFileStatus.UNKNOWN;

        if ( STATUS_CMD_ADD_FLAG.equals( flag ) )
        {
            status = ScmFileStatus.ADDED;
        }

        if ( STATUS_CMD_CHANGE_FLAG.equals( flag ) )
        {
            status = ScmFileStatus.MODIFIED;
        }

        if ( STATUS_CMD_DELETE_FLAG.equals( flag ) )
        {
            status = ScmFileStatus.DELETED;
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( " Extracted filePath  : '" + filePath + "'" );
            getLogger().debug( " Extracted     flag  : '" + flag + "'" );
            getLogger().debug( " Extracted   status  : '" + status + "'" );
        }

        fChangedFiles.add( new ScmFile( filePath, status ) );
    }

    public List<ScmFile> getChangedFiles()
    {
        return fChangedFiles;
    }

    private boolean containsChangeSets( String line )
    {
        return line.trim().startsWith( STATUS_CMD_CHANGE_SETS );
    }

    private void extractChangeSetAlias( String line )
    {
        // (2365) ---@  "This is my changeset comment." 26-Apr-2015 09:36 PM

        Matcher matcher = CHANGESET_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            JazzScmProviderRepository jazzRepository = (JazzScmProviderRepository) getRepository();

            int changeSetAlias = Integer.parseInt( matcher.group( 1 ) );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully parsed post \"Change sets:\" line:" );
                getLogger().debug( "  changeSetAlias = " + changeSetAlias );
            }
            jazzRepository.setChangeSetAlias( changeSetAlias );
            // This is a difficult one. Do I now turn it off or not?
            seenChangeSets = false;
            // For the moment I am going too.
            // If we ever need to support multiple outgoing changesets,
            // and I can not see how that makes sense in a maven sense,
            // then we can revisit using a list.
            // Also, turning if off means that we only look at the first
            // (and hopefully only!) one.
            // It also means that if we run across some Incoming: changes,
            // then we will not pick them up accidently either.
            //
            // Another way around this would to be to have a specific
            // consumer for the create changeset command itself.
            // That way we would be totally assured that we've picked
            // up the right Changet Set Alias.
        }
    }
}
