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
package org.apache.maven.scm;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class ScmResult implements Serializable {
    private static final long serialVersionUID = 7037918334820621525L;

    private final boolean success;

    private final String providerMessage;

    private final String commandOutput;

    private final String commandLine;

    public static final String PASSWORD_PLACE_HOLDER = "********";

    // works for SVN and git
    private Pattern patternForUserColonPasswordAtHost = Pattern.compile("^.*:(.*)@.*$", Pattern.DOTALL);

    /**
     * Copy constructor.
     * <p>
     * Typically used from derived classes when wrapping a ScmResult
     * into a specific type eg. AddScmResult
     *
     * @param scmResult not null
     */
    public ScmResult(ScmResult scmResult) {
        this.commandLine = scmResult.commandLine;

        this.providerMessage = scmResult.providerMessage;

        this.commandOutput = masked(scmResult.commandOutput);

        this.success = scmResult.success;
    }

    /**
     * ScmResult contructor.
     *
     * @param commandLine     The provider specific command line used
     * @param providerMessage The provider message
     * @param commandOutput   The command output of the scm tool
     * @param success         True if the command is in success
     */
    public ScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
        this.commandLine = commandLine;

        this.providerMessage = providerMessage;

        this.commandOutput = masked(commandOutput);

        this.success = success;
    }

    /**
     * @return True if the command was in success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return A message from the provider. On success this would typically be null or
     *         an empty string. On failure it would be the error message from the provider
     */
    public String getProviderMessage() {
        return providerMessage;
    }

    /**
     * @return Output from Std.Out from the provider during execution
     *         of the command that resulted in this
     */
    public String getCommandOutput() {
        return commandOutput;
    }

    /**
     * @return The actual provider specific command that resulted in this
     */
    public String getCommandLine() {
        return commandLine;
    }

    private String masked(String commandOutput) {
        if (null != commandOutput) {
            final Matcher passwordMatcher = patternForUserColonPasswordAtHost.matcher(commandOutput);
            if (passwordMatcher.find()) {
                // clear password
                final String clearPassword = passwordMatcher.group(1);
                // to be replaced in output by stars
                commandOutput = commandOutput.replace(clearPassword, PASSWORD_PLACE_HOLDER);
            }
        }
        return commandOutput;
    }
}
