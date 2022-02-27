#coding=utf8
import sys

def strQ2B(ustring):
    """全角转半角"""
    rstring = ""
    for uchar in ustring:
        inside_code=ord(uchar)
        if inside_code == 12288:                              #全角空格直接转换
            inside_code = 32
        elif (inside_code >= 65281 and inside_code <= 65374): #全角字符（除空格）根据关系转化
            inside_code -= 65248

        #rstring += unichr(inside_code)
        rstring += chr(inside_code)
    return rstring

def strB2Q(ustring):
    """半角转全角"""
    rstring = ""
    for uchar in ustring:
        inside_code=ord(uchar)
        if inside_code == 32:                                 #半角空格直接转化
            inside_code = 12288
        elif inside_code >= 32 and inside_code <= 126:        #半角字符（除空格）根据关系转化
            inside_code += 65248

        #rstring += unichr(inside_code)
        rstring += chr(inside_code)
    return rstring

if __name__ == "__main__":
    #b = strQ2B("ｍｎ123 abc博客园".decode('utf8'))
    b = strQ2B("ｍｎ123 abc博客园")
    print(b)

    #c = strB2Q("ｍｎ123 abc博客园".decode('utf8'))
    c = strB2Q("ｍｎ123 abc博客园")
    print(c)
