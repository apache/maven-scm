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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.command.add.IntegrityAddCommand;
import org.apache.maven.scm.provider.integrity.command.blame.IntegrityBlameCommand;
import org.apache.maven.scm.provider.integrity.command.branch.IntegrityBranchCommand;
import org.apache.maven.scm.provider.integrity.command.changelog.IntegrityChangeLogCommand;
import org.apache.maven.scm.provider.integrity.command.checkin.IntegrityCheckInCommand;
import org.apache.maven.scm.provider.integrity.command.checkout.IntegrityCheckOutCommand;
import org.apache.maven.scm.provider.integrity.command.diff.IntegrityDiffCommand;
import org.apache.maven.scm.provider.integrity.command.edit.IntegrityEditCommand;
import org.apache.maven.scm.provider.integrity.command.export.IntegrityExportCommand;
import org.apache.maven.scm.provider.integrity.command.fileinfo.IntegrityFileInfoCommand;
import org.apache.maven.scm.provider.integrity.command.list.IntegrityListCommand;
import org.apache.maven.scm.provider.integrity.command.lock.IntegrityLockCommand;
import org.apache.maven.scm.provider.integrity.command.login.IntegrityLoginCommand;
import org.apache.maven.scm.provider.integrity.command.mkdir.IntegrityMkdirCommand;
import org.apache.maven.scm.provider.integrity.command.remove.IntegrityRemoveCommand;
import org.apache.maven.scm.provider.integrity.command.status.IntegrityStatusCommand;
import org.apache.maven.scm.provider.integrity.command.tag.IntegrityTagCommand;
import org.apache.maven.scm.provider.integrity.command.unedit.IntegrityUnEditCommand;
import org.apache.maven.scm.provider.integrity.command.unlock.IntegrityUnlockCommand;
import org.apache.maven.scm.provider.integrity.command.update.IntegrityUpdateCommand;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

/**
 * MKS Integrity SCM Provider for Maven
 * @version $Id: IntegrityScmProvider.java 1.7 2011/08/22 13:06:46EDT Cletus D'Souza (dsouza) Exp  $
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="integrity"
 */
public class IntegrityScmProvider extends AbstractScmProvider
{
	public static final String INTEGRITY_CM_URL = "[[user][/pass]@host[:port]]|configPath";
	
	/**
	 * Returns the name of our SCM Provider
	 */
    public String getScmType()
    {
        return "integrity";
    }
	
    /**
     * This class is the central point of the SCM provider. The Maven-SCM framework will know only this class in the provider, 
     * so this class will validate the scm url, populate the IntegrityScmProviderRepository and provide all commands that we support.
     * @param scmSpecificUrl The SCM URL specific to our implementation for this plugin
     * @param delimiter The character that separates the information above
     * @throws ScmRepositoryException
     */
    public ScmProviderRepository makeProviderScmRepository(String scmSpecificUrl, char delimiter) throws ScmRepositoryException
    {
    	// Initialize our variables need to create the IntegrityScmProvderRepository
    	String hostName = "";
    	int port = 0;
    	String userName = "";
    	String password = "";
    	String configPath = "";
    	
    	// Looking for a string in the following format:
    	//	[[user][/pass]@host[:port]]|configPath
    	// Where '|' is the delimiter...
    	String[] tokens = StringUtils.split(scmSpecificUrl, String.valueOf(delimiter));
    	// Expecting a minimum of one token to a maximum of two tokens
    	if( tokens.length < 1 || tokens.length > 2 )
    	{
    		 throw new ScmRepositoryException("Invalid SCM URL '" + scmSpecificUrl + "'.  Expecting a url using format: " + INTEGRITY_CM_URL);
    	}
    	else
    	{
    		// Inspect the first token to see if it contains connection information
    		if( tokens[0].indexOf('@') >= 0 )
    		{
    			// First split up the username and password string from the host:port information
    			String userPassStr = tokens[0].substring(0, tokens[0].indexOf('@'));
    			getLogger().debug("User/Password information supplied: " + userPassStr);
    			String hostPortStr = tokens[0].substring(tokens[0].indexOf('@')+1, tokens[0].length());
    			getLogger().debug("Host/Port information supplied: " + hostPortStr);
    			
    			if( userPassStr.length() > 0 )
    			{
    				// Next, make sure the username and password are separated using a forward slash '/'
    				int userPassDelimIndx = userPassStr.indexOf('/'); 
    				if(  userPassDelimIndx > 0 )
    				{
    					userName = userPassStr.substring(0, userPassStr.indexOf('/'));
    					if( userPassStr.length() > (userPassDelimIndx+1) )
    					{
    						password = userPassStr.substring(userPassStr.indexOf('/')+1, userPassStr.length());
    					}
    				}
    				else
    				{
    					userName = userPassStr;
    				}
    			}
    			// Now, check to see what we've got for the host:port information
    			if( hostPortStr.length() > 0 )
    			{
    				int hostPortDelimIndx = hostPortStr.indexOf(':'); 
    				if(  hostPortDelimIndx > 0 )
    				{
    					hostName = hostPortStr.substring(0, hostPortStr.indexOf(':'));
    					if( hostPortStr.length() > (hostPortDelimIndx+1) )
    					{
    						port = Integer.parseInt(hostPortStr.substring(hostPortStr.indexOf(':')+1, hostPortStr.length()));
    					}
    				}
    				else
    				{
    					hostName = hostPortStr;
    				}
    			}
    		}
    		// Grab the last token (or first token depends how you look at it)
    		configPath = tokens[tokens.length-1];
    	}
    	
        return new IntegrityScmProviderRepository(hostName, port, userName, password, configPath, getLogger());
    }

