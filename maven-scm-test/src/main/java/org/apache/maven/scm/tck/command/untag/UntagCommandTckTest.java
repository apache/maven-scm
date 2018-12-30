package org.apache.maven.scm.tck.command.untag;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * This test tests the untag command.
 */
public abstract class UntagCommandTckTest
    extends ScmTckTestCase
{

    protected String getTagName()
    {
        return "test-untag";
    }

    public void testUntagCommandTest()
        throws Exception
    {
        String tag = getTagName();
        ScmProvider scmProvider = getScmManager().getProviderByUrl( getScmUrl() );
        ScmRepository scmRepository = getScmRepository();
        ScmFileSet files = new ScmFileSet( getWorkingCopy() );
        TagScmResult tagResult = scmProvider.tag( scmRepository, files, tag, new ScmTagParameters() );

        assertResultIsSuccess( tagResult );
        CommandParameters params = new CommandParameters();
        params.setString( CommandParameter.TAG_NAME, tag );

        UntagScmResult untagResult = scmProvider.untag( scmRepository, files, params );

        assertResultIsSuccess( untagResult );

        try
        {
            untagResult = scmProvider.untag( scmRepository, files, params );
            assertFalse( untagResult.isSuccess() ); // already been deleted
        }
        catch ( ScmException ignored )
        {
        }

        try
        {
            CheckOutScmResult checkoutResult =
                getScmManager().checkOut( scmRepository, new ScmFileSet( getAssertionCopy() ), new ScmTag( tag ) );
            assertFalse( checkoutResult.isSuccess() ); // can't check out a deleted tags
        }
        catch ( ScmException ignored )
        {
        }
    }

}
