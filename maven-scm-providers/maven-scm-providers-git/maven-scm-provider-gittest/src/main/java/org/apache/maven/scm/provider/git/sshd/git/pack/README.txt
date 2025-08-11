This is just a minimum fork of https://github.com/apache/mina-sshd/tree/sshd-2.11.0/sshd-git/src/main/java/org/apache/sshd/git/pack
to include the patch https://github.com/apache/mina-sshd/pull/794.

Otherwise, the SSH server cannot be used on Windows as it leaves files open.
TODO: Remove once a Mina SSHD release is available that includes this patch.