    /**
     * Maps to si connect and initialization of the project with si projectinfo
     */
    @Override
    protected LoginScmResult login(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
        IntegrityLoginCommand command = new IntegrityLoginCommand();
        command.setLogger(getLogger());
        return (LoginScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si rlog --rfilter=daterange:date1-date2
     */
    @Override
    protected ChangeLogScmResult changelog(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException
    {
    	IntegrityChangeLogCommand command = new IntegrityChangeLogCommand();
    	command.setLogger(getLogger());
    	return (ChangeLogScmResult) command.execute(repository, fileSet, parameters);
    }

    /**
     * Maps to si viewnonmembers and then si add for every non-member
     */
    @Override
    protected AddScmResult add(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityAddCommand command = new IntegrityAddCommand();
    	command.setLogger(getLogger());
    	return (AddScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si dropsandbox
     */
    @Override
    protected RemoveScmResult remove(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityRemoveCommand command = new IntegrityRemoveCommand();
    	command.setLogger(getLogger());
    	return (RemoveScmResult) command.execute(repository, fileSet, params);
    }

    
    /**
     * Maps to a si ci
     */
    @Override
    protected CheckInScmResult checkin(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityCheckInCommand command = new IntegrityCheckInCommand();
    	command.setLogger(getLogger());
    	return (CheckInScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si createsandbox and/or si resync
     */
    @Override
    protected CheckOutScmResult checkout(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityCheckOutCommand command = new IntegrityCheckOutCommand();
    	command.setLogger(getLogger());
    	return (CheckOutScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si diff
     */
    @Override
    protected DiffScmResult diff(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityDiffCommand command = new IntegrityDiffCommand();
    	command.setLogger(getLogger());
    	return (DiffScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si makewritable
     */
    @Override
    protected EditScmResult edit(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityEditCommand command = new IntegrityEditCommand();
    	command.setLogger(getLogger());
    	return (EditScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si viewsandbox with a filter of locally changed files
     */
    @Override
    protected StatusScmResult status(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityStatusCommand command = new IntegrityStatusCommand();
    	command.setLogger(getLogger());
    	return (StatusScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si checkpoint
     */
    @Override
    protected TagScmResult tag(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityTagCommand command = new IntegrityTagCommand();
    	command.setLogger(getLogger());
    	return (TagScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si revert
     */
    @Override
    protected UnEditScmResult unedit(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityUnEditCommand command = new IntegrityUnEditCommand();
    	command.setLogger(getLogger());
    	return (UnEditScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si resync
     */
    @Override
    protected UpdateScmResult update(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityUpdateCommand command = new IntegrityUpdateCommand();
    	command.setLogger(getLogger());
    	return (UpdateScmResult) command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si annotate
     */
    @Override
    protected BlameScmResult blame(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityBlameCommand command = new IntegrityBlameCommand();
    	command.setLogger(getLogger());
    	return (BlameScmResult) command.execute(repository, fileSet, params);
    }
    
    /**
     * Maps to si viewproject
     */
    @Override
    protected ListScmResult list(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityListCommand command = new IntegrityListCommand();
    	command.setLogger(getLogger());
    	return (ListScmResult) command.execute(repository, fileSet, params);
    }
    
    /**
     * Maps to si projectco (no sandbox is used)
     */
    @Override
    protected ExportScmResult export(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityExportCommand command = new IntegrityExportCommand();
    	command.setLogger(getLogger());
    	return (ExportScmResult) command.execute(repository, fileSet, params);
    }
    
    /**
     * Maps to si createdevpath
     */
    @Override
    protected BranchScmResult branch(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityBranchCommand command = new IntegrityBranchCommand();
    	command.setLogger(getLogger());
    	return (BranchScmResult) command.execute(repository, fileSet, params);
    }
    
    /**
     * Maps to si createsubproject
     */
    @Override
    protected MkdirScmResult mkdir(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityMkdirCommand command = new IntegrityMkdirCommand();
    	command.setLogger(getLogger());
    	return (MkdirScmResult) command.execute(repository, fileSet, params);
    }
    
    /**
     * Maps to si memberinfo
     */
    protected ScmResult fileinfo(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityFileInfoCommand command = new IntegrityFileInfoCommand();
    	command.setLogger(getLogger());
    	return command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si lock
     */
    protected ScmResult lock(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityLockCommand command = new IntegrityLockCommand();
    	command.setLogger(getLogger());
    	return command.execute(repository, fileSet, params);
    }

    /**
     * Maps to si unlock
     */
    protected ScmResult unlock(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters params) throws ScmException
    {
    	IntegrityUnlockCommand command = new IntegrityUnlockCommand(params.getString(CommandParameter.FILE));
    	command.setLogger(getLogger());
    	return command.execute(repository, fileSet, params);
    }    
}
