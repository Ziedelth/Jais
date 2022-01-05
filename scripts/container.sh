#!/bin/bash

# Variables
folder=container
commands="/bin/bash /bin/ls /bin/touch /bin/cat /bin/mkdir /bin/hostname /bin/sleep /bin/clear"
cpu_limit=512
memory_limit=10485760 # 10 Mio

# Création des dossiers
mkdir $folder
mkdir $folder/bin
echo '#!/bin/bash
for i in {1..10000} ; do
  sleep 10s &
done' > $folder/bin/mycommand
chmod +111 $folder/bin/mycommand
mkdir $folder/lib
mkdir $folder/lib/x86_64-linux-gnu
mkdir $folder/lib64
mkdir $folder/tmp
mkdir $folder/media
mkdir $folder/dev
touch $folder/dev/null

# Ajout des commands necéssaires dans le conteneur
for command in $commands ; do
  echo "--- Ajout de la commande ${command} dans le conteneur... ---"
  cp -v "$command" "${folder}/bin/";
  list="$(ldd "$command" | egrep -o '/lib.*\.[0-9]')"

  for i in $list ; do
    cp -v "$i" "${folder}${i}";
  done
done

# Montages
mount --bind /tmp $folder/tmp
mount --bind /media $folder/media

# Création d'un sous-script
echo '#!/bin/bash
mkdir /sys/fs/cgroup/cpu/conteneur
mkdir /sys/fs/cgroup/memory/conteneur
echo "$$" > /sys/fs/cgroup/cpu/conteneur/cgroup.procs
echo $$ > /sys/fs/cgroup/memory/conteneur/cgroup.procs
echo $2 > /sys/fs/cgroup/cpu/conteneur/cpu.shares
echo $3 > /sys/fs/cgroup/memory/conteneur/memory.limit_in_bytes
hostname conteneur
chroot $1 bash' > subscript

chmod +111 subscript

# Unshare
unshare --fork --uts --pid --net -- ./subscript $folder $cpu_limit $memory_limit