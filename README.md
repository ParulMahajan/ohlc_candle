# OHLC Candle Aggregation Service (Java + Spring Boot + TimescaleDB)

A high-performance, real-time OHLC (Open, High, Low, Close) candle aggregation service built with Spring Boot. This service processes sample ticker data and generates multiple candlesticks per symbol and intervals.

## Features

- **Real-time ticker Processing**: Processes incoming ticker data for multiple coins with low latency.
- **OHLC Candle Generation**: Generates in-memory OHLC candles(configurable /symbols,/intervals).
- **Store Historic data**: All Closed candles will be efficiently stored permanently in TimescaleDB.
- **RESTFUL API**: REST API for candle retrieval based on symbol, time range, and interval
- **Safe service shutdown**: On shutdown, ensures all in-memory candles are flushed to the database to prevent data loss.

### Components

1. **RandomTickGenerator**: Simulates incoming ticker data for testing purposes. Configurable for symbol list and interval. For production, it's better to use Kafka, with replay capability.
2. **CandleAggregationService**: Aggregates incoming ticker data in-memory into OHLC candles(/symbols,/intervals). Upon candle completion, it pushes the candle to a queue for persistence. For delayed tickers, it caches(configurable) recently completed candles in-memory.
3. **CandleFlushService**: Periodically flushes completed candles from the queue to TimescaleDB for permanent storage.
4. **HistoryController**: GET API, to get historical candles from TimescaleDB based on symbol, time range, and interval.
### Data Flow

```
Ticker Input (using RandomTickGenerator) 

          ↓       
          
CandleAggregationService(create candles in-memory and push to queue)

          ↓    
          
CandleFlushService (Poll from queue and batch save to TimescaleDb)
                                                                                                                        
                             
```

## Improvements:

1. Used Timescaledb hypertable, which is optimized for time-series data.
    SELECT create_hypertable(
       'candles',
       'time',
       partitioning_column => 'symbol',
       number_partitions => 32,
       if_not_exists => TRUE
       );

2. For ticker data ingestion in production, consider using Kafka with replay capability. ALso need to handle, on service startup, replay unprocessed tickers from Kafka to avoid data loss.
    Partition Kafka topics by symbol to ensure order within each symbol and scale horizontally.

3. Instead of calculating candles for all intervals, better calculate for 1s and just 1m and use Continous Aggregates in TimescaleDB to derive higher intervals like 5m, 15m, 1h, etc. This offloads computation to the database and simplifies the service.

4. Cache recently completed/fetched candles in redis for quick response.

