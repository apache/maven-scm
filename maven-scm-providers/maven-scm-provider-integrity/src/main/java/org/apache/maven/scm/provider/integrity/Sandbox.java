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
import com.mks.api.Option;
import com.mks.api.MultiValue;
import com.mks.api.response.Field;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItemIterator;
import com.mks.api.response.WorkItem;
import com.mks.api.response.Item;
import com.mks.api.si.SIModelTypeName;
import com.mks.api.response.APIException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.codehaus.plexus.util.StringUtils;

/**
 * This class represents an MKS Integrity Sandbox and provides an encapsulation
 * for executing typical Sandbox operations
 * @version $Id: Sandbox.java 1.11 2011/08/22 13:06:50EDT Cletus D'Souza (dsouza) Exp  $
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 */ 
public class Sandbox
{
	// Our date format
	public static final SimpleDateFormat RLOG_DATEFORMAT = new SimpleDateFormat("MMMMM d, yyyy - h:mm:ss a");
	
	// File Separator
	private String fs = System.getProperty("file.separator");
	
    // MKS API Session Object
    private APISession api;

    // Other sandbox specific class variables
    private Project siProject;
    private String sandboxDir;
    private String cpid;
    
    // Flag to indicate the overall add operation was successful
    private boolean addSuccess;

    // Flag to indicate the overall check-in operation was successful
    private boolean ciSuccess;

    /**
     * Fixes the default includes/excludes patterns for compatibility with MKS Integrity's 'si viewnonmembers' command
     * @param pattern String pattern representing the includes/excludes file/directory list
     */
	public static String formatFilePatterns(String pattern)
	{
		StringBuilder sb = new StringBuilder();
		if( null != pattern && pattern.length() > 0 )
		{
			String[] tokens = StringUtils.split(pattern, ",");
			for( int i = 0; i < tokens.length; i++ )
			{
				String tkn = tokens[i].trim();
				if( tkn.indexOf("file:") != 0 && tkn.indexOf("dir:") != 0 )
				{
					sb.append(tkn.indexOf('.') > 0 ? StringUtils.replaceOnce(tkn, "**/", "file:") : 
													StringUtils.replaceOnce(tkn, "**/", "dir:"));
				}
				else
				{
					sb.append(tkn);
				}
				sb.append(i < tokens.length ? "," : "");
			}
		}
		return sb.toString();
	}
    
	/**
	 * The Sandbox constructor 
	 * @param api MKS API Session object
	 * @param cmProject Project object
	 * @param dir Absolute path to the location for the Sandbox directory
	 */
    public Sandbox(APISession api, Project cmProject, String dir)
    {
        siProject = cmProject;
        sandboxDir = dir;
        this.api = api;
        cpid = System.getProperty("maven.scm.integrity.cpid");
        cpid = ((null == cpid || cpid.length() == 0) ? ":none" : cpid);
        addSuccess = true;
        ciSuccess = true;
    }
    
    /**
     * Attempts to figure out if the current sandbox already exists and is valid
     * @param sandbox The client-side fully qualified path to the sandbox pj
     * @return true/false depending on whether or not this location has a valid sandbox
     * @throws APIException
     */
    private boolean isValidSandbox(String sandbox) throws APIException
    {
        Command cmd = new Command(Command.SI, "sandboxinfo");
        cmd.addOption(new Option("sandbox", sandbox));

        api.getLogger().debug("Validating existing sandbox: " + sandbox);
        Response res = api.runCommand(cmd);
        WorkItemIterator wit = res.getWorkItems();
        try
        {
	        WorkItem wi = wit.next();
	        return wi.getField("fullConfigSyntax").getValueAsString().equalsIgnoreCase(siProject.getConfigurationPath());
        }
        catch(APIException aex)
        {
        	ExceptionHandler eh = new ExceptionHandler(aex);
        	api.getLogger().error("MKS API Exception: " + eh.getMessage());
        	api.getLogger().debug(eh.getCommand() + " completed with exit code " + eh.getExitCode());
        	return false;
        }
    }
    
