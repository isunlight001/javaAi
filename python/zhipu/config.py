import os

# tushare token
TUSHARE_TOKEN = os.getenv('TUSHARE_TOKEN', '1e69de06c2e26b5348f94c299a25e154b6a809b7eb88192503531e48')

# 数据库配置
SQLALCHEMY_DATABASE_URL = os.getenv('DATABASE_URL', 'sqlite:///./zhipu.db')

# 邮件配置
MAIL_USERNAME = os.getenv('MAIL_USERNAME', '903635811@qq.com')
MAIL_PASSWORD = os.getenv('MAIL_PASSWORD', 'okfrcokdlwlbbbac')
MAIL_HOST = os.getenv('MAIL_HOST', 'smtp.qq.com')
MAIL_PORT = int(os.getenv('MAIL_PORT', 465))
MAIL_RECEIVER = os.getenv('MAIL_RECEIVER', '57954282@qq.com')
MAIL_SSL = True 