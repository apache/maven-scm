package org.apache.maven.scm.provider.jazz.repository;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    /**
     * The URI of the repository server.
     * Of the form <protocol>://<server>:<port>/<path>
     * For example:
     * https://rtc:9444/jazz
     */
    private String fRepositoryURI;

    /**
     * The name of the remote repository workspace (as set from the URL).
     */
    private String fRepositoryWorkspace;

    // Fields that are *only* set when parsing the output of the "scm status" command.
    // So, in essence, they are the 'real' values, as returned from the system.
    // What is in the URL, via the pom, may not be correct.
    // If the "scm status" command has not been called, then these values will be zero/null.

    /**
     * The alias of the repository workspace, as returned from the "scm status" command.
     */
    private int fWorkspaceAlias;

    /**
     * The name of the repository workspace, as returned from the "scm status" command.
     */
    private String fWorkspace;

    // Note: If there are no flow targets defined, then the repository workspace points to itself,
    //       so fWorkspaceAlias = fStreamAlias and fWorkspace = fStream

    // TODO: Change to enable multiple flow targets, via a List (?).

    // NOTE: We are not parsing the Component Alias nor the Baseline Alias, as they are not currently needed.

    /**
     * The alias of the flow target, as returned from the "scm status" command.
     */
    private int fFlowTargetAlias;

    /**
     * The name of the flow target, as returned from the "scm status" command.
     */
    private String fFlowTarget;     // Can also be a repository workspace, possibly the same as fWorkspace

    /**
     * The name of the component, as returned from the "scm status" command.
     */
    private String fComponent;

    /**
     * The name of the baseline, as returned from the "scm status" command.
     */
    private String fBaseline;

    /**
     * The outgoing aliases of the change sets, as returned from the "scm status" command.
     */
    private List<Integer> fOutgoingChangeSetAliases = new ArrayList<Integer>();

    /**
     * The incoming aliases of the change sets, as returned from the "scm status" command.
     */
    private List<Integer> fIncomingChangeSetAliases = new ArrayList<Integer>();

    // TODO In the future we might expand the details of this repository.
    // For example we might extend the scm url to include a stream (as well as the repository workspace)
    // This stream could represent the desired flow target of the repository workspace.
    // We would also need to cater for multiple streams/flow targets.
    public JazzScmProviderRepository( String repositoryURI, String userName, String password, String hostName, int port,
                                      String repositoryWorkspace )
    {
        this.fRepositoryURI = repositoryURI;
        setUser( userName );
        setPassword( password );
        setHost( hostName );
        setPort( port );
        this.fRepositoryWorkspace = repositoryWorkspace;
    }

    /**
     * Return <code>true</code> if we have a valid flow target and pushChanges is <code>true</code>.
     */
    public boolean isPushChangesAndHaveFlowTargets()
    {
        if ( !isPushChanges() )
        {
            return isPushChanges();
        }

        return isHaveFlowTargets();
    }

    /**
     * Return <code>true</code> if we have a valid flow target.
     * A valid flow target is a destination other than ourselves.
     * To determine this, we need to parse the output of the 'scm status' command.
     */
    public boolean isHaveFlowTargets()
    {
        // We have a workspace and a flow target and they are not the same nor are their aliases.
        return StringUtils.isNotEmpty( getWorkspace() ) && StringUtils.isNotEmpty( getFlowTarget() )
            && !getWorkspace().equals( getFlowTarget() ) && getWorkspaceAlias() != getFlowTargetAlias();
    }

    /**
     * Return the URI of the repository server, as parsed from the URL.
     *
     * @return The URI of the repository server, as parsed from the URL.
     */
    public String getRepositoryURI()
    {
        return fRepositoryURI;
    }

    /**
     * Return the name of the remote repository workspace, as parsed from the URL.
     *
     * @return The name of the remote repository workspace, as parsed from the URL.
     */
    public String getRepositoryWorkspace()
    {
        return fRepositoryWorkspace;
    }

    // NOTE: The following getter/setters are only used when the "scm status" command
    //       has been called. Those commands that need it, need to call the status()
    //       command first. Otherwise these values will be zero or null.

    /**
     * @return The alias of the repository workspace, as returned from the "scm status" command.
     */
    public int getWorkspaceAlias()
    {
        return fWorkspaceAlias;
    }

    /**
     * @param workspaceAlias the workspaceAlias to set
     */
    public void setWorkspaceAlias( int workspaceAlias )
    {
        this.fWorkspaceAlias = workspaceAlias;
    }

    /**
     * @return The name of the repository workspace, as returned from the "scm status" command.
     */
    public String getWorkspace()
    {
        return fWorkspace;
    }

    /**
     * @param fWorkspace The fWorkspace to set.
     */
    public void setWorkspace( String fWorkspace )
    {
        this.fWorkspace = fWorkspace;
    }

    /**
     * @return The alias of the flow target, as returned from the "scm status" command.
     */
    public int getFlowTargetAlias()
    {
        return fFlowTargetAlias;
    }

    /**
     * @param flowTargetAlias the flowTargetAlias to set
     */
    public void setFlowTargetAlias( int flowTargetAlias )
    {
        this.fFlowTargetAlias = flowTargetAlias;
    }

    /**
     * @return The name of the flow target, as returned from the "scm status" command.
     */
    public String getFlowTarget()
    {
        return fFlowTarget;
    }

    /**
     * @param flowTarget The flowTarget to set.
     */
    public void setFlowTarget( String flowTarget )
    {
        this.fFlowTarget = flowTarget;
    }

    /**
     * @return The name of the component, as returned from the "scm status" command.
     */
    public String getComponent()
    {
        return fComponent;
    }

    /**
     * @param component The component to set.
     */
    public void setComponent( String component )
    {
        this.fComponent = component;
    }

    /**
     * @return The name of the baseline, as returned from the "scm status" command.
     */
    public String getBaseline()
    {
        return fBaseline;
    }

    /**
     * @param baseline The baseline to set.
     */
    public void setBaseline( String baseline )
    {
        this.fBaseline = baseline;
    }

    /**
     * @return The List<Integer> of aliases of the outgoing changesets, as returned from the "scm status" command.
     */
    public List<Integer> getOutgoingChangeSetAliases()
    {
        return fOutgoingChangeSetAliases;
    }

    /**
     * @param outgoingChangeSetAliases The List of Integers of outgoing change set aliases to set
     */
    public void setOutgoingChangeSetAliases( List<Integer> outgoingChangeSetAliases )
    {
        this.fOutgoingChangeSetAliases = outgoingChangeSetAliases;
    }
    
    /**
     * @return The List<Integer> of aliases of the incoming changesets, as returned from the "scm status" command.
     */
    public List<Integer> getIncomingChangeSetAliases()
    {
        return fIncomingChangeSetAliases;
    }

    /**
     * @param incomingChangeSetAliases The List of Integers of incoming change set aliases to set
     */
    public void setIncomingChangeSetAliases( List<Integer> incomingChangeSetAliases )
    {
        this.fIncomingChangeSetAliases = incomingChangeSetAliases;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getRepositoryURI() + ":" + getRepositoryWorkspace();
    }

}