    /**
     * Inspects the MKS API Response object's Item field to determine whether or nor a working file delta exists
     * @param wfdelta MKS API Response object's Item representing the Working File Delta
     * @return true if the working file is a delta; false otherwise
     */
    private boolean isDelta(Item wfdelta)
    {
        // Return false if there is no working file
        if( wfdelta.getField("isDelta").getBoolean().booleanValue() )
        {
            return true;
        }
        else
        {
        	return false;
        }
    }
    
    /**
     * Executes a 'si add' command using the message for the description
     * @param memberFile Full path to the new member's location
     * @param message Description for the new member's archive
     * @return MKS API Response object
     * @throws APIException
     */
    private Response add(File memberFile, String message) throws APIException
    {
		// Setup the add command    	
        api.getLogger().info("Adding member: " + memberFile.getAbsolutePath());			
        Command siAdd = new Command(Command.SI, "add");
        siAdd.addOption(new Option("onExistingArchive", "sharearchive"));
        siAdd.addOption(new Option("cpid", cpid));
        if( null != message && message.length() > 0 ){ siAdd.addOption(new Option("description", message)); }
        siAdd.addOption(new Option("cwd", memberFile.getParentFile().getAbsolutePath()));
        siAdd.addSelection(memberFile.getName());
        return api.runCommand(siAdd);
    }
    
    /**
     * Executes a 'si ci' command using the relativeName for the member name and message for the description
     * @param memberFile Full path to the member's current sandbox location
     * @param relativeName Relative path from the nearest subproject or project
     * @param message Description for checking in the new update
     * @return MKS API Response object
     * @throws APIException
     */
    private Response checkin(File memberFile, String relativeName, String message) throws APIException
    {
		// Setup the check-in command    	
        api.getLogger().info("Checking in member:  " + memberFile.getAbsolutePath());			
        Command sici = new Command(Command.SI, "ci");
        sici.addOption(new Option("cpid", cpid));
        if( null != message && message.length() > 0 ){ sici.addOption(new Option("description", message)); }
        sici.addOption(new Option("cwd", memberFile.getParentFile().getAbsolutePath()));
        sici.addSelection(relativeName);
        return api.runCommand(sici);
    }
    
    /**
     * Executes a 'si drop' command using the relativeName for the member name
     * @param memberFile Full path to the member's current sandbox location
     * @param relativeName Relative path from the nearest subproject or project
     * @return MKS API Response object
     * @throws APIException
     */
    private Response dropMember(File memberFile, String relativeName) throws APIException
    {
		// Setup the drop command    	
        api.getLogger().info("Dropping member " + memberFile.getAbsolutePath());			    		
        Command siDrop = new Command(Command.SI, "drop");
        siDrop.addOption(new Option("cwd", memberFile.getParentFile().getAbsolutePath()));
        siDrop.addOption(new Option("noconfirm"));
        siDrop.addOption(new Option("cpid", cpid));
        siDrop.addOption(new Option("delete"));
        siDrop.addSelection(relativeName);
        return api.runCommand(siDrop);
    }    
    
    /**
     * Executes a 'si diff' command to see if the working file has actually changed.  Even though the
     * working file delta might be true, that doesn't always mean the file has actually changed.
     * @param memberFile Full path to the member's current sandbox location
     * @param relativeName Relative path from the nearest subproject or project
     * @return MKS API Response object
     */
    private boolean hasMemberChanged(File memberFile, String relativeName)
    {
    	// Setup the differences command
		Command siDiff = new Command(Command.SI, "diff");
		siDiff.addOption(new Option("cwd", memberFile.getParentFile().getAbsolutePath()));
		siDiff.addSelection(relativeName);
		try
		{
			// Run the diff command...
			Response res = api.runCommand(siDiff);
			try
			{
				// Return the changed flag...
				return res.getWorkItems().next().getResult().getField("resultant").getItem().getField("different").getBoolean().booleanValue();
			}
			catch(NullPointerException npe)
			{
	    		api.getLogger().warn("Couldn't figure out differences for file: " + memberFile.getAbsolutePath());
				api.getLogger().warn("Null value found along response object for WorkItem/Result/Field/Item/Field.getBoolean()");
				api.getLogger().warn("Proceeding with the assumption that the file has changed!");				
			}
		}
		catch(APIException aex)
		{
    		ExceptionHandler eh = new ExceptionHandler(aex);
    		api.getLogger().warn("Couldn't figure out differences for file: " + memberFile.getAbsolutePath());
			api.getLogger().warn(eh.getMessage());
			api.getLogger().warn("Proceeding with the assumption that the file has changed!");
			api.getLogger().debug(eh.getCommand() + " completed with exit Code " + eh.getExitCode());			
		}
		return true;	
    }
    	
