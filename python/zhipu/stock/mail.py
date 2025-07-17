import aiosmtplib
from email.message import EmailMessage
from config import MAIL_USERNAME, MAIL_PASSWORD, MAIL_HOST, MAIL_PORT, MAIL_RECEIVER, MAIL_SSL

async def send_mail(subject, content, to=None):
    msg = EmailMessage()
    msg["From"] = MAIL_USERNAME
    msg["To"] = to or MAIL_RECEIVER
    msg["Subject"] = subject
    msg.set_content(content)
    await aiosmtplib.send(
        msg,
        hostname=MAIL_HOST,
        port=MAIL_PORT,
        username=MAIL_USERNAME,
        password=MAIL_PASSWORD,
        use_tls=MAIL_SSL
    )

async def send_stock_pick_result(results):
    subject = f"股票选股结果 - {len(results)}只股票"
    content = "今日选股结果：\n\n"
    for i, r in enumerate(results):
        stock = r['stock']
        content += f"{i+1}. {stock['name']}（{stock['ts_code']}） - {r['reason']}\n"
    await send_mail(subject, content)

async def send_risk_alert(ts_code, reason):
    subject = f"股票风险提醒 - {ts_code}"
    content = f"股票代码: {ts_code}\n风险原因: {reason}"
    await send_mail(subject, content) 