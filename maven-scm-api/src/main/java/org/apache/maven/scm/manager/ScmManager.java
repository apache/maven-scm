package org.apache.maven.scm.manager;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.Date;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ScmManager
{
    String ROLE = ScmManager.class.getName();

    // ----------------------------------------------------------------------
    // Repository
    // ----------------------------------------------------------------------

    ScmRepository makeScmRepository( String scmUrl )
    	throws ScmRepositoryException, NoSuchScmProviderException;

    // ----------------------------------------------------------------------
    // Scm Commands
    // ----------------------------------------------------------------------

    CheckOutScmResult checkOut( ScmRepository repository, File workingDirectory, String tag )
    	throws ScmException;

    CheckInScmResult checkIn( ScmRepository repository, File workingDirectory, String tag, String message )
    	throws ScmException;

    UpdateScmResult update( ScmRepository repository, File workingDirectory, String tag )
        throws ScmException;

    ChangeLogScmResult changeLog( ScmRepository repository, File workingDirectory, Date startDate, Date endDate, int numDays, String branch )
        throws ScmException;
}
