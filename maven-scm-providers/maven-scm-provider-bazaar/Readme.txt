How to build bazaar scm provider?

You must have bazaar installed on your machine. Bazaar is available there (http://bazaar.canonical.com/)
It require Python and some others modules (http://bazaar.canonical.com/Installation).

For windows users, you'll need, after installation to create a bzr.bat file added in your path with the folowing content:

----------
python <path_to_bzr_home>\bzr %*
----------

Unfortunately, you can't use the Cygwin bzr at this time. While it will start with a batch file called:

----------
c:\cygwin\bin\python2.4.exe /usr/bin/bzr %*
----------

Any of the arguments that are files cannot be translated to cygwin paths. What is required is support in Commandline
to recognise a cygwin environment, and for anything that was created with createArgument().setFile(...), it should be
translated with cygpath -w. Commands would be executed with "sh".