    /**
     * Returns the full path name to the current Sandbox directory
     * @return
     */
    public String getSandboxDir()
    {
    	return sandboxDir;
    }
    
    /**
     * Executes a 'si lock' command using the relativeName of the file
     * @param memberFile Full path to the member's current sandbox location
     * @param relativeName Relative path from the nearest subproject or project
     * @return MKS API Response object
     * @throws APIException
     */
	public Response lock(File memberFile, String relativeName) throws APIException
	{
		// Setup the lock command
        api.getLogger().debug("Locking member: " + memberFile.getAbsolutePath());			
		Command siLock = new Command(Command.SI, "lock");
		siLock.addOption(new Option("revision", ":member"));
		siLock.addOption(new Option("cpid", cpid));
		siLock.addOption(new Option("cwd", memberFile.getParentFile().getAbsolutePath()));
		siLock.addSelection(relativeName);
		// Execute the lock command
		return api.runCommand(siLock);
	}

	/**
	 * Executes a 'si unlock' command using the relativeName of the file
	 * @param memberFile Full path to the member's current sandbox location
	 * @param relativeName Relative path from the nearest subproject or project
	 * @return MKS API Response object
	 * @throws APIException
	 */
	public Response unlock(File memberFile, String relativeName) throws APIException
	{
		// Setup the unlock command
        api.getLogger().debug("Unlocking member: " + memberFile.getAbsolutePath());			
		Command siUnlock = new Command(Command.SI, "unlock");
		siUnlock.addOption(new Option("revision", ":member"));
		siUnlock.addOption(new Option("action", "remove"));
		siUnlock.addOption(new Option("cwd", memberFile.getParentFile().getAbsolutePath()));
		siUnlock.addSelection(relativeName);
		// Execute the unlock command
		return api.runCommand(siUnlock);
	}
	
    /**
     * Removes the registration for the Sandbox in the user's profile
     * @return The API Response associated with executing this command
     * @throws APIException 
     */
    public Response drop() throws APIException
    {
    	File project = new File(siProject.getProjectName());
    	File sandboxpj = new File(sandboxDir + fs + project.getName());
    	
    	// Check to see if the sandbox file already exists and its OK to use
    	api.getLogger().debug("Sandbox Project File: " + sandboxpj.getAbsolutePath());    	
		Command cmd = new Command(Command.SI, "dropsandbox");
		cmd.addOption(new Option("delete", "members"));
		cmd.addOption(new Option("sandbox", sandboxpj.getAbsolutePath()));    			
		cmd.addOption(new Option("cwd", sandboxDir));
		return api.runCommand(cmd);
    }
    
