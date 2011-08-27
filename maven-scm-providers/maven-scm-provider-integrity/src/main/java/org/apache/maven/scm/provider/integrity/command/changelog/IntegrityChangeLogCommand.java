package org.apache.maven.scm.provider.integrity.command.changelog;

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

import java.util.Date;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import com.mks.api.response.APIException;

/**
 * MKS Integrity implementation for Maven's AbstractChangeLogCommand
 * <br>Currently there is a limitation in the 'si rlog' command where changes
 * can't be limited to a normal/variant/build configuration.  In other
 * words all revisions modified within the date range will be picked up
 * for the Change Log report.  By default the Change Log is grouped by
 * Change Package ID.  However, if no Change Package is found or Change
 * Packages are not in use, then all the changes are grouped in one big
 * Change Log Set
 * @version $Id: IntegrityChangeLogCommand.java 1.3 2011/08/22 13:06:19EDT Cletus D'Souza (dsouza) Exp  $
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 */
public class IntegrityChangeLogCommand extends AbstractChangeLogCommand 
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChangeLogScmResult executeChangeLogCommand(ScmProviderRepository repository, 
											ScmFileSet fileSet, Date startDate, Date endDate, 
											ScmBranch branch, String datePattern) throws ScmException 
	{
		// First lets validate the date range provided
		if( null == startDate || null == endDate )
		{
			throw new ScmException("Both 'startDate' and 'endDate' must be specified!");
		}
		if( startDate.after(endDate) )
		{
			throw new ScmException("'stateDate' is not allowed to occur after 'endDate'!");
		}
		getLogger().info("Attempting to obtain change log for date range: '" + Sandbox.RLOG_DATEFORMAT.format(startDate) 
						+ "' to '" + Sandbox.RLOG_DATEFORMAT.format(endDate) + "'");
		ChangeLogScmResult result;
		IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository; 
    	try
    	{
    		result = new ChangeLogScmResult(iRepo.getSandbox().getChangeLog(startDate, endDate), new ScmResult("si rlog", "", "", true)); 
    	}
    	catch(APIException aex)
    	{
    		ExceptionHandler eh = new ExceptionHandler(aex);
    		getLogger().error("MKS API Exception: " + eh.getMessage());
    		getLogger().info(eh.getCommand() + " exited with return code " + eh.getExitCode());
    		result = new ChangeLogScmResult(eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false);
    	}
    	
		return result;
	}

}
