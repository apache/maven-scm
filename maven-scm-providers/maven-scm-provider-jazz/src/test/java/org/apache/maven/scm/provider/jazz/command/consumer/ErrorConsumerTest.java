package org.apache.maven.scm.provider.jazz.command.consumer;

import junit.framework.TestCase;

import org.apache.maven.scm.log.DefaultLog;

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

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class ErrorConsumerTest
     extends TestCase
{
    private ErrorConsumer errConsumer;
    
    public void setUp()
        throws Exception
    {
        errConsumer = new ErrorConsumer( new DefaultLog() );
    }

    public void testErrorConsumer()
    {
        assertFalse( errConsumer.hasBeenFed() );
        errConsumer.consumeLine( "Some line to consume" );
        assertTrue( errConsumer.hasBeenFed() );
    }
}