    /**
     * Creates a new Sandbox in the sandboxDir specified
     * @return true if the operation is successful; false otherwise
     * @throws APIException 
     */
    public boolean create() throws APIException
    {
    	File project = new File(siProject.getProjectName());
    	File sandboxpj = new File(sandboxDir + fs + project.getName());
    	
    	// Check to see if the sandbox file already exists and its OK to use
    	api.getLogger().debug("Sandbox Project File: " + sandboxpj.getAbsolutePath());    	
    	if( sandboxpj.isFile() )
    	{
    		// Validate this sandbox
    		if( isValidSandbox(sandboxpj.getAbsolutePath()) )
    		{
    			api.getLogger().debug("Reusing existing Sandbox in " + sandboxDir + " for project " + siProject.getConfigurationPath());
    			return true;
    		}
    		else
    		{
    			api.getLogger().error("An invalid Sandbox exists in " + sandboxDir + ". Please provide a different location!");
    			return false;
    		}
    	}
    	else // Create a new sandbox in the location specified
    	{
    		api.getLogger().debug("Creating Sandbox in " + sandboxDir + " for project " + siProject.getConfigurationPath());
    		try
    		{
    			Command cmd = new Command(Command.SI, "createsandbox");
    			cmd.addOption(new Option("recurse"));
    			cmd.addOption(new Option("nopopulate"));
    			cmd.addOption(new Option("project", siProject.getConfigurationPath()));
    			cmd.addOption(new Option("cwd", sandboxDir));
    			api.runCommand(cmd);
    		}
    		catch(APIException aex)
    		{
    			// Check to see if this exception is due an existing sandbox registry entry
    			ExceptionHandler eh = new ExceptionHandler(aex);
    			if( eh.getMessage().indexOf("There is already a registered entry") > 0 )
    			{
    				// This will re-validate the sandbox, if Maven blew away the old directory
    				return create();
    			}
    			else
    			{
    				throw aex;
    			}
    		}
    		return true;
    	}
    }

    /**
     * Resynchronizes an existing Sandbox
     * Assumes that the create() call has already been made to validate this sandbox
     * @throws APIException 
     */
    public Response resync() throws APIException
    {
		api.getLogger().debug("Resynchronizing Sandbox in " + sandboxDir + " for project " + siProject.getConfigurationPath());
		Command cmd = new Command(Command.SI, "resync");
		cmd.addOption(new Option("recurse"));
		cmd.addOption(new Option("populate"));
		cmd.addOption(new Option("cwd", sandboxDir));
		return api.runCommand(cmd);
    }

    /**
     * Executes a 'si makewritable' command to allow edits to all files in the Sandbox directory
     * @return MKS API Response object
     * @throws APIException
     */
    public Response makeWriteable() throws APIException
    {
		api.getLogger().debug("Setting files to writeable in " + sandboxDir + " for project " + siProject.getConfigurationPath());
		Command cmd = new Command(Command.SI, "makewritable");
		cmd.addOption(new Option("recurse"));
		cmd.addOption(new Option("cwd", sandboxDir));
		return api.runCommand(cmd);    	
    }

    /**
     * Executes a 'si revert' command to roll back changes to all files in the Sandbox directory
     * @return MKS API Response object
     * @throws APIException
     */
    public Response revertMembers() throws APIException
    {
		api.getLogger().debug("Reverting changes in sandbox " + sandboxDir + " for project " + siProject.getConfigurationPath());
		Command cmd = new Command(Command.SI, "revert");
		cmd.addOption(new Option("recurse"));
		cmd.addOption(new Option("cwd", sandboxDir));
		return api.runCommand(cmd);    	
    }
    
    /**
     * Executes a 'si viewnonmembers' command filtering the results using the exclude and include lists
     * @param exclude Pattern containing the exclude file list
     * @param include Pattern containing the include file list
     * @return List of ScmFile objects representing the new files in the Sandbox
     * @throws APIException
     */
    public List<ScmFile> getNewMembers(String exclude, String include) throws APIException
    {
    	// Store a list of files that were added to the repository
    	List<ScmFile> filesAdded = new ArrayList<ScmFile>();    	
    	Command siViewNonMem = new Command(Command.SI, "viewnonmembers");
        siViewNonMem.addOption(new Option("recurse"));
        if( null != exclude && exclude.length() > 0 ){ siViewNonMem.addOption(new Option("exclude", exclude)); }
        if( null != include && include.length() > 0 ){ siViewNonMem.addOption(new Option("include", include)); }       
        siViewNonMem.addOption(new Option("noincludeFormers"));
        siViewNonMem.addOption(new Option("cwd", sandboxDir));
        Response response = api.runCommand(siViewNonMem);
        for(WorkItemIterator wit = response.getWorkItems(); wit.hasNext();)
        {
        	filesAdded.add(new ScmFile(wit.next().getField("absolutepath").getValueAsString(), ScmFileStatus.ADDED));
        }
        return filesAdded;
    	
    }
    
