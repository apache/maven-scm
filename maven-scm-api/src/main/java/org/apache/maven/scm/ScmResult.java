package org.apache.maven.scm;

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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScmResult
{
    private boolean success;

    private String providerMessage;

    private String commandOutput;

    public static class Failure
        extends ScmResult
    {
        public Failure()
        {
            super( null, false );
        }
    }

    public ScmResult()
    {
        this.success = true;
    }

    public ScmResult( String providerMessage, boolean success )
    {
        this.providerMessage = providerMessage;

        this.success = success;
    }

    public ScmResult( String providerMessage, String commandOutput )
    {
        this.providerMessage = providerMessage;

        this.commandOutput = commandOutput;

        success = false;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getProviderMessage()
    {
        return providerMessage;
    }

    public String getCommandOutput()
    {
        return commandOutput;
    }

    public String getMessage()
    {
        return providerMessage;
    }

    public String getLongMessage()
    {
        return commandOutput;
    }
}
