package org.apache.maven.scm.provider.synergy.util;

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
 * This class contains the different Synergy roles available.
 * 
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyRole
{

    public static final SynergyRole BUILD_MGR = new SynergyRole( "build_mgr" );

    public static final SynergyRole CCM_ADMIN = new SynergyRole( "ccm_admin" );

    private String value;

    private SynergyRole( String value )
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }

}
