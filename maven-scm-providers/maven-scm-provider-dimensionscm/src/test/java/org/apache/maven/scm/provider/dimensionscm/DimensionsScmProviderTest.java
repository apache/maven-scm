package org.apache.maven.scm.provider.dimensionscm;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.dimensionscm.DimensionsScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.Test;

import java.util.List;

public class DimensionsScmProviderTest extends ScmTestCase
{
    @Test
    public void testGetScmType()
    {
        assertEquals( ( new DimensionsScmProvider() ).getScmType(), "dimensionscm" );
    }

    @Test
    public void testMakeProviderScmRepositoryStringChar() throws Exception
    {
        String url = "scm:dimensionscm://dmsys:dmsys_test@vmsql2k8win32:671/cm_typical@dim10/qlarius:test";
        ScmRepository repository = getScmManager().makeScmRepository( url );
        assertEquals( repository.getProvider(), "dimensionscm" );
    }

    @Test
    public void testValidateScmUrlStringChar()
    {
        String validUrl = "//dmsys:dmsys_test@vmsql2k8win32:671/cm_typical@dim10/qlarius:test/folder_name";
        DimensionsScmProvider provider = new DimensionsScmProvider();
        List<String> error = provider.validateScmUrl( validUrl, ':' );
        assertEquals( 0, error.size() );
        String invalidUrl = "//dmsys:dmsys_test@vmsql2k8win32:671/";
        error = provider.validateScmUrl( invalidUrl, ':' );
        assertTrue( error.size() > 0 );
    }

}
