package org.apache.maven.scm.provider.integrity;

/**
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

import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Field;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.mks.api.si.SIModelTypeName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class represents a MKS Integrity Configuration Management Project
 * <br>Provides metadata information about a Project
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: Project.java 1.6 2011/08/22 13:06:48EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class Project
{
    public static final String NORMAL_PROJECT = "Normal";

    public static final String VARIANT_PROJECT = "Variant";

    public static final String BUILD_PROJECT = "Build";

    private String projectName;

    private String projectType;

    private String projectRevision;

    private String fullConfigSyntax;

    private Date lastCheckpoint;

    private APISession api;

    // Create a custom comparator to compare project members
    public static final Comparator<Member> FILES_ORDER = new Comparator<Member>()
    {
        public int compare( Member cmm1, Member cmm2 )
        {
            return cmm1.getMemberName().compareToIgnoreCase( cmm2.getMemberName() );
        }
    };

    /**
     * Checks if the given value is a valid MKS Integrity Label.
     * <br>If it's invalid, this method throws an exception providing the reason as string.
     *
     * @param tagName The checkpoint label name
     * @return the error message, or null if label is valid
     */
    public static void validateTag( String tagName )
        throws Exception
    {
        if ( tagName == null || tagName.length() == 0 )
        {
            throw new Exception( "The checkpoint label string is empty!" );
        }

        char ch = tagName.charAt( 0 );
        if ( !( ( 'A' <= ch && ch <= 'Z' ) || ( 'a' <= ch && ch <= 'z' ) ) )
        {
            throw new Exception( "The checkpoint label must start with an alpha character!" );
        }

        for ( char invalid : "$,.:;/\\@".toCharArray() )
        {
            if ( tagName.indexOf( invalid ) >= 0 )
            {
                throw new Exception(
                    "The checkpoint label may cannot contain one of the following characters: $ , . : ; / \\ @" );
            }
        }
    }

    /**
     * Creates an instance of an Integrity SCM Project
     *
     * @param api        MKS API Session object
     * @param configPath Configuration path for the MKS Integrity SCM Project
     * @throws APIException
     */
    public Project( APISession api, String configPath )
        throws APIException
    {
        // Initialize our local APISession
        this.api = api;
        try
        {
            // Get the project information for this project
            Command siProjectInfoCmd = new Command( Command.SI, "projectinfo" );
            siProjectInfoCmd.addOption( new Option( "project", configPath ) );
            api.getLogger().info( "Preparing to execute si projectinfo for " + configPath );
            Response infoRes = api.runCommand( siProjectInfoCmd );
            // Get the only work item from the response
            WorkItem wi = infoRes.getWorkItems().next();
            // Get the metadata information about the project
            Field pjNameFld = wi.getField( "projectName" );
            Field pjTypeFld = wi.getField( "projectType" );
            Field pjCfgPathFld = wi.getField( "fullConfigSyntax" );
            Field pjChkptFld = wi.getField( "lastCheckpoint" );

            // Convert to our class fields
            // First obtain the project name field
            if ( null != pjNameFld && null != pjNameFld.getValueAsString() )
            {
                projectName = pjNameFld.getValueAsString();
            }
            else
            {
                api.getLogger().warn( "Project info did not provide a value for the 'projectName' field!" );
                projectName = "";
            }
            // Next, we'll need to know the project type
            if ( null != pjTypeFld && null != pjTypeFld.getValueAsString() )
            {
                projectType = pjTypeFld.getValueAsString();
                if ( isBuild() )
                {
                    // Next, we'll need to know the current build checkpoint for this configuration
                    Field pjRevFld = wi.getField( "revision" );
                    if ( null != pjRevFld && null != pjRevFld.getItem() )
                    {
                        projectRevision = pjRevFld.getItem().getId();
                    }
                    else
                    {
                        projectRevision = "";
                        api.getLogger().warn( "Project info did not provide a vale for the 'revision' field!" );
                    }
                }
            }
            else
            {
                api.getLogger().warn( "Project info did not provide a value for the 'projectType' field!" );
                projectType = "";
            }
            // Most important is the configuration path
            if ( null != pjCfgPathFld && null != pjCfgPathFld.getValueAsString() )
            {
                fullConfigSyntax = pjCfgPathFld.getValueAsString();
            }
            else
            {
                api.getLogger().error( "Project info did not provide a value for the 'fullConfigSyntax' field!" );
                fullConfigSyntax = "";
            }
            // Finally, we'll need to store the last checkpoint to figure out differences, etc.
            if ( null != pjChkptFld && null != pjChkptFld.getDateTime() )
            {
                lastCheckpoint = pjChkptFld.getDateTime();
            }
            else
            {
                api.getLogger().warn( "Project info did not provide a value for the 'lastCheckpoint' field!" );
                lastCheckpoint = Calendar.getInstance().getTime();
            }
        }
        catch ( NoSuchElementException nsee )
        {
            api.getLogger().error( "Project info did not provide a value for field " + nsee.getMessage() );
        }
    }

    /**
     * Returns the project path for this Integrity SCM Project
     *
     * @return
     */
    public String getProjectName()
    {
        return projectName;
    }

    /**
     * Returns the project revision for this Integrity SCM Project
     *
     * @return
     */
    public String getProjectRevision()
    {
        return projectRevision;
    }

    /**
     * Returns true is this is a Normal Project
     *
     * @return
     */
    public boolean isNormal()
    {
        if ( projectType.equalsIgnoreCase( NORMAL_PROJECT ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns true if this is a Variant Project
     *
     * @return
     */
    public boolean isVariant()
    {
        if ( projectType.equalsIgnoreCase( VARIANT_PROJECT ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns true if this is a Build Project
     *
     * @return
     */
    public boolean isBuild()
    {
        if ( projectType.equalsIgnoreCase( BUILD_PROJECT ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the Full Configuration Path for this Integrity SCM Project
     *
     * @return
     */
    public String getConfigurationPath()
    {
        return fullConfigSyntax;
    }

    /**
     * Returns the date when the last checkpoint was performed on this Project
     *
     * @return
     */
    public Date getLastCheckpointDate()
    {
        return lastCheckpoint;
    }

    /**
     * Parses the output from the si viewproject command to get a list of members
     *
     * @param workspaceDir The current workspace directory, which is required for an export
     * @return The list of Member objects for this project
     * @throws APIException
     */
    public List<Member> listFiles( String workspaceDir )
        throws APIException
    {
        // Re-initialize the member list for this project
        List<Member> memberList = new ArrayList<Member>();
        // Initialize the project config hash
        Hashtable<String, String> pjConfigHash = new Hashtable<String, String>();
        // Add the mapping for this project
        pjConfigHash.put( projectName, fullConfigSyntax );
        // Compute the project root directory
        String projectRoot = projectName.substring( 0, projectName.lastIndexOf( '/' ) );

        // Now, lets parse this project
        Command siViewProjectCmd = new Command( Command.SI, "viewproject" );
        siViewProjectCmd.addOption( new Option( "recurse" ) );
        siViewProjectCmd.addOption( new Option( "project", fullConfigSyntax ) );
        MultiValue mvFields = new MultiValue( "," );
        mvFields.add( "name" );
        mvFields.add( "context" );
        mvFields.add( "memberrev" );
        mvFields.add( "membertimestamp" );
        mvFields.add( "memberdescription" );
        siViewProjectCmd.addOption( new Option( "fields", mvFields ) );
        api.getLogger().info( "Preparing to execute si viewproject for " + fullConfigSyntax );
        Response viewRes = api.runCommand( siViewProjectCmd );

        // Iterate through the list of members returned by the API
        WorkItemIterator wit = viewRes.getWorkItems();
        while ( wit.hasNext() )
        {
            WorkItem wi = wit.next();
            if ( wi.getModelType().equals( SIModelTypeName.SI_SUBPROJECT ) )
            {
                // Save the configuration path for the current subproject, using the canonical path name
                pjConfigHash.put( wi.getField( "name" ).getValueAsString(), wi.getId() );
            }
            else if ( wi.getModelType().equals( SIModelTypeName.MEMBER ) )
            {
                // Figure out this member's parent project's canonical path name
                String parentProject = wi.getField( "parent" ).getValueAsString();
                // Instantiate our Integrity CM Member object
                Member iCMMember = new Member( wi, pjConfigHash.get( parentProject ), projectRoot, workspaceDir );
                // Add this to the full list of members in this project
                memberList.add( iCMMember );
            }
            else
            {
                api.getLogger().warn( "View project output contains an invalid model type: " + wi.getModelType() );
            }
        }

        // Sort the files list...
        Collections.sort( memberList, FILES_ORDER );
        return memberList;
    }

    /**
     * Performs a checkpoint on the Integrity SCM Project
     *
     * @param message Checkpoint description
     * @param tag     Checkpoint label
     * @return MKS API Response object
     * @throws APIException
     */
    public Response checkpoint( String message, String tag )
        throws APIException
    {
        // Setup the checkpoint command
        api.getLogger().debug( "Checkpointing project " + fullConfigSyntax + " with label '" + tag + "'" );
        // Construct the checkpoint command
        Command siCheckpoint = new Command( Command.SI, "checkpoint" );
        siCheckpoint.addOption( new Option( "recurse" ) );
        // Set the project name
        siCheckpoint.addOption( new Option( "project", fullConfigSyntax ) );
        // Set the label
        siCheckpoint.addOption( new Option( "label", tag ) );
        // Set the description, if specified
        if ( null != message && message.length() > 0 )
        {
            siCheckpoint.addOption( new Option( "description", message ) );
        }
        // Run the checkpoint command
        return api.runCommand( siCheckpoint );
    }

    /**
     * Creates a Development Path (project branch) for the MKS Integrity SCM Project
     *
     * @param devPath Development Path Name
     * @return MKS API Response object
     * @throws APIException
     */
    public Response createDevPath( String devPath )
        throws APIException
    {
        // First we need to obtain a checkpoint from the current configuration (normal or variant)
        String chkpt = projectRevision;
        if ( !isBuild() )
        {
            Response chkptRes = checkpoint( "Pre-checkpoint for development path " + devPath, devPath + " Baseline" );
            WorkItem wi = chkptRes.getWorkItem( fullConfigSyntax );
            chkpt = wi.getResult().getField( "resultant" ).getItem().getId();
        }

        // Now lets setup the create development path command
        api.getLogger().debug(
            "Creating development path '" + devPath + "' for project " + projectName + " at revision '" + chkpt + "'" );
        Command siCreateDevPath = new Command( Command.SI, "createdevpath" );
        siCreateDevPath.addOption( new Option( "devpath", devPath ) );
        // Set the project name
        siCreateDevPath.addOption( new Option( "project", projectName ) );
        // Set the checkpoint we want to create the development path from
        siCreateDevPath.addOption( new Option( "projectRevision", chkpt ) );
        // Run the create development path command
        return api.runCommand( siCreateDevPath );
    }
}

