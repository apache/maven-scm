package org.apache.maven.scm.provider.synergy.util;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.ScmLogger;

/**
 * In some Synergy versions (ie. 6.5) closing a session results in de-selecting
 * the current (default) task. Therefore, the maven release-plugin fails, as the
 * Synergy commands, as implemented in the Synergy-SCM-Provider, always close
 * their session after being executed.<br>
 * This manager circumvents this problem by storing the last created task which
 * causes all check outs to be associated with it. Then, when this task gets
 * checked in, all associated files get checked in as well.
 * 
 * @author <a href="jan.malcomess@steria-mummert.de">Jan Malcomess</a>
 * @since 1.5
 */
public class SynergyTaskManager
{
    /**
     * No Synergy-Task was created yet.
     */
    private final static short TASK_STATE_NONE = 0;

    /**
     * The current Synergy-Task is created but not yet completed.
     */
    private final static short TASK_STATE_CREATED = 1;

    /**
     * The current Synergy-Task is completed.
     */
    private final static short TASK_STATE_COMPLETED = 2;

    /**
     * singleton instance.
     */
    private final static SynergyTaskManager INSTANCE = new SynergyTaskManager();

    /**
     * The number of the current Synergy-Task.
     */
    private int currentTaskNumber;

    /**
     * The state of the current Synergy-Task.
     */
    private short currentTaskState = TASK_STATE_NONE;

    /**
     * @return singleton instance.
     */
    public static SynergyTaskManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * If necessary create a new task. Otherwise return the current task.
     * 
     * @param logger a logger.
     * @param synopsis short description of task.
     * @param release release.
     * @param defaultTask should this task become the default task?
     * @param ccmAddr current Synergy session ID. Used to run in multi-session.
     * @return Task number
     * @throws ScmException
     */
    public int createTask( ScmLogger logger, String synopsis, String release, boolean defaultTask, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering createTask method of SynergyTaskManager" );
        }
        switch ( currentTaskState )
        {
            case TASK_STATE_CREATED:
                if ( defaultTask )
                {
                    // make sure the current task is the default task
                    if ( SynergyUtil.getDefaultTask( logger, ccmAddr ) != currentTaskNumber )
                    {
                        SynergyUtil.setDefaultTask( logger, currentTaskNumber, ccmAddr );
                    }
                }
                break;
            case TASK_STATE_NONE: // fall through
            case TASK_STATE_COMPLETED:
                currentTaskNumber = SynergyUtil.createTask( logger, synopsis, release, defaultTask, ccmAddr );
                currentTaskState = TASK_STATE_CREATED;
                break;
            default:
                throw new IllegalStateException( "Programming error: SynergyTaskManager is in unkown state." );
        }
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "createTask returns " + currentTaskNumber );
        }
        return currentTaskNumber;
    }

    /**
     * Check in (that is: complete) the default task. This is either the current task managed by
     * <code>SynergyTaskManager</code> or, if none is managed, the default task.<br>
     * In case no task has yet been created by <code>SynergyTaskManager</code> AND no default task is set, then this is
     * an error.<br>
     * However, if the task that was created by <code>SynergyTaskManager</code> has already been checked in AND no
     * default task is set, then it is assumed that all files that were checked out are already checked in because
     * checking in a task checks in all files associated with it.
     * 
     * @param logger a logger.
     * @param comment a comment for checkin.
     * @param ccmAddr current Synergy session ID. Used to run in multi-session.
     * @throws ScmException
     */
    public void checkinDefaultTask( ScmLogger logger, String comment, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering checkinDefaultTask method of SynergyTaskManager" );
        }
        switch ( currentTaskState )
        {
            case TASK_STATE_NONE:
                // if a default task is set, then check in that
                // otherwise we have an error
                if ( SynergyUtil.getDefaultTask( logger, ccmAddr ) != 0 )
                {
                    SynergyUtil.checkinDefaultTask( logger, comment, ccmAddr );
                }
                else
                {
                    throw new ScmException(
                                            "Check in not possible: no default task is set and no task has been created with SynergyTaskManager." );
                }
                break;
            case TASK_STATE_CREATED:
                SynergyUtil.checkinTask( logger, currentTaskNumber, comment, ccmAddr );
                currentTaskState = TASK_STATE_COMPLETED;
                break;
            case TASK_STATE_COMPLETED:
                // if a default task is set, then check in that
                // otherwise do nothing, as all tasks and all files with them have
                // been checked in
                if ( SynergyUtil.getDefaultTask( logger, ccmAddr ) != 0 )
                {
                    SynergyUtil.checkinDefaultTask( logger, comment, ccmAddr );
                }
                else
                {
                    if ( logger.isDebugEnabled() )
                    {
                        logger.debug( "Synergy : No check in necessary as default task and all tasks created with SynergyTaskManager have already been checked in." );
                    }
                }
                break;
            default:
                throw new IllegalStateException( "Programming error: SynergyTaskManager is in unkown state." );
        }
    }
}
