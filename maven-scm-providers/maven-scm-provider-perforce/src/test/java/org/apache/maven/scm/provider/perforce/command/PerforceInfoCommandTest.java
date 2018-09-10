package org.apache.maven.scm.provider.perforce.command;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.tag.PerforceTagCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PerforceInfoCommandTest
    extends ScmTestCase
{
    public void testPerforceInfo()
        throws Exception
    {
        ScmRepository repo = makeScmRepository( "scm:perforce://depot/projects/pathname" );

        PerforceScmProviderRepository p4Repo = (PerforceScmProviderRepository) repo.getProviderRepository();

       if ( !ScmTestCase.isSystemCmd( "p4" ) )
       {
           ScmTestCase.printSystemCmdUnavail( "p4", getName() );
           return;
       }

        PerforceScmProvider prov = new PerforceScmProvider();
        PerforceTagCommand cmd = new PerforceTagCommand();
        cmd.setLogger( prov.getLogger() );

        if ( PerforceScmProvider.isLive() )
        {
            assertNotNull( PerforceInfoCommand.getInfo( prov.getLogger(), p4Repo ).getEntry( "User name" ) );
            assertNotNull( PerforceInfoCommand.getInfo( prov.getLogger(), p4Repo ).getEntry( "Client root" ) );
            assertNotNull( PerforceInfoCommand.getInfo( prov.getLogger(), p4Repo ).getEntry( "Client name" ) );
            assertNotNull( PerforceInfoCommand.getInfo( prov.getLogger(), p4Repo ).getEntry( "Client host" ) );
            assertNull( PerforceInfoCommand.getInfo( prov.getLogger(), p4Repo ).getEntry( "foobar" ) );
        }
    }
}
