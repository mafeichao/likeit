#0 0,12 * * * /work/likeit/script/likeit_backup.sh
source ~/.bashrc
conda activate py27

set -x
UTIL="import util;print util"
ROOT=$(cd `dirname $0`;pwd)
cd $ROOT

HOST=$(python -c "$UTIL.get_ip()")
JOB=$(basename $0)
PROJECT="likeit"
OWNER="owner"

find ../log -mtime +7 | xargs rm -rf
find /backup/ -mtime +30 | xargs rm -rf
dt=`python -c "$UTIL.today('%Y%m%d%H')"`
LOG="../log/syncup.log.$dt"
{
    mysqldump likeit -h 82.157.160.66 > /backup/likeit.$dt
} > $LOG 2>&1
