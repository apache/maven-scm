package org.apache.maven.scm.provider.cvslib;

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
import java.util.Calendar;
import java.util.Date;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCvsScmTest
    extends ScmTestCase
{
    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.mkdir( getWorkingDirectory().getAbsolutePath() );
    }

    // ----------------------------------------------------------------------
    // Implementation of the abstract ScmTestCase methods
    // ----------------------------------------------------------------------

    protected ScmRepository getScmUrl()
    	throws Exception
    {
        return makeScmRepository( "scm:cvs|local|" + getRepository() + "|" + getModule() );
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    protected Date getDate( int year, int month, int day )
    {
        Calendar cal = Calendar.getInstance();

        cal.set( year, month, day );

        return cal.getTime();
    }

    protected void executeCVS( File workingDirectory, String arguments )
    	throws Exception
    {
        execute( workingDirectory, "cvs", arguments );
    }
}
