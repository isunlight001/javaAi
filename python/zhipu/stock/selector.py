from . import data_service
from datetime import datetime, timedelta

def select_stocks():
    stocks = data_service.get_stock_list()
    today = datetime.now().strftime('%Y%m%d')
    five_days_ago = (datetime.now() - timedelta(days=5)).strftime('%Y%m%d')
    results = []
    for stock in stocks:
        ts_code = stock['ts_code']
        # 一阳穿三线
        ma_list = data_service.get_ma(ts_code, five_days_ago, today, ma=[5,10,20])
        if not ma_list:
            continue
        last_ma = ma_list[-1]
        close = last_ma['close']
        if close <= last_ma.get('ma5', 0) or close <= last_ma.get('ma10', 0) or close <= last_ma.get('ma20', 0):
            continue
        # 涨幅大于3%
        daily = data_service.get_daily(ts_code, today, today, fields=['pct_chg'])
        if not daily or daily[0].get('pct_chg', 0) <= 3:
            continue
        # 量能放大
        vol_list = data_service.get_daily(ts_code, five_days_ago, today, fields=['vol'])
        if not vol_list:
            continue
        avg_vol = sum([v['vol'] for v in vol_list]) / len(vol_list)
        if daily[0]['vol'] <= avg_vol * 1.2:
            continue
        # MACD红柱
        macd_list = data_service.get_macd(ts_code, five_days_ago, today)
        if not macd_list or macd_list[-1].get('macd', 0) <= 0:
            continue
        results.append({
            'stock': stock,
            'reason': '一阳穿三线+涨幅大于3%+量能放大+MACD红柱'
        })
    return results 