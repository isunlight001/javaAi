#encoding:gbk
import pandas as pd
import numpy as np
import datetime
import talib
import time
import traceback

class StrategyState:
    """策略状态管理类"""
    def __init__(self):
        self.stock = "159915.SZ"  # 创业板ETF
        self.amount = 100000       # 单笔买入金额
        self.target_profit = 0.5  # 15%止盈
        self.stop_loss = 0.03      # 5%止损
        self.buy_price = 0         # 买入价格
        self.waiting_list = []     # 委托列表
        self.macd_fast = 12
        self.macd_slow = 26
        self.macd_signal = 9
        self.last_price = 0.0       # 最新价格

# 创建全局状态对象
A = StrategyState()

def get_current_time_ms():
    """获取当前时间（毫秒格式）"""
    return datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]

def init(C):
    """策略初始化函数"""
    try:
        current_time = get_current_time_ms()
        print(f"[{current_time}] 策略初始化开始...")
        
        # 设置账号信息
        A.acct = ""
        A.acct_type = "STOCK"
        
        if not A.acct or A.acct == '您的交易账号':
            print(f"[{current_time}] 警告：请设置您的交易账号！")
            return
        
        print(f"[{current_time}] 交易账号: A.acct")
        print(f"[{current_time}] 账号类型: A.acct_type")
        print(f"[{current_time}] 目标品种: A.stock")
        
        # 订阅行情
        C.set_universe([A.stock])
        print(f"[{current_time}] 创业板ETF MACD策略启动 | 单笔金额: A.amount元")
        
        # 设置买卖代码
        A.buy_code = 23 if A.acct_type == 'STOCK' else 33
        A.sell_code = 24 if A.acct_type == 'STOCK' else 34
        
        print(f"[{current_time}] 策略初始化完成")
        
    except Exception as e:
        error_time = get_current_time_ms()
        error_details = f"初始化失败: {str(e)}\n{traceback.format_exc()}"
        print(f"[{error_time}] {error_details}")

def get_reliable_price(C):
    """安全获取当前价格"""
    current_time = get_current_time_ms()
    try:
        # 1. 尝试获取1分钟收盘价
        min_data = C.get_market_data(['close'], stock_code=[A.stock], period='1m', count=1)
        if isinstance(min_data, pd.DataFrame) and not min_data.empty:
            last_close = min_data.iloc[-1]['close']
            if last_close > 0:
                print(f"[{current_time}] 获取1分钟收盘价: {last_close}")
                A.last_price = last_close
                return last_close
    except Exception as e:
        print(f"[{current_time}] 获取1分钟数据出错: {str(e)}")
        traceback.print_exc()
    try:
        # 2. 尝试日线收盘价
        day_data = C.get_market_data(['close'], stock_code=[A.stock], period='1d', count=1)
        if isinstance(day_data, pd.DataFrame) and not day_data.empty:
            last_close = day_data.iloc[-1]['close']
            if last_close > 0:
                print(f"[{current_time}] 获取日线收盘价: {last_close}")
                A.last_price = last_close
                return last_close
    except Exception as e:
        print(f"[{current_time}] 获取日线数据出错: {str(e)}")
        traceback.print_exc()
    try:
        # 3. 尝试获取tick价格
        tick_data = C.get_market_data(['quoter'], stock_code=[A.stock], period='tick', count=1)
        if isinstance(tick_data, dict) and 'lastPrice' in tick_data and tick_data['lastPrice']:
            last_price = tick_data['lastPrice'][0]
            if last_price > 0:
                print(f"[{current_time}] 获取Tick价格: {last_price}")
                A.last_price = last_price
                return last_price
    except Exception as e:
        print(f"[{current_time}] 获取Tick数据出错: {str(e)}")
        traceback.print_exc()
    # 4. 使用最后有效价格
    if A.last_price > 0:
        print(f"[{current_time}] 使用最后有效价格: {A.last_price}")
        return A.last_price
    # 5. 无法获取有效价格
    print(f"[{current_time}] 无法获取有效价格")
    return 0

