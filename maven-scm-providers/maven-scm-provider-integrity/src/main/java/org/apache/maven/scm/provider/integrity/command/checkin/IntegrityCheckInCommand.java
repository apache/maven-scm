	package org.apache.maven.scm.provider.integrity.command.checkin;

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

import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

/**
 * MKS Integrity implementation for Maven's AbstractCheckInCommand
 * <br>The check-in command will also drop any files that are missing from the working directory 
 * @version $Id: IntegrityCheckInCommand.java 1.3 2011/08/22 13:06:20EDT Cletus D'Souza (dsouza) Exp  $
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 */
public class IntegrityCheckInCommand extends AbstractCheckInCommand 
{
	/**
	 * {@inheritDoc}
	 */	
	@Override
	public CheckInScmResult executeCheckInCommand(ScmProviderRepository repository, ScmFileSet fileSet, 
												String message, ScmVersion scmVersion) throws ScmException 
	{
		getLogger().info("Attempting to check-in updates from sandbox " + fileSet.getBasedir().getAbsolutePath());
		IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
		Sandbox siSandbox = iRepo.getSandbox();
		List<ScmFile> changedFiles = siSandbox.checkInUpdates(message);
		if( siSandbox.getOverallCheckInSuccess() )
		{
			return new CheckInScmResult("si ci/drop", changedFiles);
		}
		else
		{
			return new CheckInScmResult(changedFiles, new ScmResult("si ci/drop", "There was a problem updating the repository", "", false)); 
		}		
	}
}
