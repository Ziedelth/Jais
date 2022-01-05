#!/bin/bash

# Création des dossiers
folder=container
mkdir $folder
mkdir $folder/bin
mkdir $folder/lib
mkdir $folder/lib/x86_64-linux-gnu
mkdir $folder/lib64
mkdir $folder/tmp
mkdir $folder/media

# Ajout des commands necéssaires dans le conteneur
commands="/bin/bash /bin/ls /bin/touch /bin/cat /bin/mkdir"

for command in $commands ; do
  echo "--- Ajout de la commande ${command} dans le conteneur... ---"
  cp -v "$command" "${folder}/bin/";
  list="$(ldd "$command" | egrep -o '/lib.*\.[0-9]')"

  for i in $list ; do
    cp -v "$i" "${folder}${i}";
  done
done

# Montages
#mount --bind /tmp $folder/tmp
#mount --bind /media $folder/media

# Unshare
#unshare -Ufp --kill-child --pid --net -- /bin/bash

# Chroot
chroot $folder /bin/bash