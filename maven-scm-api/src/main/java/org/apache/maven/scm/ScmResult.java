package org.apache.maven.scm;

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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScmResult
{
    private boolean success;

    private String providerMessage;

    private String commandOutput;

    private String commandLine;

    /**
     * @deprecated
     */
    public static class Failure
        extends ScmResult
    {
        public Failure()
        {
            super( null, null, null, false );
        }
    }

    public ScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        this.commandLine = commandLine;

        this.providerMessage = providerMessage;

        this.commandOutput = commandOutput;

        this.success = success;
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

    public String getCommandLine()
    {
        return commandLine;
    }

    /**
     * @return
     * @deprecated
     */
    public String getMessage()
    {
        return providerMessage;
    }

    /**
     * @return
     * @deprecated
     */
    public String getLongMessage()
    {
        return commandOutput;
    }
}
