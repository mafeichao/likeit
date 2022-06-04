#coding=utf8
import sys
import logging

for line in sys.stdin:
    line = line.strip()

    str_begin = 'INSERT INTO `user_urls` VALUES '
    str_vals = line[len(str_begin):-1]
    #print(str_vals)

    R = []
    ps = None
    record = []
    word = ''
    status = 'B'
    for s in str_vals:
        if status == 'B':
            if s == ',':
                pass
            elif s == '(':
                status = 'R'
            else:
                logging.error('error:%s,%s' % (status, s))
                break
        elif status == 'R':
            if s == "'":
                word += s
                status = 'Q'
            elif s == ',':
                record.append(word)
                word = ''
            elif s == '(':
                logging.error('error:%s,%s' % (status, s))
                break
            elif s == ')':
                record.append(word)
                #print(",".join(record))
                R.append(record)
                word = ''
                record = []
                status = 'B'
            else:
                word += s
                status = 'W'
        elif status == 'Q':
            if s == "'":
                word += s
                if ps == '\\':
                    pass
                else:
                    status = 'W'
            else:
                word += s
        elif status == 'W':
            if s == "'":
                word += s
                if ps == '\\':
                    pass
                else:
                    status = 'Q'
            elif s == ',':
                record.append(word)
                word = ''
                status = 'R'
            elif s == ')':
                record.append(word)
                #print(",".join(record))
                R.append(record)
                word = ''
                record = []
                status = 'B'
            elif s == '(':
                logging.error('error:%s,%s' % (status, s))
                break
            else:
                word += s
        else:
            logging.error('error:%s,%s' % (status, s))
            break
        ps = s

    #print("===:" + word)
    SR = []
    for r in R:
        SR.append("(%s)" % ",".join(r))
    print('INSERT INTO `user_urls` VALUES %s;' % ",".join(SR))
