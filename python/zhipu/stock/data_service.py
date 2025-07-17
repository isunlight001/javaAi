import tushare as ts
from config import TUSHARE_TOKEN
import pandas as pd

ts.set_token(TUSHARE_TOKEN)
pro = ts.pro_api()

# 股票基础信息
def get_stock_list():
    df: pd.DataFrame = pro.stock_basic(list_status='L', fields='ts_code,symbol,name,area,industry,list_date')
    return df.to_dict(orient='records')  # type: ignore

# 日行情
def get_daily(ts_code, start_date, end_date, fields=None):
    df = pro.daily(ts_code=ts_code, start_date=start_date, end_date=end_date)
    if fields:
        df = df[fields]
    return df.to_dict(orient='records')

# 均线
def get_ma(ts_code, start_date, end_date, ma=[5,10,20]):
    df = ts.pro_bar(ts_code=ts_code, start_date=start_date, end_date=end_date, ma=ma)
    return df.to_dict(orient='records')

# MACD
def get_macd(ts_code, start_date, end_date, freq='D'):
    df = ts.pro_bar(ts_code=ts_code, start_date=start_date, end_date=end_date, freq=freq, factors=['macd'])
    return df.to_dict(orient='records') 