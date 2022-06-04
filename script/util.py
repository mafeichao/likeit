#coding=utf8
import os
import re
import sys
import time
import math
import random
import logging
import hashlib
import datetime

logging.basicConfig(level = logging.INFO,
        format='%(asctime)s %(filename)s[line:%(lineno)d] %(levelname)s %(message)s',
        datefmt='%a, %d %b %Y %H:%M:%S')

def md5sum(line):
    md5hash = hashlib.md5(line)
    return md5hash.hexdigest()

def get_conda_source():
    try:
        cmd = "conda info | grep -i 'base environment'"
        fd = os.popen(cmd)
        if fd:
            line = fd.readlines()[0].strip()
            cols = line.split()
            return cols[3] + "/etc/profile.d/conda.sh"
    except Exception as e:
        logging.error("error:%s", str(e))
    return None

def rate(expo, click):
    res = 0.0
    if click <= 0.0:
        return res

    if click > expo:
        avg = (click + expo) * 0.5
        click = avg
        expo = avg
    
    z = 1.96
    p = click*1.0 / expo
    res = (p + math.pow(z, 2) / (2 * expo) - z * math.sqrt(p * (1 - p) / expo + math.pow(z, 2) / (4 * math.pow(expo, 2)))) / (1 + math.pow(z, 2) / expo)
    return res

def last_modify(f):
    info = os.stat(f)
    return int(info.st_mtime)

def get_mtime(fp, f='%Y%m%d'):
    fp = unicode(fp)
    t = os.path.getmtime(fp)
    return ts2str(t, f)
    
def balance(s, n):
    r = random.randint(1,n)
    return s + '-' + str(r) 

def replace_with(S,s,t):
    return S.replace(s,t)

def expand(sg):
    if '-' in sg:
        sgs = os.popen("echo {%s}" % sg.replace('-','..')).readlines()[0].strip().split(' ')
    elif ',' in sg:
        sgs = sg.split(',')
    else:
        sgs = [sg]
    return sgs

def str2ts(s, f='%Y%m%d'):
    uts = time.mktime(time.strptime(s,f))
    return int(uts)

def ts2str(t, f='%Y%m%d %H:%M:%S'):
    dt = datetime.datetime.fromtimestamp(t)
    return dt.strftime(f)

def hour_partitions(dt, hr, rg):
    result = []
    for i in range(rg):
        cur = n_hours_ago(i, dt+hr)
        result.append('dt=%s/hour=%s' % (cur[:8],cur[8:]))
    return ",".join(result)

def hour_partitions2(dt, hr, rg):
    result = []
    for i in range(rg):
        cur = n_hours_ago(i, dt+hr)
        result.append('%s/%s/%s/%s' % (cur[:4],cur[4:6], cur[6:8], cur[8:]))
    return ",".join(result)

def get_ip():
    cmd = '/usr/sbin/ifconfig'
    fd = os.popen(cmd)
    result = set()
    if fd:
        lines = fd.readlines()
        for line in lines:
            line = line.strip()
            if line == "":
                continue
            if line.startswith('inet '):
                ip = line.split(' ')[1]
                if ip == '127.0.0.1':
                    continue
                result.add(ip)
 
    return ",".join(result)

def get_table_location(table, hive = 'hive'):
    cmd = "%s -e 'desc formatted %s'" % (hive, table)
    logging.debug(cmd)
    fd = os.popen(cmd)
    if fd:
        lines = fd.readlines() 
        for line in lines:
            line = line.strip()
            if line == "": 
                continue

            if line.startswith("Location:"):
                kv = line.split(":", 1)
                return kv[1].strip()
    return None 
 
def partition_size(table,date,hour,DT='dt',HR='hour'):
    if hour == None:
        cmd = "%s -e 'desc %s partition(%s=%s)' 2>/dev/null" % (ahive,table,DT,date)
    else:
        cmd = "%s -e 'desc %s partition(%s=%s,%s=%s)' 2>/dev/null" % (ahive,table,DT,date,HR,hour)

    logging.debug(cmd)
    fd = os.popen(cmd)
    if fd:
        lines = fd.readlines()
        for line in lines:
            line = line.strip("| \n\t")
            if line == "":
                continue

            if "PartitionSize" in line:
                items = line.split(":")
                return int(items[1].strip())
    return 0

