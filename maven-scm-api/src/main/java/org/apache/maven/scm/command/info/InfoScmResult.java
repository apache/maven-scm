package org.apache.maven.scm.command.info;

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

import org.apache.maven.scm.ScmResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class InfoScmResult
    extends ScmResult
{

    private static final long serialVersionUID = 955993340040530451L;
    private List<InfoItem> infoItems;

    public InfoScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );

        infoItems = new ArrayList<InfoItem>( 0 );
    }

    public InfoScmResult( String commandLine, List<InfoItem> files )
    {
        super( commandLine, null, null, true );

        this.infoItems = files;
    }

    public InfoScmResult( List<InfoItem> infoItems, ScmResult result )
    {
        super( result );

        this.infoItems = infoItems;
    }

    public InfoScmResult( ScmResult result )
    {
        super( result );
    }
    
    public List<InfoItem> getInfoItems()
    {
        return infoItems;
    }
}