def handlebar(C):
    """主交易逻辑（已修复图片中的缩进问题）"""
    try:
        # 只在最新K线执行
        if not C.is_last_bar():
            return
        
        now = datetime.datetime.now()
        current_time = get_current_time_ms()
        timestamp = now.strftime('%H%M%S')
        
        print(f"\n[{current_time}] handlebar开始执行")
        
        # 交易时间过滤（9:30-14:57）
        if timestamp < '093000' or timestamp > '145700':
            print(f"[{current_time}] 非交易时段，跳过")
            #return
        
        # 账户与持仓检查
        try:
            account_info = get_trade_detail_data(A.acct, A.acct_type, 'account')
            if not account_info:
                print(f"[{current_time}] 账号信息获取失败")
                return
                
            available_cash = account_info[0].m_dAvailable
            print(f"[{current_time}] 可用资金: {available_cash:.2f}")
        except Exception as e:
            print(f"[{current_time}] 账户信息获取失败: {str(e)}")
            return
        
        # 获取当前持仓
        positions = get_trade_detail_data(A.acct, A.acct_type, 'position') or []
        current_position = 0
        
        for pos in positions:
            try:
                if all(hasattr(pos, attr) for attr in ['m_strInstrumentID', 'm_strExchangeID']):
                    pos_id = f"{pos.m_strInstrumentID}.{pos.m_strExchangeID}"
                    if pos_id == A.stock and hasattr(pos, 'm_nCanUseVolume'):
                        current_position = pos.m_nCanUseVolume
            except:
                pass
                
        print(f"[{current_time}] 当前持仓: {current_position}股")
        
        # 处理未完成委托
        if A.waiting_list:
            found_list = []
            orders = get_trade_detail_data(A.acct, A.acct_type, 'order')
            if orders:
                for order in orders:
                    if hasattr(order, 'm_strRemark') and order.m_strRemark in A.waiting_list:
                        found_list.append(order.m_strRemark)
            
            # 从等待列表中移除已完成的委托
            A.waiting_list = [i for i in A.waiting_list if i not in found_list]
            
            if A.waiting_list:
                print(f"[{current_time}] 当前有未查到委托 {A.waiting_list} 暂停后续报单")
                return
        
        # 获取当前价格
        current_price = get_reliable_price(C)
        
        # 价格有效性处理
        if current_price <= 0:
            if A.buy_price > 0:
                current_price = A.buy_price
                print(f"[{current_time}] 使用持仓价: {current_price:.4f}")
            else:
                print(f"[{current_time}] 无有效价格，跳过交易信号检查")
                return
        else:
            print(f"[{current_time}] 当前价格: {current_price:.4f}")
            
        # 止盈止损检查
        if current_position > 0 and A.buy_price > 0:
            profit_ratio = (current_price - A.buy_price) / A.buy_price
            print(f"[{current_time}] 当前盈亏: {profit_ratio*100:.2f}%")
            
            # 止盈逻辑
            if profit_ratio >= A.target_profit:
                msg = f"止盈平仓 {A.stock}@{current_price:.4f} 数量:{current_position}"
                try:
                    passorder(A.sell_code, 1101, A.acct, A.stock, 14, -1, current_position, 'MACD策略', 1, msg, C)
                    print(f"[{current_time}] {msg}")
                    A.waiting_list.append(msg)
                    A.buy_price = 0
                except Exception as e:
                    print(f"[{current_time}] 平仓失败: {str(e)}")
                return
                
            # 止损逻辑
            if profit_ratio <= -A.stop_loss:
                msg = f"止损平仓 {A.stock}@{current_price:.4f} 数量:{current_position}"
                try:
                    passorder(A.sell_code, 1101, A.acct, A.stock, 14, -1, current_position, 'MACD策略', 1, msg, C)
                    print(f"[{current_time}] {msg}")
                    A.waiting_list.append(msg)
                    A.buy_price = 0
                except Exception as e:
                    print(f"[{current_time}] 平仓失败: {str(e)}")
                return
        
        # 开仓信号检查
        if current_position <= 0:
            print(f"[{current_time}] 检查开仓条件...")
            
            # 条件1：30分钟MACD在零轴上方
            macd_cond = is_macd_above_zero(C)
            print(f"[{current_time}] 30分钟MACD零轴上: {'是' if macd_cond else '否'}")
            
            # 条件2：5分钟MACD底背离
            div_cond = is_macd_divergence(C)
            print(f"[{current_time}] 5分钟底背离: {'是' if div_cond else '否'}")
            
            if macd_cond and div_cond:
                # 计算交易数量（ETF最小100股）
                vol = int(A.amount / current_price / 100) * 100
                if vol < 100:
                    print(f"[{current_time}] 交易量不足（{vol}<100）")
                    return
                    
                # 执行买入
                msg = f"MACD底背离买入 {A.stock}@{current_price:.4f} 数量:{vol}"
                try:
                    passorder(A.buy_code, 1101, A.acct, A.stock, 14, -1, vol, 'MACD策略', 1, msg, C)
                    print(f"[{current_time}] {msg}")
                    A.waiting_list.append(msg)
                    A.buy_price = current_price
                except:
                    return None, None
        # ====== 关键修复点：正确位置添加执行完成日志 ======
        print(f"[{current_time}] handlebar执行完成")
    except Exception as e:
        error_time = get_current_time_ms()
        error_details = f"handlebar错误: {str(e)}\n{traceback.format_exc()}"
        print(f"[{error_time}] {error_details}")

