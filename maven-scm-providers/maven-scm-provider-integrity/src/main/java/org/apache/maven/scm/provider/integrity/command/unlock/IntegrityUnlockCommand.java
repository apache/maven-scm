package org.apache.maven.scm.provider.integrity.command.unlock;

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

import java.io.File;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.unlock.AbstractUnlockCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import com.mks.api.response.APIException;
import com.mks.api.response.Response;

/**
 * MKS Integrity implementation of Maven's AbstractUnlockCommand
 * <br>This command will run a 'si unlock' command for the specified filename 
 * @version $Id: IntegrityUnlockCommand.java 1.3 2011/08/22 13:06:40EDT Cletus D'Souza (dsouza) Exp  $
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 */
public class IntegrityUnlockCommand extends AbstractUnlockCommand 
{
	// This command will require the filename argument to be supplied for its execution
	private String filename;
	
	/**
	 * IntegrityUnlockCommand constructor requires a filename argument to be supplied for its execution
	 * <br>This avoids having to run the unlock command across the entire Sandbox.
	 * @param filename Relative path of the file needed to be unlocked
	 */
	public IntegrityUnlockCommand(String filename)
	{
		this.filename = filename;
	}
	
	/**
	 * {@inheritDoc}
	 */	
	@Override
	public ScmResult executeUnlockCommand(ScmProviderRepository repository, File workingDirectory) throws ScmException 
	{
		getLogger().info("Attempting to unlock file: " + filename);		
		if( null == filename || filename.length() == 0 )
		{
			throw new ScmException("A single filename is required to execute the unlock command!");
		}

		ScmResult result;
		IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository; 
    	try
    	{
    		Sandbox siSandbox = iRepo.getSandbox();
    		File memberFile = new File(workingDirectory.getAbsoluteFile() + File.separator + filename); 
    		Response res = siSandbox.unlock(memberFile, filename);
    		int exitCode = res.getExitCode();
    		boolean success = (exitCode == 0 ? true : false);
    		result = new ScmResult(res.getCommandString(), "", "Exit Code: " + exitCode, success); 
    	}
    	catch(APIException aex)
    	{
    		ExceptionHandler eh = new ExceptionHandler(aex);
    		getLogger().error("MKS API Exception: " + eh.getMessage());
    		getLogger().info(eh.getCommand() + " exited with return code " + eh.getExitCode());
    		result = new ScmResult(eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false);
    	}
    	
    	return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ScmResult executeCommand(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException 
	{
		filename = parameters.getString(CommandParameter.FILE);
		return executeUnlockCommand(repository, fileSet.getBasedir());
	}

}
