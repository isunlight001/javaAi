from fastapi import FastAPI, Request, Depends, BackgroundTasks
from fastapi.responses import HTMLResponse, FileResponse
from fastapi.staticfiles import StaticFiles
from sqlalchemy import create_engine, Column, Integer, String, Float
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from pydantic import BaseModel
import os
from config import SQLALCHEMY_DATABASE_URL
from stock import selector, risk, mail, data_service
import asyncio

app = FastAPI()

# 静态文件
app.mount("/static", StaticFiles(directory="static"), name="static")

# 数据库
Base = declarative_base()
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

class Position(Base):
    __tablename__ = "positions"
    id = Column(Integer, primary_key=True, index=True)
    code = Column(String, index=True)
    buy_price = Column(Float)
    buy_day_low = Column(Float)

Base.metadata.create_all(bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

class PositionIn(BaseModel):
    code: str
    buyPrice: float
    buyDayLow: float

@app.get("/", response_class=HTMLResponse)
def index():
    return FileResponse("static/index.html")

@app.get("/api/stock/select")
def api_select_stocks(bg: BackgroundTasks):
    results = selector.select_stocks()
    # 邮件提醒
    if results:
        bg.add_task(asyncio.run, mail.send_stock_pick_result(results))
    return results

@app.post("/api/stock/risk/check")
def api_check_risk(pos: PositionIn, bg: BackgroundTasks):
    result = risk.check_risk(pos.code, pos.buyPrice, pos.buyDayLow)
    # 邮件提醒
    if result.get('action') in ['止盈', '止损']:
        bg.add_task(asyncio.run, mail.send_risk_alert(pos.code, result.get('reason', '')))
    return result

@app.post("/api/stock/risk/position")
def add_position(pos: PositionIn, db: Session = Depends(get_db)):
    db_pos = Position(code=pos.code, buy_price=pos.buyPrice, buy_day_low=pos.buyDayLow)
    db.add(db_pos)
    db.commit()
    db.refresh(db_pos)
    return {"success": True, "id": db_pos.id}

@app.get("/api/stock/risk/positions")
def get_positions(db: Session = Depends(get_db)):
    positions = db.query(Position).all()
    return {"success": True, "positions": [
        {"code": p.code, "buyPrice": p.buy_price, "buyDayLow": p.buy_day_low} for p in positions
    ]} 