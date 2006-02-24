package org.apache.maven.scm.provider.bazaar.command.diff;

import org.apache.maven.scm.provider.bazaar.BazaarTestUtils;
import org.apache.maven.scm.tck.command.diff.DiffCommandTckTest;

public class BazaarDiffCommandTest
    extends DiffCommandTckTest
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