def table_fields(table, hive, withtp = False):
    cmd = "%s -e 'desc %s'" % (hive, table)
    lines = os.popen(cmd).readlines()
    if len(lines) == 0:
        return ""
    
    idx = 0
    beg_idx = 0
    end_idx = 0
    for line in lines:
        if 'Field' in line:
            beg_idx = idx
        elif 'Partition Columns' in line:
            end_idx = idx
        idx += 1
    
    res = []
    for idx in range(len(lines)):
        if idx > beg_idx and idx < end_idx:
            line = lines[idx]
            if line.startswith('|'):
                items = line.split('|')
                items = map(lambda x:x.strip(), items)
                items = filter(lambda x:x != "",items)
                if not withtp:
                    res.append(items[0])
                else:
                    res.append(items[0] + ' ' + items[1])
    return ",".join(res)

def now():
    return int(time.time())

def today(fmt='%Y%m%d'):
    now = datetime.datetime.now()
    return now.strftime(fmt)

def yesterday(fmt='%Y%m%d'):
    now = datetime.datetime.now()
    delta = datetime.timedelta(days = 1)
    return (now - delta).strftime(fmt)

def hour():
    now = datetime.datetime.now()
    if now.hour >= 10:
        return str(now.hour)
    return '0' + str(now.hour)

def n_days_ago(n,dt,fmt='%Y%m%d'):
    ndt = datetime.datetime.strptime(dt, fmt)
    delta = datetime.timedelta(days = n)
    return (ndt - delta).strftime(fmt)

def n_hours_ago(n,dt,fmt='%Y%m%d%H'):
    ndt = datetime.datetime.strptime(dt, fmt)
    delta = datetime.timedelta(hours = n)
    return (ndt - delta).strftime(fmt)

def n_mins_ago(n,dt,fmt='%Y%m%d%H%M'):
    ndt = datetime.datetime.strptime(dt, fmt)
    delta = datetime.timedelta(minutes = n)
    return (ndt - delta).strftime(fmt)
    
def date_range(end_dt, days, prefix=""):
    end_dt = datetime.datetime.strptime(end_dt, '%Y%m%d')
    delta = datetime.timedelta(days = 1)
    res = []
    while days > 0:
        res.append(prefix + end_dt.strftime('%Y%m%d'))
        days -= 1
        end_dt -= delta
    return ",".join(res)

def hour_range(end_dt, end_hour, hours, dt_pre = 'dt=', hr_pre = 'hour='):
    end_dt_hour = datetime.datetime.strptime(end_dt+end_hour, '%Y%m%d%H')
    delta = datetime.timedelta(hours = 1)
    res = []
    while hours > 0:
        tmp = end_dt_hour.strftime('%Y%m%d%H')
        dt = tmp[:8]
        hr = tmp[8:]
        res.append(dt_pre + dt + '/' + hr_pre + hr)
        hours -= 1
        end_dt_hour -= delta
    return ",".join(res)

def get_latest_date(table, dt = None, hive="hive"):
    cmd = "%s -e 'show partitions %s'" % (hive, table)
    res = os.popen(cmd).readlines()
    if len(res) == 0:
        logging.warning('table %s no partition' % table)
        return '' 
    else:
        res.sort()
    if dt == None:
        return res[-1].split('=')[-1].strip()
    else:
        for fd in res[-1].split('/'):
            kv = fd.split('=')
            if kv[0] == dt:
                return kv[1].strip()
        return ''

def get_latest_date_before_dt(table, dt, hive="hive"):
    cmd = "%s -e 'show partitions %s'" % (hive, table)
    res = os.popen(cmd).readlines()
    if len(res) == 0:
        logging.warning('table %s no partition' % table)
        return '' 
    else:
        res.sort()
        i = len(res) - 1
        while i >= 0:
            t = res[i].split('=')[-1].strip()
            if t <= dt:
                return t
            i -= 1;
        return res[0].split('=')[-1].strip()

def get_latest_path(root, hadoop=""):
    if hadoop == "":
        cmd = 'ls -ltr %s' % root
    else:
        cmd = '%s fs -ls %s' % (hadoop, root)

    res = os.popen(cmd).readlines()
    if len(res) == 0:
        logging.warning('file:%s may not exist' % root)
        return '' 
    else:
        return res[-1].strip().split()[-1]

