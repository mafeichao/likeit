import requests
import json
import logging

import models


def get_user_by_name(name):
    try:
        result = requests.get('http://127.0.0.1:8082/user/get_by_name.json?name=%s' % name)
        objs = json.loads(result.text)
        user = models.User()
        user.name = objs["name"]
        user.id = objs["uid"]
        user.email = objs["email"]
        logging.info("get user:%s", user)
        return user
    except Exception as e:
        logging.error("get user failed:%s,%s", name, str(e))
    return None


def get_user_by_id(id):
    try:
        result = requests.get('http://127.0.0.1:8082/user/get_by_id.json?id=%d' % id)
        objs = json.loads(result.text)
        user = models.User()
        user.name = objs["name"]
        user.id = objs["uid"]
        user.email = objs["email"]
        return user
    except Exception as e:
        logging.error("get user failed:%d,%s", id, str(e))
    return None


def get_user_by_email(email):
    return None


def verify_user_pwd(name, pwd):
    try:
        result = requests.get('http://127.0.0.1:8082/user/verify_pwd.json?name=%s&pwd=%s' % (name, pwd))
        objs = json.loads(result.text)
        user = models.User()
        user.name = objs["name"]
        user.id = objs["uid"]
        user.email = objs["email"]
        return user
    except Exception as e:
        logging.error("verify user failed:%s,%s,%s", name, pwd, str(e))
    return None


def register_user(user, email, pwd):
    return None


def search(query, page):
    try:
        result = requests.get('http://127.0.0.1:8082/search/query.json?q=%s&page=%d' % (query, page))
        objs = json.loads(result.text)
        return objs
    except Exception as e:
        logging.error("search failed:%s,%s", query, str(e))
    return None


def uid_ais(uid, page):
    try:
        result = requests.get('http://127.0.0.1:8082/search/uid_ais.json?uid=%d&page=%d' % (uid, page))
        objs = json.loads(result.text)
        return objs
    except Exception as e:
        logging.error("all favs failed:%s,%s", uid, str(e))
    return None


def index_url(uid, url, source, tags):
    try:
        result = requests.get('http://127.0.0.1:8082/indexer/index_url.json?uid=%d&url=%s&source=%s&tags=%s' % (uid, url, source, tags))
        return result.text
    except Exception as e:
        logging.error("index url failed:%s,%s,%s", str(uid), url, str(e))
        return "exception:%s" % str(e)
