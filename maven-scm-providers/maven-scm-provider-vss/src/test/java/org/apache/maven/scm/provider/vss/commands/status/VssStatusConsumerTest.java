package org.apache.maven.scm.provider.vss.commands.status;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.manager.plexus.PlexusLogger;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.IOUtil;

/**
 * @author <a href="mailto:matpimenta@gmail.com">Mateus Pimenta</a>
 * 
 */
public class VssStatusConsumerTest
    extends ScmTestCase
{

    private ScmManager scmManager;

    private org.codehaus.plexus.logging.Logger logger;

    public void setUp()
        throws Exception
    {
        super.setUp();
        scmManager = getScmManager();
        logger = getContainer().getLogger();
    }

    public void testConsumeLine()
        throws ScmRepositoryException, NoSuchScmProviderException, IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new InputStreamReader( this.getResourceAsStream( "/test.txt" ), "UTF-8" ) );
            ScmRepository repository = scmManager.makeScmRepository(
                "scm:vss|username|password@C:/Program File/Visual Source Safe|D:/myProject" );
            
            ScmFileSet fileSet = new ScmFileSet( getTestFile( "target" ) );

            VssStatusConsumer consumer = 
                new VssStatusConsumer( (VssScmProviderRepository) repository.getProviderRepository(),
                                       new PlexusLogger( logger ), fileSet );


            for ( String line = reader.readLine(); line != null; line = reader.readLine() )
            {
                consumer.consumeLine( line );
            }

            reader.close();
            reader = null;
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

}
