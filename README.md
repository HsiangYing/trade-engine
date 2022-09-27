- [trade-engine](#trade-engine)
  * [需求說明](#需求說明)
  * [系統架構](#系統架構)
  * [系統設計說明](#系統設計說明)
    + [操盤手提交委託單](#操盤手提交委託單)
    + [系統撮合委託單](#系統撮合委託單)
    + [儲存撮合成功的委託單為一筆交易至DB](#儲存撮合成功的委託單為一筆交易至db)
  * [啟動方式](#啟動方式)
  * [相關文件](#相關文件)

# trade-engine

## 需求說明
Imagine you have a trade engine that accepts orders via the protocol (or triggering) you defined. 
An order request at least has this information (buy or sell, quantity, market price or limit price).
The engine matches buy and sell orders that have the same price. Orders have the same price determined by their timestamp (FIFO). 
Pending orders queue up in your system until they are filled or killed. 
Your system will have multiple traders executing orders at the same time.


## 系統架構
[如果看不清楚可以點這個連結](https://whimsical.com/trade-engine-system-architechure-55jZhRdv8FmJJ3oqFucy82)
![image](https://user-images.githubusercontent.com/104806006/192540996-9aa56d9c-5cc1-4f46-8354-9a9b0878be99.png)

## 系統設計說明

### 操盤手提交委託單
<img width="754" alt="image" src="https://user-images.githubusercontent.com/104806006/192535777-8dc9d09c-5fbc-4134-9dc8-aaf638681f02.png">

1. 操盤手可以同時下單
   - springboot embedded tomcat 初始設定 server.tomcat.max-threads=200, 該設定可以決定最多可以同時處理多少個 http request
2. 當 trade engine 接收到 request 後, 便會將委託單透過儲存至 **redis stream**
   - 類似 kafka 可以當作是一個 message queue, 但是是以 memory 存取, 因此速度可能相對會比 kafka 快
   - 後續會被撮合引擎消費, 能達到 FIFO
3. issue: https://github.com/HsiangYing/trade-engine/issues/2



### 系統撮合委託單
![image](https://user-images.githubusercontent.com/104806006/192538333-c225d30f-1d4c-4abe-9810-b87ffc4b6396.png)
   
1. 從 **redis stream** 消費委託單, 並比對該委託單與**委託簿**中是否有可以撮合的委託單
2. 撮合邏輯
   - 規則: 市價單優先於限價單
   - [這裡可以看比較大一點的圖](https://whimsical.com/trade-engine-28temnR9yP5bnVryCoGtWS)
   ![image](https://user-images.githubusercontent.com/104806006/192541403-6dd4ddc9-b1c1-4d05-ae4f-72bd446b8d7f.png)

3. 如何達到 Thread safe?
   - 使用 **single thread** 消費(監聽)**redis stream**
   - 指定負責撮合的 method 為 **synchronized**, 也就是只有一個thread 可以執行該method
     - 但如果未來撮合系統 scale out, 則必須使用其他方式處理, e.g.: shedlock
   - 消費(監聽) **redis stream** 時如果遇到 exception, 必須處理他並繼續消費！
3. issue: https://github.com/HsiangYing/trade-engine/issues/6

### 儲存撮合成功的委託單為一筆交易至DB
![image](https://user-images.githubusercontent.com/104806006/192539110-550430f8-5ca9-425a-95b2-c2525ff6e15c.png)

1. 開一個 ThreadPool 負責將交易單儲存至DB, 不與上述撮合引擎使用同一個 thread, 以提升效率
2. issue: https://github.com/HsiangYing/trade-engine/issues/9

## 啟動方式
- 環境要求
  - docker
- 啟動指令

```docker-compose up -d```

## 相關文件
- API 文件：
  - 若Application啟動成功, 則可以至 swagger 使用API `http://yourDockerHost:8080/swagger-ui/index.html`





