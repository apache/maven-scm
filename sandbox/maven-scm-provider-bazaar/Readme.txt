How to build bazaar scm provider?

You must have bazaar installed on your machine. Bazaar is available there (http://bazaar.canonical.com/)
It require Python and some others modules (http://bazaar.canonical.com/Installation).

For windows users, you'll need, after installation to create a bzr.bat file added in your path with the folowing content:

----------
python <path_to_bzr_home>\bzr %*
----------
