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

    /**
     * @deprecated
     */
    public static class Failure
        extends ScmResult
    {
        public Failure()
        {
            super( null, null, false );
        }
    }

    /**
     * @deprecated
     */
    public ScmResult()
    {
        this.success = true;
    }

    /**
     * @deprecated
     */
    public ScmResult( String providerMessage, String commandOutput )
    {
        this.providerMessage = providerMessage;

        this.commandOutput = commandOutput;

        success = false;
    }

    /**
     * @deprecated
     */
    public ScmResult( String providerMessage, boolean success )
    {
        this.providerMessage = providerMessage;

        this.success = success;
    }

    public ScmResult( String providerMessage, String commandOutput, boolean success )
    {
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

    /**
     * @deprecated
     * @return
     */
    public String getMessage()
    {
        return providerMessage;
    }

    /**
     * @deprecated
     * @return
     */
    public String getLongMessage()
    {
        return commandOutput;
    }
}
