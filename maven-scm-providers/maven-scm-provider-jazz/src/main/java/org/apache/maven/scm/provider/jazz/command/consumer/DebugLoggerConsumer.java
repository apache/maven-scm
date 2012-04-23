package org.apache.maven.scm.provider.jazz.command.consumer;

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

import org.apache.maven.scm.log.ScmLogger;

/**
 * This class exists only to consume output that is normally not needed,
 * though it may be of interest when in debug mode.
 * 
 * For example, "scm lock acquire" produces:
 * "Locks successfully acquired."
 * and "scm lock release" produces:
 * "Locks successfully released."
 * 
 * So, basically nothing of real interest to parse and pass back.
 * 
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class DebugLoggerConsumer
    extends AbstractRepositoryConsumer
{
    private StringBuilder content = new StringBuilder();

    private String ls = System.getProperty( "line.separator" );

    public DebugLoggerConsumer( ScmLogger logger )
    {
        // Only ever used for debugging, so do not need the repository.
        super( null, logger );
    }

    @Override
    public void consumeLine( String line )
    {
        super.consumeLine( line );
        content.append( line ).append( ls );
    }

    public String getOutput()
    {
        return content.toString();
    }
}
