package org.apache.maven.scm.command.blame;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRequest;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * @author Olivier Lamy
 * @since 1.8
 */
public class BlameScmRequest
    extends ScmRequest
{

    /**
     * -w option for git
     */
    private boolean ignoreWhitespace;


    public BlameScmRequest( ScmRepository scmRepository, ScmFileSet scmFileSet )
    {
        super( scmRepository, scmFileSet );
    }

    public void setFilename( String filename )
        throws ScmException
    {
        this.getCommandParameters().setString( CommandParameter.FILE, filename );
    }

    public String getFilename()
        throws ScmException
    {
        return this.getCommandParameters().getString( CommandParameter.FILE );
    }

    public boolean isIgnoreWhitespace()
    {
        return ignoreWhitespace;
    }

    public void setIgnoreWhitespace( boolean ignoreWhitespace )
    {
        this.ignoreWhitespace = ignoreWhitespace;
    }
}
