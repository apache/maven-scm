package org.apache.maven.scm.provider.integrity.command.branch;

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

import java.util.ArrayList;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Project;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import com.mks.api.response.APIException;
import com.mks.api.response.Response;

/**
 * MKS Integrity implementation for Maven's AbstractBranchCommand
 * <br>For a normal and variant configuration, a fresh checkpoint is executed
 * prior to creating a Development Path (branch).  In the case of a build
 * configuration, the specified checkpoint revision is used to create
 * the Development Path
 * @version $Id: IntegrityBranchCommand.java 1.3 2011/08/22 13:06:17EDT Cletus D'Souza (dsouza) Exp  $
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 */
public class IntegrityBranchCommand extends AbstractBranchCommand 
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BranchScmResult executeBranchCommand(ScmProviderRepository repository, ScmFileSet fileSet, 
												String branchName, String message) throws ScmException 
	{
		BranchScmResult result;
		IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository; 
		Project siProject = iRepo.getProject();
		getLogger().info("Attempting to branch project " + siProject.getProjectName() + " using branch name '" + branchName + "'");
    	try
    	{
    		Project.validateTag(branchName);
    		Response res = siProject.createDevPath(branchName);
    		int exitCode = res.getExitCode();
    		boolean success = (exitCode == 0 ? true : false);
    		ScmResult scmResult = new ScmResult(res.getCommandString(), "", "Exit Code: " + exitCode, success);
    		result = new BranchScmResult(new ArrayList<ScmFile>(), scmResult); 
    	}
    	catch(APIException aex)
    	{
    		ExceptionHandler eh = new ExceptionHandler(aex);
    		getLogger().error("MKS API Exception: " + eh.getMessage());
    		getLogger().info(eh.getCommand() + " exited with return code " + eh.getExitCode());
    		result = new BranchScmResult(eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false);
    	}
    	catch(Exception e)
    	{
    		getLogger().error("Failed to checkpoint project! " + e.getMessage());
    		result = new BranchScmResult("si createdevpath", e.getMessage(), "", false);    		
    	}
    	
		return result;
	}

}
