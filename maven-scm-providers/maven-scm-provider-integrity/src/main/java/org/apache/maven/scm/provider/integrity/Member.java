package org.apache.maven.scm.provider.integrity;

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

import com.mks.api.Command;
import com.mks.api.FileOption;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;

import java.io.File;
import java.util.Date;

/**
 * This class represents an Integrity SCM Member
 * <br>It contains all the necessary metadata to check this file out individually
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @since 1.6
 */
public class Member
{
    private String memberID;

    private String memberName;

    private Date memberTimestamp;

    private String memberDescription;

    private String projectConfigPath;

    private String memberRev;

    private File targetFile;

    private String relativeFile;

    private String lineTerminator;

    private String overwriteExisting;

    private String restoreTimestamp;

    /**
     * This class represents an MKS Integrity Source File
     * It needs the Member Name (relative path to pj), Full Member Path, Project Configuration Path, Revision,
     * Project's Root Path, and the current Workspace directory (to compute the working file path) for its
     * instantiation.  This helper class will be used to then perform a project checkout from the repository
     *
     * @param wi           A MKS API Response Work Item representing metadata related to a Integrity Member
     * @param configPath   Configuration Path for this file's project/subproject
     * @param projectRoot  Full path to the root location for this file's parent project
     * @param workspaceDir Full path to the workspace root directory
     */
    public Member( WorkItem wi, String configPath, String projectRoot, String workspaceDir )
    {
        // Initialize our parent with the information needed
        this.projectConfigPath = configPath;
        this.memberID = wi.getId();
        this.memberName = wi.getField( "name" ).getValueAsString();
        this.memberRev = wi.getField( "memberrev" ).getItem().getId();
        this.memberTimestamp = wi.getField( "membertimestamp" ).getDateTime();
        if ( null != wi.getField( "memberdescription" ) && null != wi.getField(
            "memberdescription" ).getValueAsString() )
        {
            this.memberDescription = wi.getField( "memberdescription" ).getValueAsString();
        }
        else
        {
            this.memberDescription = new String( "" );
        }
        this.lineTerminator = "native";
        this.overwriteExisting = "overwriteExisting";
        this.restoreTimestamp = "restoreTimestamp";
        this.relativeFile = this.memberName.substring( projectRoot.length() );
        this.targetFile = new File( workspaceDir + relativeFile );
    }

    /**
     * Returns a string representation of this file's full path name, where it will checked out to disk for the build.
     *
     * @return
     */
    public String getTargetFilePath()
    {
        return targetFile.getAbsolutePath();
    }

    /**
     * Returns a string representation of this member's revision
     *
     * @return
     */
    public String getRevision()
    {
        return memberRev;
    }

    /**
     * Returns the date/time associated with this member revision
     *
     * @return
     */
    public Date getTimestamp()
    {
        return memberTimestamp;
    }

    /**
     * Returns any check-in comments associated with this revision
     *
     * @return
     */
    public String getDescription()
    {
        return memberDescription;
    }

    /**
     * Returns the full server-side member path for this member
     *
     * @return
     */
    public String getMemberName()
    {
        return memberName;
    }

    /**
     * Returns only the file name portion for this full server-side member path
     *
     * @return
     */
    public String getName()
    {
        if ( memberID.indexOf( '/' ) > 0 )
        {
            return memberID.substring( memberID.lastIndexOf( '/' ) + 1 );
        }
        else if ( memberID.indexOf( '\\' ) > 0 )
        {
            return memberID.substring( memberID.lastIndexOf( '\\' ) + 1 );
        }
        else
        {
            return memberID;
        }
    }

    /**
     * Optionally, one may set a line terminator, if the default is not desired.
     *
     * @param lineTerminator
     */
    public void setLineTerminator( String lineTerminator )
    {
        this.lineTerminator = lineTerminator;
    }

    /**
     * Optionally, one may choose not to overwrite existing files, this may speed up the synchronization process.
     *
     * @param overwriteExisting
     */
    public void setOverwriteExisting( String overwriteExisting )
    {
        this.overwriteExisting = overwriteExisting;
    }

    /**
     * Optionally, one might want to restore the timestamp, if the build is smart not to recompile files that were not
     * touched.
     *
     * @param restoreTimestamp
     */
    public void setRestoreTimestamp( boolean restoreTime )
    {
        if ( restoreTime )
        {
            this.restoreTimestamp = "restoreTimestamp";
        }
        else
        {
            this.restoreTimestamp = "norestoreTimestamp";
        }
    }

    /**
     * Performs a checkout of this MKS Integrity Source File to a working file location on the build server represented
     * by targetFile
     *
     * @param api MKS API Session
     * @return true if the operation succeeded or false if failed
     * @throws APIException
     */
    public boolean checkout( APISession api )
        throws APIException
    {
        // Make sure the directory is created
        if ( !targetFile.getParentFile().isDirectory() )
        {
            targetFile.getParentFile().mkdirs();
        }
        // Construct the project check-co command
        Command coCMD = new Command( Command.SI, "projectco" );
        coCMD.addOption( new Option( overwriteExisting ) );
        coCMD.addOption( new Option( "nolock" ) );
        coCMD.addOption( new Option( "project", projectConfigPath ) );
        coCMD.addOption( new FileOption( "targetFile", targetFile ) );
        coCMD.addOption( new Option( restoreTimestamp ) );
        coCMD.addOption( new Option( "lineTerminator", lineTerminator ) );
        coCMD.addOption( new Option( "revision", memberRev ) );
        // Add the member selection
        coCMD.addSelection( memberID );

        // Execute the checkout command
        Response res = api.runCommand( coCMD );

        // Return true if we were successful
        return ( res.getExitCode() == 0 );
    }

    /**
     * Uses the name of file for equality check
     */
    @Override
    public boolean equals( Object o )
    {
        if ( o instanceof Member )
        {
            return ( (Member) o ).getMemberName().equals( this.getMemberName() );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getMemberName().hashCode();
    }
}