def rm_path_before_dt(root, dt, hadoop="", skip=False):
    #skip = False
    if hadoop == "":
        cmd = 'ls -ltr %s' % root
    else:
        cmd = '%s fs -ls %s' % (hadoop, root)

    res = os.popen(cmd).readlines()
    dts = []
    for r in res:
        r = r.strip().split()[-1]
        p = r.split('/')[-1]
        if p.startswith('dt='):
            p = p[3:]

        if p <= '20200601':
            #logging.info('skip p:' + p)
            continue

        if p <= dt:
            dts.append(r)

    for p in dts:
        if p == '' or p == '*':
            continue

        if hadoop == "":
            cmd = 'rm -rf %s' % p
        else:
            if skip:
                cmd = '%s fs -rmr -skipTrash %s' % (hadoop, p)
            else:            
                cmd = '%s fs -rmr %s' % (hadoop, p)
        logging.info(cmd)
        os.popen(cmd)

def get_file_size(root, hadoop=""):
    if hadoop == "":
        cmd = 'du -s %s' % root
    else:
        cmd = '%s fs -dus %s' % (hadoop, root)

    res = os.popen(cmd).readlines()
    if len(res) == 0:
        logging.warning('file:%s may not exist' % root)
        return -1 
    else:
        return reduce(lambda x,y:x+y,map(lambda x:int(x.strip().split()[0]), res),0)

def check_file(root, hadoop="", minsz=-1, maxsz=-1):
    if hadoop == "":
        cmd = 'ls %s > /dev/null' % root
    else:
        cmd = '%s fs -ls %s > /dev/null' % (hadoop, root)
    
    ret = os.system(cmd)
    if ret != 0:
        logging.warning('file:%s not exist' % root)
        return 'no_exist'
    else:
        if minsz >= 0:
            sz = get_file_size(root, hadoop)
            if sz < minsz:
                logging.warning('minsz:%d, maxsz:%d, filesz:%d, small' % (minsz, maxsz, sz))
                return 'sz_small'
        if maxsz >= 0:
            sz = get_file_size(root, hadoop)
            if sz > maxsz:
                logging.warning('minsz:%d, maxsz:%d, filesz:%d, large' % (minsz, maxsz, sz))
                return 'sz_large'
        return 'ok'

def wait_file(root, max_try = 1, try_interval = 60, hadoop="", minsz=-1, maxsz=-1):
    cnt = 0
    while True:
        ret = check_file(root, hadoop, minsz, maxsz)
        if ret == 'ok':
            return 'ok'
        if ret == 'sz_large':
            return 'sz_large'
        cnt += 1
        logging.info('tried time:%d for %s' % (cnt, root))
        if cnt >= max_try:
            break
        time.sleep(try_interval)

    if cnt >= max_try:
        logging.warning('reach max try time:%d for %s,ret:%s' % (max_try, root, ret))
    return ret

if __name__ == "__main__":
    '''
    print date_range('20171210', 14,'dt=')

    print get_latest_path("/data0/")
    print get_latest_path("/data/")

    print get_file_size("/data0/")
    print get_file_size("/data/")
    
    print check_file("/data0/")
    print check_file("/data/")
    
    print today()
    print yesterday()
    print yesterday('%Y/%m/%d')

    print wait_file('./test', 3, 5)

    print get_latest_date('actions_in_interest_based_reading', 'dt')
    print get_latest_date('actions_in_interest_based_reading', 'hour')
    
    print get_latest_date('uid_tags_mfc')

    print rate(5,3)
    print rate(1,1)
    print rate(0,0) 

    print n_days_ago(0,'20180501')
    print n_days_ago(1,'20180501')
    print n_days_ago(-1,'20180501')
    
    print str2ts('20180718')
    print ts2str(str2ts('20180718'))
    print ts2str(1532041666)
    print str2ts('201807185','%Y%m%d%H')
    print ts2str(str2ts('201807185','%Y%m%d%H'))

    print n_hours_ago(2, '201808081')
    print n_hours_ago(2, '201808085')

    print hour_partitions('20180808', '0', 18)
    print hour_partitions2('20180808', '0', 18)

    print get_mtime("../conf/mid_tags.dict")
    print get_table_location("read_duration")
    '''
    
    print(hour_range('20190810', '2', 3, 'dt=', 'hour='))
    print(hour_range('20190810', '02', 5, 'dt=', 'hour='))
    print(n_mins_ago(2, '201808081210'))

    print(last_modify("./util.py"))

    print(now())
    print(get_ip())
    print(hour())

    print(get_conda_source())
    
    print(md5sum("md5"))
    print(md5sum("测试"))
    print(md5sum("https://blog.csdn.net/t_xuanfeng123/article/details/107728016"))
