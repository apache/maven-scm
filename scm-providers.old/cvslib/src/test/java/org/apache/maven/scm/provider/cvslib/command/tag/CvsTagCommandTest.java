package org.apache.maven.scm.provider.cvslib.command.tag;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @version $Id$
 */
public class CvsTagCommandTest
    extends AbstractCvsScmTest
{
    public void testGetCommandWithTag() throws Exception
    {
        CvsTagCommand command = new CvsTagCommand();

        CvsRepository repo = new CvsRepository();

        repo.setDelimiter( ":" );
        repo.setConnection( "pserver:anonymous@cvs.codehaus.org:/scm/cvspublic:test-repo" );
        repo.setPassword( "anonymous@cvs.codehaus.org" );

        command.setRepository( repo );
        command.setTagName( "my_tag" );
        Commandline cl = command.getCommandLine();
        assertEquals( "cvs -d :pserver:anonymous@cvs.codehaus.org:/scm/cvspublic -q tag -c my_tag", cl.toString() );
    }

    public void testGetCommandWithoutTag()
        throws Exception
    {
        CvsTagCommand command = new CvsTagCommand();
        CvsRepository repo = new CvsRepository();
        repo.setDelimiter( ":" );
        repo.setConnection( "pserver:anonymous@cvs.codehaus.org:/scm/cvspublic:test-repo" );
        repo.setPassword( "anonymous@cvs.codehaus.org" );

        command.setRepository( repo );
    }

    public void testGetDisplayNameName()
        throws Exception
    {
        CvsTagCommand command = new CvsTagCommand();

        assertEquals( "Tag", command.getDisplayName() );
    }

    public void testGetName()
        throws Exception
    {
        CvsTagCommand command = new CvsTagCommand();

        assertEquals( "tag", command.getName() );
    }
}