    /**
     * Adds a list of files to the MKS Integrity SCM Project
     * @param exclude Pattern containing the exclude file list
     * @param include Pattern containing the include file list
     * @param message Description for the member's archive
     * @return
     */
    public List<ScmFile> addNonMembers(String exclude, String include, String message)
    {
    	// Re-initialize the overall addSuccess to be true for now
    	addSuccess = true;
    	// Store a list of files that were actually added to the repository
    	List<ScmFile> filesAdded = new ArrayList<ScmFile>();
    	api.getLogger().debug("Looking for new members in sandbox dir: " + sandboxDir);
        try
        {
        	List<ScmFile> newFileList = getNewMembers(exclude, include);
	        for( Iterator<ScmFile> sit = newFileList.iterator(); sit.hasNext();)
	        {
	        	try
	        	{
	        		ScmFile localFile = sit.next();
	        		// Attempt to add the file to the Integrity repository
	        		add(new File(localFile.getPath()), message);
	        		// If it was a success, then add it to the list of files that were actually added
	        		filesAdded.add(localFile);
	        	}
	            catch(APIException aex)
	            {
	            	// Set the addSuccess to false, since we ran into a problem
	            	addSuccess = false;
	        		ExceptionHandler eh = new ExceptionHandler(aex);
	    			api.getLogger().error("MKS API Exception: " + eh.getMessage());
	    			api.getLogger().debug(eh.getCommand() + " completed with exit Code " + eh.getExitCode());
	            }	        	
	        }
    	}
        catch(APIException aex)
        {
        	// Set the addSuccess to false, since we ran into a problem
        	addSuccess = false;
    		ExceptionHandler eh = new ExceptionHandler(aex);
			api.getLogger().error("MKS API Exception: " + eh.getMessage());
			api.getLogger().debug(eh.getCommand() + " completed with exit Code " + eh.getExitCode());
        }
        return filesAdded;
    }

    /**
     * Returns the overall success of the add operation
     * @return
     */
    public boolean getOverallAddSuccess()
    {
    	return addSuccess;
    }

    /**
     * Inspects the MKS API Response object's Item field to determine whether or nor a working file exists
     * @param wfdelta MKS API Response object's Item representing the Working File Delta
     * @return
     */
    public boolean hasWorkingFile(Item wfdelta)
    {
        // Return false if there is no working file
        if( wfdelta.getField("noWorkingFile").getBoolean().booleanValue() )
        {
            return false;
        }
        else
        {
        	return true;
        }
    }
    
    /**
     * Executes a 'si viewsandbox' and parses the output for changed or dropped working files
     * @return A list of MKS API Response WorkItem objects representing the changes in the Sandbox
     * @throws APIException
     */
    public List<WorkItem> getChangeList() throws APIException
    {
    	// Store a list of files that were changed/removed to the repository
    	List<WorkItem> changedFiles = new ArrayList<WorkItem>();
    	// Setup the view sandbox command to figure out what has changed...
        Command siViewSandbox = new Command(Command.SI, "viewsandbox");
        // Create the --fields option
        MultiValue mv = new MultiValue(",");
        mv.add("name");
        mv.add("context");
        mv.add("wfdelta");
        mv.add("memberarchive");
        siViewSandbox.addOption(new Option("fields", mv));
        siViewSandbox.addOption(new Option("recurse"));
        siViewSandbox.addOption(new Option("noincludeDropped"));
        siViewSandbox.addOption(new Option("filterSubs"));
        siViewSandbox.addOption(new Option("cwd", sandboxDir));
        
        // Run the view sandbox command
        Response r = api.runCommand(siViewSandbox);
        // Check-in all changed files, drop all members with missing working files
        for( WorkItemIterator wit = r.getWorkItems(); wit.hasNext(); )
        {
            WorkItem wi = wit.next();
            api.getLogger().debug("Inspecting file: " + wi.getField("name").getValueAsString());
            
            if( wi.getModelType().equals(SIModelTypeName.MEMBER) )
            {
            	Item wfdeltaItem = (Item)wi.getField("wfdelta").getValue();
            	// Proceed with this entry only if it is an actual working file delta
            	if( isDelta(wfdeltaItem) )
            	{
	                File memberFile = new File(wi.getField("name").getValueAsString());
	                if( hasWorkingFile(wfdeltaItem) )
	                {
	            		// Only report on files that have actually changed...                	
	                	if( hasMemberChanged(memberFile, wi.getId()) )
	                	{
	                		changedFiles.add(wi);		                		
	                	}
	                }
	                else
	                {
	            		// Also report on dropped files
	            		changedFiles.add(wi);		                		
	                }
            	}
            }
        }
        return changedFiles;
    }
    
