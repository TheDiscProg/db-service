CREATE DATABASE IF NOT EXISTS shareprice
    CHARACTER SET = utf8;

USE shareprice;

CREATE TABLE IF NOT EXISTS customer (
    id BINARY(16) DEFAULT (UUID_TO_BIN(UUID())),
    forename VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    PRMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS user(
    id BINARY(16) DEFAULT (UUID_TO_BIN(UUID())),
    customer_id BINARY(16),
    username VARCHAR(100),
    password VARCHAR(128).
    PRIMARY KEY(id),
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASECASE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;