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
 * <p/>
 * As this class has expanded over time, it has become more and more of a state
 * machine, one that needs to parse the output of the "scm status --wide" command.
 * If there are any issues with this provider, I would suggest this is a good
 * place to start.
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
// We can also have a multiple changesets. These will be seen when a JBE is used to perform
// the release and has been instructed to create a baseline prior to starting the build.
// Multiple changesets will also be seen when a maven release process fails (for whatever reason).
//
//  Workspace: (1156) "GPDBWorkspace" <-> (1157) "GPDBStream"
//    Component: (1158) "GPDB"
//      Baseline: (2362) 48 "GPDB-1.0.50"
//        Outgoing:
//          Change sets:
//            (2366) *--@  62 "Release the next release of GPDB." - "Man Created Changeset: X.Y.Z" 28-Apr-2015 07:55 PM
//            (2365) ---@  "This is my changeset comment." 26-Apr-2015 09:36 PM
//
// We can also have Baselines, of which there may be more than one (especially true if an update (accept changes)
// has not been done in a while.
//
// So the most complete/complex example I can find is something like this:
//
//  Workspace: (1756) "Scott's GPDBWorkspace" <-> (1157) "GPDBStream"
//    Component: (1158) "GPDB"
//      Baseline: (1718) 25 "GPDB-1.0.25"
//      Unresolved:
//        -c- /GPDB/pom.xml
//      Outgoing:
//        Change sets:
//          (2389) *--@  "<No comment>" 23-May-2015 07:09 PM
//      Incoming:
//        Change sets:
//          (2385) ---$ Deb 62 "Release the next release of GPDB." - \
//             + "[maven-release-plugin] prepare for next development itera..." 02-May-2015 11:01 PM
//      Baselines:
//        (2386) 52 "GPDB-1.0.53"
//        (2387) 51 "GPDB-1.0.52"
//        (2388) 50 "GPDB-1.0.51"
//        (2369) 49 "GPDB-MAN-1.0.50"
//        (2362) 48 "GPDB-1.0.50"
//        (2357) 47 "GPDB-1.0.49"
//        (2352) 46 "GPDB-1.0.48"
//        (2347) 45 "GPDB-1.0.47"
//        (2292) 44 "GPDB-1.0.46"
//        (2285) 42 "GPDB-1.0.42"
//        (2276) 41 "GPDB-1.0.41"
//        (2259) 40 "GPDB-1.0.40"
//        (2250) 39 "GPDB-1.0.39"
//        (2241) 38 "GPDB-1.0.38"
//        (2232) 37 "GPDB-1.0.37"
//        (2222) 36 "GPDB-1.0.36"
//        (2212) 35 "GPDB-1.0.35"
//        (2202) 34 "GPDB-1.0.34"
//        (2191) 33 "GPDB-1.0.33"
//        (2181) 32 "GPDB-1.0.32"
//        (2171) 31 "GPDB-1.0.31"
//        (2160) 30 "GPDB-1.0.30"
//        (2147) 29 "GPDB-1.0.29"
//        (2079) 28 "GPDB-1.0.28"
//        (1851) 27 "GPDB-1.0.27"
//        (1807) 26 "GPDB-1.0.26"
//
// Because the "Change sets:" line exists by itself, and it is followed by the changeset
// lines, we need to implement a state machine... (seenIncomingChangeSets and seenOutgoingChangeSets)
//
// We can also have collisions:
//
//  Workspace: (8551) "myNewWorkspace" <-> (8552) "stream19_test_max_results_1256765247692134"
//    Component: (8553) "Flux Capacitor"
//      Baseline: (8554) 1 "Initial Baseline"
//      Outgoing:
//        Change sets:
//          (8617) -#@ "Update from November planning meeting"
//            Changes:
//              -#-c /flux.capacitor/requirements.txt
//      Incoming:
//        Change sets:
//          (8616) -#$ "Results of initial trials"
//            Changes:
//              -#-c /flux.capacitor/requirements.txt

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
     * The "Status" command output line that contains the "Outgoing" eye catcher.
     */
    public static final String STATUS_CMD_OUTGOING = "Outgoing:";

    /**
     * The "Status" command output line that contains the "Incoming" eye catcher.
     */
    public static final String STATUS_CMD_INCOMING = "Incoming:";

    /**
     * The "Status" command output line that contains the line "Change sets:".
     * This will be followed by the change set lines themselves. 
     */
    public static final String STATUS_CMD_CHANGE_SETS = "Change sets:";

    /**
     * The "Status" command output line that contains the "Baselines" eye catcher.
     */
    public static final String STATUS_CMD_BASELINES = "Baselines:";
    
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
     * Implement a simple state machine: Have we seen the "Change sets:" (outgoing) line or not?
     */
    private boolean seenOutgoingChangeSets = false;

    /**
     * Implement a simple state machine: Have we seen the "Change sets:" (incoming) line or not?
     */
    private boolean seenIncomingChangeSets = false;

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
        if ( containsOutgoing( line ) )
        {
            // Now looking for outgoing, not incoming
            seenOutgoingChangeSets = true;
            seenIncomingChangeSets = false;
        }
        if ( containsIncoming( line ) )
        {
            // Now looking for incoming, not outgoing
            seenOutgoingChangeSets = false;
            seenIncomingChangeSets = true;
        }
        if ( containsBaselines( line ) )
        {
            // Got to baselines, stop looking for all changesets
            seenOutgoingChangeSets = false;
            seenIncomingChangeSets = false;
        }
        if ( seenOutgoingChangeSets )
        {
            Integer changeSetAlias = extractChangeSetAlias( line );
            if ( changeSetAlias != null )
            {
                // We are now supporting multiple change sets, as this allows
                // us to cater for multiple changeset caused by previous failed
                // release attempts.
                // Our starting point should always be a clean slate of a workspace
                // or sandbox, however, if something fails, then we will have some
                // changesets already created, so we need to be able to deal with them effectively.
                JazzScmProviderRepository jazzRepository = (JazzScmProviderRepository) getRepository();
                jazzRepository.getOutgoingChangeSetAliases().add( new Integer( changeSetAlias ) );
            }
        }
        if ( seenIncomingChangeSets )
        {
            Integer changeSetAlias = extractChangeSetAlias( line );
            if ( changeSetAlias != null )
            {
                // We are now supporting multiple change sets, as this allows
                // us to cater for multiple changeset caused by previous failed
                // release attempts.
                // Our starting point should always be a clean slate of a workspace
                // or sandbox, however, if something fails, then we will have some
                // changesets already created, so we need to be able to deal with them effectively.
                JazzScmProviderRepository jazzRepository = (JazzScmProviderRepository) getRepository();
                jazzRepository.getIncomingChangeSetAliases().add( new Integer( changeSetAlias ) );
            }
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

    private boolean containsOutgoing( String line )
    {
        return line.trim().startsWith( STATUS_CMD_OUTGOING );
    }

    private boolean containsIncoming( String line )
    {
        return line.trim().startsWith( STATUS_CMD_INCOMING );
    }

    private boolean containsBaselines( String line )
    {
        return line.trim().startsWith( STATUS_CMD_BASELINES );
    }

    /**
     * Extract and return an Integer of a change set alias, from both
     * incoming and outgoing changesets.
     * @param line The line to extract the change sets from.
     * @return A parsed Integer value, or null if not able to parse.
     */
    private Integer extractChangeSetAlias( String line )
    {
        // (2365) ---@  "This is my changeset comment." 26-Apr-2015 09:36 PM

        Matcher matcher = CHANGESET_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            int changeSetAlias = Integer.parseInt( matcher.group( 1 ) );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully parsed post \"Change sets:\" line:" );
                getLogger().debug( "  changeSetAlias = " + changeSetAlias );
            }
            return new Integer( changeSetAlias );
        }
        else
        {
            return null;
        }
    }
}
