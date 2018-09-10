package org.apache.maven.scm.provider.accurev;

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

/**
 * 
 */
public enum AccuRevCapability
{
    DIFF_BETWEEN_STREAMS( "4.7.2", null ), POPULATE_TO_TRANSACTION( "4.9.0", "4.9.9" ), STAT_ADDED_NOT_PROMOTED_BUG(
        "4.9.0", "4.9.9" );

    public static final String DEFAULT_VERSION_FOR_TESTS = "4.9.0";
    
    private String fromVersion;

    private String toVersion;

    AccuRevCapability( String fromVersion, String toVersion )
    {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    /**
     * @todo simple string compare because the version numbers have never used
     *       more than one digit.
     * @param version
     * @return if the capability is available for this version
     */
    public boolean isSupported( String version )
    {
        return ( fromVersion == null || fromVersion.compareTo( version ) <= 0 )
            && ( toVersion == null || toVersion.compareTo( version ) >= 0 );

    }

}