# ====== MACD相关函数 ======
def calculate_macd(close_prices):
    try:
        if len(close_prices) < max(A.macd_fast, A.macd_slow):
            return None, None
            
        close_prices = np.array(close_prices, dtype=np.float64)
        close_prices = np.nan_to_num(close_prices, nan=0.0)
        
        macd, signal, _ = talib.MACD(
            close_prices, 
            fastperiod=A.macd_fast, 
            slowperiod=A.macd_slow, 
            signalperiod=A.macd_signal
        )
        return macd, signal
    except:
        return None, None

def is_macd_above_zero(C):
    """检查30分钟MACD是否在零轴上方"""
    try:
        data = C.get_market_data(['close'], stock_code=[A.stock], period='30m', count=50, dividend_type='front_ratio')
        # 判断data类型，兼容DataFrame和Panel等
        closes = None
        if isinstance(data, dict) and A.stock in data:
            closes = data[A.stock]
        elif isinstance(data, pd.DataFrame):
            closes = data['close'] if 'close' in data.columns else None
        if closes is None or len(closes) < max(A.macd_fast, A.macd_slow):
            return False
        macd, signal = calculate_macd(list(closes))
        if macd is None or signal is None:
            return False
        return macd[-1] > 0 and signal[-1] > 0
    except Exception as e:
        print(f"[{get_current_time_ms()}] MACD零轴检查错误: {str(e)}")
        traceback.print_exc()
        return False

def is_macd_divergence(C):
    """检测5分钟MACD底背离"""
    try:
        data = C.get_market_data(['close'], stock_code=[A.stock], period='5m', count=40, dividend_type='front_ratio')
        closes = None
        if isinstance(data, dict) and A.stock in data:
            closes = data[A.stock]
        elif isinstance(data, pd.DataFrame):
            closes = data['close'] if 'close' in data.columns else None
        if closes is None or len(closes) < 20:
            return False
        macd, _ = calculate_macd(list(closes))
        if macd is None or len(macd) < 20:
            return False
        recent_closes = list(closes)[-20:]
        recent_macd = macd[-20:]
        # 寻找价格低点
        price_lows = []
        for i in range(1, len(recent_closes)-1):
            if recent_closes[i] < recent_closes[i-1] and recent_closes[i] < recent_closes[i+1]:
                price_lows.append(i)
        # 寻找MACD低点
        macd_lows = []
        for i in range(1, len(recent_macd)-1):
            if recent_macd[i] < recent_macd[i-1] and recent_macd[i] < recent_macd[i+1]:
                macd_lows.append(i)
        # 检测底背离
        if len(price_lows) >= 2 and len(macd_lows) >= 2:
            last_price_low = price_lows[-1]
            prev_price_low = price_lows[-2]
            last_macd_low = macd_lows[-1]
            prev_macd_low = macd_lows[-2]
            price_down = recent_closes[last_price_low] < recent_closes[prev_price_low]
            macd_up = recent_macd[last_macd_low] > recent_macd[prev_macd_low]
            return price_down and macd_up
        return False
    except Exception as e:
        print(f"[{get_current_time_ms()}] 底背离检查错误: {str(e)}")
        traceback.print_exc()
        return False