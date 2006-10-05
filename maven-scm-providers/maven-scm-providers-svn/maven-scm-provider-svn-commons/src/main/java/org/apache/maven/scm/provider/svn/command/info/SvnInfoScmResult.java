package org.apache.maven.scm.provider.svn.command.info;

import org.apache.maven.scm.ScmResult;

import java.util.ArrayList;
import java.util.List;

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

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 * $Id$
 */
public class SvnInfoScmResult
    extends ScmResult
{
    private List infoItems;

    public SvnInfoScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );

        infoItems = new ArrayList( 0 );
    }

    public SvnInfoScmResult( String commandLine, List files )
    {
        super( commandLine, null, null, true );

        this.infoItems = files;
    }

    public SvnInfoScmResult( List files, ScmResult result )
    {
        super( result );

        this.infoItems = files;
    }

    public List getInfoItems()
    {
        return infoItems;
    }
}