    /**
     * Wrapper function to check-in all changes and drop members associated with missing working files
     * @param message Description for the changes
     * @return
     */
    public List<ScmFile> checkInUpdates(String message)
    {
    	// Re-initialize the overall ciSuccess to be true for now
    	ciSuccess = true;
    	// Store a list of files that were changed/removed to the repository
    	List<ScmFile> changedFiles = new ArrayList<ScmFile>();
    	api.getLogger().debug("Looking for changed and dropped members in sandbox dir: " + sandboxDir);
    	
        try
        {
	        // Let the list of changed files
        	List<WorkItem> changeList = getChangeList();
	        // Check-in all changed files, drop all members with missing working files
	        for( Iterator<WorkItem> wit = changeList.iterator(); wit.hasNext(); )
	        {
	        	try
	        	{
	        		WorkItem wi = wit.next();
	        		File memberFile = new File(wi.getField("name").getValueAsString());
            		// Check-in files that have actually changed...
	        		if( hasWorkingFile((Item)wi.getField("wfdelta").getValue()) )
	        		{
                		// Lock each member as you go...
                		lock(memberFile, wi.getId());
                		// Commit the changes...
                		checkin(memberFile, wi.getId(), message);
                		// Update the changed file list
                		changedFiles.add(new ScmFile(memberFile.getAbsolutePath(), ScmFileStatus.CHECKED_IN));		                		
                	}
	                else
	                {
	                    // Drop the member if there is no working file
	                    dropMember(memberFile, wi.getId());
                		// Update the changed file list
	                    changedFiles.add(new ScmFile(memberFile.getAbsolutePath(), ScmFileStatus.DELETED));		                		
	                }
	        	}
	            catch(APIException aex)
	            {
	            	// Set the ciSuccess to false, since we ran into a problem
	            	ciSuccess = false;
	        		ExceptionHandler eh = new ExceptionHandler(aex);
	    			api.getLogger().error("MKS API Exception: " + eh.getMessage());
	    			api.getLogger().debug(eh.getCommand() + " completed with exit Code " + eh.getExitCode());
	            }	        	
	        }
        }
        catch(APIException aex)
        {
        	// Set the ciSuccess to false, since we ran into a problem
        	ciSuccess = false;
    		ExceptionHandler eh = new ExceptionHandler(aex);
			api.getLogger().error("MKS API Exception: " + eh.getMessage());
			api.getLogger().debug(eh.getCommand() + " completed with exit Code " + eh.getExitCode());
        }        
        
        return changedFiles;
    }

    /**
     * Returns the overall success of the check-in operation
     * @return
     */
    public boolean getOverallCheckInSuccess()
    {
    	return ciSuccess;
    }

    /**
     * Creates one subproject per directory, as required.
     * @param dirPath A relative path structure of folders
     * @return Response containing the result for the created subproject
     * @throws APIException 
     */
    public Response createSubproject(String dirPath) throws APIException
    {
		// Setup the create subproject command
        api.getLogger().debug("Creating subprojects for: " + dirPath + "/project.pj");			
		Command siCreateSubproject = new Command(Command.SI, "createsubproject");
		siCreateSubproject.addOption(new Option("cpid", cpid));
		siCreateSubproject.addOption(new Option("createSubprojects"));
		siCreateSubproject.addOption(new Option("cwd", sandboxDir));
		siCreateSubproject.addSelection(dirPath + "/project.pj");
		// Execute the create subproject command
		return api.runCommand(siCreateSubproject);
    }
    
