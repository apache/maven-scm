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

/**
 * @author Olivier Lamy
 * @since 1.2
 */
public class ScmTagParameters implements Serializable {
    private static final long serialVersionUID = 7241536408630606807L;

    private String message;

    private boolean remoteTagging = false;

    private boolean pinExternals = false;

    private CommandParameters.SignOption signOption;

    private String scmRevision;

    private boolean lightweight = false;

    public ScmTagParameters() {
        this.remoteTagging = false;
        this.pinExternals = false;
        this.signOption = CommandParameters.SignOption.DEFAULT;
    }

    public ScmTagParameters(String message) {
        this();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRemoteTagging() {
        return remoteTagging;
    }

    public void setRemoteTagging(boolean remoteTagging) {
        this.remoteTagging = remoteTagging;
    }

    public boolean isPinExternals() {
        return pinExternals;
    }

    public void setPinExternals(boolean pinExternals) {
        this.pinExternals = pinExternals;
    }

    /**
     * @return true if the tag operation should be signed, false otherwise
     * @deprecated use {@link #getSignOption()} instead
     */
    @Deprecated
    public boolean isSign() {
        return signOption == CommandParameters.SignOption.FORCE_SIGN;
    }

    /**
     * Set the signing option for the tag operation.
     *
     * @param sign
     * @deprecated use {@link #setSignOption(org.apache.maven.scm.CommandParameters.SignOption)} instead
     */
    @Deprecated
    public void setSign(boolean sign) {
        signOption = sign ? CommandParameters.SignOption.FORCE_SIGN : CommandParameters.SignOption.DEFAULT;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    /**
     * Get the signing option for the tag operation.
     *
     * @return the signing option
     * @since 2.2.1
     */
    public CommandParameters.SignOption getSignOption() {
        return signOption;
    }

    /**
     * Set the signing option for the tag operation.
     *
     * @param signOption
     * @since 2.2.1
     */
    public void setSignOption(CommandParameters.SignOption signOption) {
        this.signOption = signOption;
    }

    /**
     * Tells whether a lightweight tag (a plain reference without tag object, message or signature) is requested.
     * Only applicable to SCMs that distinguish lightweight and annotated tags (Git).
     *
     * @return {@code true} for a lightweight tag, {@code false} (the default) for an annotated one
     * @since 2.3.0
     */
    public boolean isLightweight() {
        return lightweight;
    }

    /**
     * Sets whether a lightweight tag should be created instead of an annotated one. A lightweight tag carries
     * neither a message nor a signature, so both are ignored. A lightweight tag is never signed, even if the Git
     * configuration asks for signed tags: the gitexe provider rejects {@code FORCE_SIGN} and the jgit provider
     * ignores the sign option.
     *
     * @param lightweight {@code true} for a lightweight tag
     * @since 2.3.0
     */
    public void setLightweight(boolean lightweight) {
        this.lightweight = lightweight;
    }

    @Override
    public String toString() {
        return "ScmTagParameters{" + "message='"
                + message + '\'' + ", remoteTagging="
                + remoteTagging + ", pinExternals="
                + pinExternals + ", signOption="
                + signOption + ", lightweight="
                + lightweight + ", scmRevision='"
                + scmRevision + '\'' + '}';
    }
}
