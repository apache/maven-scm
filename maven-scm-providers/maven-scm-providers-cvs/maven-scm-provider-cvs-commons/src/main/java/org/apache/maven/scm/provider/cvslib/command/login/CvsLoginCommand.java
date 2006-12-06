package org.apache.maven.scm.provider.cvslib.command.login;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.login.AbstractLoginCommand;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;

import java.io.IOException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsLoginCommand
    extends AbstractLoginCommand
{
    /**
     * @see org.apache.maven.scm.command.login.AbstractLoginCommand#executeLoginCommand(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public LoginScmResult executeLoginCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException
    {
        CvsScmProviderRepository repo = (CvsScmProviderRepository) repository;

        if ( !"pserver".equals( repo.getTransport() ) )
        {
            return new LoginScmResult( null, "The cvs login ignored for " + repo.getTransport() + ".", "", true );
        }
        else if ( CvsCommandUtils.isCvsNT() )
        {
            //We don't continue becauseCVSNT doesn't use .cvspass
            return new LoginScmResult( null, "The cvs login ignored for CVSNT.", "", true );
        }

        CvsPass passGenerator = new CvsPass( getLogger() );

        passGenerator.setCvsroot( repo.getCvsRootForCvsPass() );

        passGenerator.setPassword( repo.getPassword() );
        try
        {
            passGenerator.execute();
        }
        catch ( IOException e )
        {
            throw new ScmException( "Error while executing cvs login command.", e );
        }

        return new LoginScmResult( null, "The cvs command succeed.", "", true );
    }
}
