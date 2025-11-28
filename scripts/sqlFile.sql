CREATE DATABASE candle_db;
\c candle_db;

CREATE EXTENSION IF NOT EXISTS timescaledb;

DROP TABLE IF EXISTS candles;

CREATE TABLE candles (
    symbol   TEXT NOT NULL,
    interval TEXT NOT NULL,
    time     TIMESTAMPTZ NOT NULL,
    open     DOUBLE PRECISION NOT NULL,
    high     DOUBLE PRECISION NOT NULL,
    low      DOUBLE PRECISION NOT NULL,
    close    DOUBLE PRECISION NOT NULL,
    volume   BIGINT NOT NULL,
    PRIMARY KEY(symbol, interval, time)
);

SELECT create_hypertable(
    'candles',
    'time',
    partitioning_column => 'symbol',
    number_partitions => 32,
    if_not_exists => TRUE
);