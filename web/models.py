from werkzeug.security import generate_password_hash

from flask_login import UserMixin


class User(UserMixin):
    def __init__(self):
        self.id = 0
        self.name = None
        self.email = None
        self.pwd = None

    def __repr__(self):
        return "User:{}".format(self.name)

    def set_pwd(self):
        self.pwd = generate_password_hash(self.pwd)

    def get_id(self):
        return self.id
