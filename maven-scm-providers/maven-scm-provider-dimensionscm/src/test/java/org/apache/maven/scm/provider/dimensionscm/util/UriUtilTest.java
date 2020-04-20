package org.apache.maven.scm.provider.dimensionscm.util;

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
import org.apache.maven.scm.provider.dimensionscm.util.UrlUtil;
import org.junit.Test;


public class UriUtilTest extends ScmTestCase {


    @Test
    public void testIsValidUrl()
    {
        String url = "//fred:fred_test@dimserver:671/cm_typical@dim12/qlarius:mainline_vs_str/";
        boolean isValid = UrlUtil.isValidUrl( url );
        assertTrue( isValid );

        url = "//fred:fred_test@dimserver/cm_typical@dim12/qlarius:mainline_vs_str/Qlarius_Underwriter/Qlarius_Underwriter/Dialogs/";
        isValid = UrlUtil.isValidUrl( url );
        assertTrue( isValid );

        url = "/fred:fred_test@dimserver:671/cm_typical@dim12/qlarius:mainline_vs_str";
        isValid = UrlUtil.isValidUrl( url );
        assertFalse( isValid );

        url = "//fred:fred_test@dimserver:671/cm_typical@dim12/qlarius/mainline_vs_str";
        isValid = UrlUtil.isValidUrl( url );
        assertFalse( isValid );

        url = "//fred:fred_test@dimserver:671/cm_typical:dim12/qlarius:mainline_vs_str";
        isValid = UrlUtil.isValidUrl( url );
        assertFalse( isValid );
    }
}
