package org.apache.maven.scm.command.update;

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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UpdateScmResult
    extends ScmResult
{
    private List updatedFiles;

    private List changes;

    public UpdateScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public UpdateScmResult( String commandLine, List updatedFiles )
    {
        super( commandLine, null, null, true );

        this.updatedFiles = updatedFiles;
    }

    public UpdateScmResult( List updatedFiles, List changes, ScmResult result )
    {
        super( result );

        this.updatedFiles = updatedFiles;

        this.changes = changes;
    }

    public List getUpdatedFiles()
    {
        return updatedFiles;
    }

    public List getChanges()
    {
        if ( changes == null )
        {
            return new ArrayList();
        }
        return changes;
    }

    public void setChanges( List changes )
    {
        this.changes = changes;
    }
}