    /**
     * Executes the 'si rlog' command to generate a list of changed revision found between startDate and endDate
     * @param startDate The date range for the beginning of the operation
     * @param endDate The date range for the end of the operation
     * @return ChangeLogSet containing a list of changes grouped by Change Pacakge ID
     * @throws APIException
     */
    public ChangeLogSet getChangeLog(Date startDate, Date endDate) throws APIException
    {
    	// Initialize our return object
    	ChangeLogSet changeLog = new ChangeLogSet(startDate, endDate);
    	// By default we're going to group-by change package
    	// Non change package changes will be lumped into one big Change Set
    	Hashtable<String, ChangeSet> changeSetHash = new Hashtable<String, ChangeSet>();
    	
    	// Lets prepare our si rlog command for execution
    	Command siRlog = new Command(Command.SI, "rlog");
        siRlog.addOption(new Option("recurse"));
        MultiValue rFilter = new MultiValue(":");
        rFilter.add("daterange");
        rFilter.add("'" + RLOG_DATEFORMAT.format(startDate) + "'-'" + RLOG_DATEFORMAT.format(endDate) + "'");
        siRlog.addOption(new Option("rfilter", rFilter));
        siRlog.addOption(new Option("cwd", sandboxDir));
        // Execute the si rlog command
        Response response = api.runCommand(siRlog);
        for(WorkItemIterator wit = response.getWorkItems(); wit.hasNext();)
        {
        	WorkItem wi = wit.next();
        	String memberName = wi.getContext();
        	// We're going to have to do a little dance to get the correct server file name
        	memberName = memberName.substring(0, memberName.lastIndexOf('/'));
        	memberName = memberName + '/' + wi.getId();
        	memberName = memberName.replace('\\', '/');
        	// Now lets get the revisions for this file
        	Field revisionsFld = wi.getField("revisions");
        	if( null != revisionsFld && revisionsFld.getDataType().equals(Field.ITEM_LIST_TYPE) && null != revisionsFld.getList() )
        	{
        		@SuppressWarnings("unchecked")
				List<Item> revList = revisionsFld.getList();
        		for( Iterator<Item> lit = revList.iterator(); lit.hasNext(); )
        		{
        			Item revisionItem = lit.next();
        			String revision = revisionItem.getId();
        			String author = revisionItem.getField("author").getItem().getId();
        			// Attempt to get the full name, if available
        			try { author = revisionItem.getField("author").getItem().getField("fullname").getValueAsString(); }
        			catch(NullPointerException npe){ /* ignore */ }
        			String cpid = ":none";
        			// Attempt to get the cpid for this revision
        			try { cpid = revisionItem.getField("cpid").getItem().getId(); }
        			catch(NullPointerException npe){ /* ignore */ }
        			// Get the Change Package summary for this revision
        			String comment = cpid + ": " + revisionItem.getField("cpsummary").getValueAsString();
        			// Get the date associated with this revision
        			Date date = revisionItem.getField("date").getDateTime();
     
        			// Lets create our ChangeFile based on the information we've gathered so far
        			ChangeFile changeFile = new ChangeFile(memberName, revision); 

        			// Check to see if we already have a ChangeSet grouping for this revision
        			ChangeSet changeSet = changeSetHash.get(cpid);
        			if( null != changeSet )
        			{
        				// Set the date of the ChangeSet to the oldest entry
        				if( changeSet.getDate().after(date) ){ changeSet.setDate(date); }
        				// Add the new ChangeFile
        				changeSet.addFile(changeFile);
        				// Update the changeSetHash
        				changeSetHash.put(cpid, changeSet);
        			}
        			else // Create a new ChangeSet grouping and add the ChangeFile
        			{
        				List<ChangeFile> changeFileList = new ArrayList<ChangeFile>();
        				changeFileList.add(changeFile);
        				changeSet = new ChangeSet(date, comment, author, changeFileList);
        				// Update the changeSetHash with an initial entry for the cpid
        				changeSetHash.put(cpid, changeSet);
        			}
        		}
        	}
        	
        }
    	
        // Update the Change Log with the Change Sets
        List<ChangeSet> changeSetList = new ArrayList<ChangeSet>();
        changeSetList.addAll(changeSetHash.values());
        changeLog.setChangeSets(changeSetList);
        
    	return changeLog;
    }
}