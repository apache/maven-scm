#!/bin/bash

scripts=`ls src/main/bash/maven-scm-*`

dest=$HOME/bin2

echo "Installing scripts to '$dest'."

if [ ! -x $dest ]
then
  echo "Destination directory '$dest' doesn't exist!" 1>&2

  exit 1
fi

for script in $scripts
do
  cp $script $dest/$script
  chmod +x $HOME/bin/$script
done

echo "Installation done."
