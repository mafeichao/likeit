# coding=utf8

import logging

from werkzeug.urls import url_parse

from flask import Flask, render_template, request, redirect, url_for, flash
from flask_bootstrap import Bootstrap
from flask_login import LoginManager, login_required, current_user, login_user, logout_user
from flask_paginate import Pagination
from flask_script import Manager
#from flask_moment import Moment
from gevent import pywsgi

import forms
import service

app = Flask(__name__)
app.config["SECRET_KEY"] = "likeit"

login = LoginManager(app)
login.login_view = 'login'
login.login_message = '请先登陆！'
@login.user_loader
def load_user(uid):
    return service.get_user_by_id(uid)


#moment = Moment(app)
bootstrap = Bootstrap(app)
manager = Manager(app)

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(filename)s[line:%(lineno)d] - %(levelname)s: %(message)s')  # logging.basicConfig函数对日志的输出格式及方式做相关配置


@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))

    form = forms.LoginForm()
    if form.validate_on_submit():
        # flash('Login requested for{},remember_me{}'.format(form.username.data,form.remember_me.data))
        user = service.verify_user_pwd(form.username.data, form.password.data)
        if user is None:
            flash('无效用户名或密码')
            return redirect(url_for('login'))

        login_user(user)
        # login_user(user, remember=form.remember_me.data)
        # logging.info("user:" + str(user.get_id()) + ",guser:" + str(current_user.get_id()))

        next_page = request.args.get('next')
        logging.info("next page:%s", next_page)
        if not next_page or url_parse(next_page).netloc != '':
            next_page = url_for('index')
        return redirect(next_page)

    return render_template('login.html', title='登录', form=form)


@app.route('/logout')
def logout():
    logout_user()
    return redirect(url_for('index'))


@app.route('/register', methods=['POST', 'GET'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))

    form = forms.RegistrationForm()
    if form.validate_on_submit():
        user = form.username.data
        email = form.email.data
        pwd = form.password.data
        if service.register_user(user, email, pwd):
            flash('祝贺您注册成功,请登录！')
            return redirect(url_for('login'))
        else:
            return redirect(url_for('register'))

    return render_template('register.html', title='注册', form=form)


@app.route("/", methods=["GET", "POST"])
@app.route("/home", methods=["GET", "POST"])
@login_required
def index():
    page = request.args.get("page", type=int, default=1)
    logging.info("method:%s, page:%d", request.method, page)

    if page <= 1:
        page = 1

    total = 0
    docs = []
    pagination = None
    objs = service.uid_ais(current_user.get_id(), page)
    if objs:
        total = objs["total"]
        docs = objs["data"]
        per_page = len(docs)
        # logging.info("result:%s", docs)
        pagination = Pagination(bs_version=3, page=page, per_page_parameter=per_page, total=total)
    return render_template("index.html", title="首页", total=total, docs=docs, pages=pagination)


@app.route("/search", methods=['GET'])
@login_required
def search():
    query = request.args.get("query", type=str, default=None)
    page = request.args.get("page", type=int, default=1)
    stype = request.args.get("type", type=str, default="self")
    logging.info("method:%s, query:%s, page:%d, type:%s", request.method, query, page, stype)

    total = 0
    docs = []
    pagination = None
    if query:
        if page <= 1:
            page = 1

        objs = service.search(query, page, stype)
        total = objs["total"]
        docs = objs["data"]
        per_page = len(docs)
        # logging.info("result:%s", docs)
        pagination = Pagination(bs_version=3, page=page, per_page_parameter=per_page, total=total)

    return render_template("search.html", query=query, total=total, docs=docs, pages=pagination, stype=stype)


@app.route("/index_url", methods=['GET'])
@login_required
def index_url():
    uid = request.args.get("uid", type=int, default=-1)
    url = request.args.get("url", type=str, default=None)
    source = request.args.get("source", type=str, default=None)
    tags = request.args.get("tags", type=str, default='')
    query = request.args.get("query", type=str, default='')
    logging.info("method:%s, uid:%d, url:%s, source:%s, tags:%s, query:%s", request.method, uid, url, source, tags, query)

    return service.index_url(uid, url, source, tags, query)


if __name__ == "__main__":
    #manager.run()
    server = pywsgi.WSGIServer(('0.0.0.0',8888),app)
    server.serve_forever()
