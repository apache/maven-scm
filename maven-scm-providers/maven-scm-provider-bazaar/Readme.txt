How to build bazaar scm provider?

You must have bazaar installed on your machine. Bazaar is available at (http://bazaar-vcs.org/)
It requires Python and some other modules (http://bazaar-vcs.org/Installation).

For bazaar version 0.8+ consult (http://bazaar-vcs.org/Installation).

For bazaar pre 0.8 on windows:

For windows users, you'll need, after installation to create a bzr.bat file
added in your path with the following content:

----------
python <path_to_bzr_home>\bzr %*
----------

Unfortunately, you can't use the Cygwin bzr at this time.
While it will start with a batch file called:

----------
c:\cygwin\bin\python2.4.exe /usr/bin/bzr %*
----------

Any of the arguments that are files cannot be translated to cygwin paths.
What is required is support in Commandline to recognise a cygwin environment,
and for anything that was created with createArgument().setFile(...), it should be
translated with cygpath -w. Commands would be executed with "sh".
