package org.apache.maven.scm.provider.bazaar.command.changelog;

import org.apache.maven.scm.provider.bazaar.BazaarTestUtils;
import org.apache.maven.scm.tck.command.changelog.ChangeLogCommandTckTest;

public class BazaarChangeLogCommandTckTest
    extends ChangeLogCommandTckTest
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
