package org.apache.maven.scm.provider.bazaar.command.checkin;

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

import org.apache.maven.scm.provider.bazaar.BazaarTestUtils;
import org.apache.maven.scm.tck.command.checkin.CheckInCommandTckTest;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 */
public class BazaarCheckInCommandTest
    extends CheckInCommandTckTest
{
    public String getScmUrl()
        throws Exception
    {
        return BazaarTestUtils.getScmUrl();
    }

    public void initRepo()
        throws Exception
    {
        BazaarTestUtils.initRepo();
    }
}
