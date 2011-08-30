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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.provider.integrity.command.IntegrityCommandTest;
import org.apache.maven.scm.provider.integrity.command.checkin.IntegrityCheckInCommand;
import org.apache.maven.scm.provider.integrity.command.edit.IntegrityEditCommand;

/**
 * IntegrityCheckInCommandTest unit test class
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityCheckInCommandTest.java 1.1 2011/08/29 00:29:54EDT Cletus D'Souza (dsouza) Exp  $
 */
public class IntegrityCheckInCommandTest extends IntegrityCommandTest 
{
	/**
	 * Sets up this unit test for execution
	 */
    public void setUp() throws Exception
    {
    	super.setUp();
    }

    /**
     * Executes the IntegrityCheckInCommand and validates the result
     * @throws Exception
     */
    public void testCheckInCommandTest() throws Exception
    {
    	// First we need to make the workspace writable
    	IntegrityEditCommand edit = new IntegrityEditCommand();
    	edit.setLogger(logger);
    	assertResultIsSuccess(edit.execute(iRepo, fileSet, parameters));
    	// Now lets add something to the file we added in the add test
    	String nl = System.getProperty("line.separator");
    	BufferedWriter bw = new BufferedWriter(new FileWriter(fileSet.getBasedir() + File.separator + fileName, true));
    	bw.write(nl + nl + "A new change appended to file by the check-in command test" + nl);
    	bw.flush();
    	bw.close();
    	// Set the message parameter required for the check-in command to work
    	parameters.setString(CommandParameter.MESSAGE, "Attempting change to an existing file " + fileName);
    	// Now execute the check-in command and validate the results
    	IntegrityCheckInCommand checkin = new IntegrityCheckInCommand();
    	checkin.setLogger(logger);
    	assertResultIsSuccess(checkin.execute(iRepo, fileSet, parameters)); 
    }
}

