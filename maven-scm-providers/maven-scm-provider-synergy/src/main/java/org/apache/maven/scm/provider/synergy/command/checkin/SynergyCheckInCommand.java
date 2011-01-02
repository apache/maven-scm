package org.apache.maven.scm.provider.synergy.command.checkin;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class SynergyCheckInCommand
    extends AbstractCheckInCommand
    implements SynergyCommand
{

    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                      String message, ScmVersion version )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing update command..." );
        }

        SynergyScmProviderRepository repo = (SynergyScmProviderRepository) repository;

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "basedir: " + fileSet.getBasedir() );
        }

        String ccmAddr = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        try
        {
            SynergyUtil.checkinDefaultTask( getLogger(), message, ccmAddr );
        }
        finally
        {
            SynergyUtil.stop( getLogger(), ccmAddr );
        }
        List<ScmFile> scmFiles = new ArrayList<ScmFile>(fileSet.getFileList().size());
        for (File f : fileSet.getFileList()) 
        {
            scmFiles.add( new ScmFile( f.getPath(), ScmFileStatus.CHECKED_IN ) );
        }
        return new CheckInScmResult( "ccm checkin", scmFiles );
    }

}
