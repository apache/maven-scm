package org.apache.maven.scm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;

/**
 * parameters used by implementation to perform untag operation
 *
 * @since 1.11.2
 */
public class ScmUntagParameters
    implements Serializable
{
    /**
     * serial version id
     */
    private static final long serialVersionUID = -7508529445894924957L;

    /**
     * id of tag to delete/remove
     */
    private String tag;

    /**
     * commit message
     */
    private String message;

    /**
     * constructor with tag and message
     *
     * @param tag     tag id
     * @param message commit message
     */
    public ScmUntagParameters( String tag, String message )
    {
        this.tag = tag;
        this.message = message;
    }

    /**
     * get tag id
     *
     * @return tag id
     */
    public String getTag()
    {
        return tag;
    }

    /**
     * set tag id
     *
     * @param tag tag id
     */
    public void setTag( String tag )
    {
        this.tag = tag;
    }

    /**
     * get commit message
     *
     * @return commit message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * set commit message
     *
     * @param message commit message
     */
    public void setMessage( String message )
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return ScmUntagParameters.class.getSimpleName() + " [tag=" + tag + ", message=" + message + "]";
    }
}
