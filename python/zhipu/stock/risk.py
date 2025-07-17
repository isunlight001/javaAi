from . import data_service
from datetime import datetime

def check_risk(ts_code, buy_price, buy_day_low):
    today = datetime.now().strftime('%Y%m%d')
    # 获取当前价格
    daily = data_service.get_daily(ts_code, today, today, fields=['close'])
    if not daily:
        return {'success': False, 'error': '无法获取当前价格'}
    current_price = daily[0]['close']
    profit_percent = (current_price - buy_price) / buy_price * 100
    # 止盈：收益超10%且15分钟MACD顶背离
    macd_15min = data_service.get_macd(ts_code, today, today, freq='15min')
    macd_top_div = False
    if macd_15min and len(macd_15min) >= 2:
        macd1 = macd_15min[-2].get('macd', 0)
        macd2 = macd_15min[-1].get('macd', 0)
        close1 = macd_15min[-2].get('close', 0)
        close2 = macd_15min[-1].get('close', 0)
        macd_top_div = macd2 < macd1 and close2 > close1
    if profit_percent > 10 and macd_top_div:
        return {'success': True, 'action': '止盈', 'reason': '收益超10%且15分钟MACD顶背离', 'currentPrice': current_price, 'profitPercent': profit_percent}
    # 止损：收盘价低于买入日最低价
    if current_price < buy_day_low:
        return {'success': True, 'action': '止损', 'reason': '收盘价低于买入日最低价', 'currentPrice': current_price, 'profitPercent': profit_percent}
    return {'success': True, 'action': '持有', 'reason': '未触发止盈止损条件', 'currentPrice': current_price, 'profitPercent': profit_percent} 