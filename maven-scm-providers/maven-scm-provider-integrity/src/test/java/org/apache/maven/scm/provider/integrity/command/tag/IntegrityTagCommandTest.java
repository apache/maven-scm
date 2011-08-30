package org.apache.maven.scm.provider.integrity.command.tag;

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
import org.apache.maven.scm.provider.integrity.command.IntegrityCommandTest;

/**
 * IntegrityTagCommandTest unit test class
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityTagCommandTest.java 1.1 2011/08/29 00:30:06EDT Cletus D'Souza (dsouza) Exp  $
 */
public class IntegrityTagCommandTest
    extends IntegrityCommandTest
{
    /**
     * Sets up this unit test for execution
     */
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    /**
     * Executes the IntegrityTagCommand and validates the result
     *
     * @throws Exception
     */
    public void testTagCommandTest()
        throws Exception
    {
        // Set the message parameter which is optional for the tag command
        parameters.setString( CommandParameter.MESSAGE, "Checkpointing project whith changes to file " + fileName );

        // Now we can then execute the tag command
        IntegrityTagCommand command = new IntegrityTagCommand();
        command.setLogger( logger );
        assertResultIsSuccess( command.execute( iRepo, fileSet, parameters ) );
    }
}

