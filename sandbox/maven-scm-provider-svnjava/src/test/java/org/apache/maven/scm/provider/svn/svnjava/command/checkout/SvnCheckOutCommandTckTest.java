package org.apache.maven.scm.provider.svn.svnjava.command.checkout;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.svn.svnjava.command.SvnJavaTckTestCommand;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;

/**
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 */
public class SvnCheckOutCommandTckTest
    extends CheckOutCommandTckTest
{
    private SvnJavaTckTestCommand cmd = new SvnJavaTckTestCommand( this.getClass() );

    public String getScmUrl()
        throws Exception
    {
        return cmd.getScmUrl();
    }

    public void initRepo()
        throws Exception
    {
        cmd.initRepo();
    }
}
