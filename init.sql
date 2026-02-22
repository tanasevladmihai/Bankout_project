DROP TABLE IF EXISTS user_discount_obtained CASCADE ;
DROP TABLE IF EXISTS discount_offer CASCADE ;
DROP TABLE IF EXISTS budget CASCADE;
DROP TABLE IF EXISTS transaction CASCADE ;
DROP TABLE IF EXISTS account CASCADE ;
DROP TABLE IF EXISTS store CASCADE ;
DROP TABLE IF EXISTS category CASCADE ;
DROP TABLE IF EXISTS users CASCADE;




CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       email VARCHAR(100) UNIQUE NOT NULL,
                       phone_number VARCHAR(20),
                       password_hash VARCHAR(255),
                       is_verified BOOLEAN DEFAULT FALSE,
                       status VARCHAR(20),
                       registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);




CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(50) NOT NULL
);




CREATE TABLE store (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       industry VARCHAR(50),
                       contact_email VARCHAR(100),
                       is_active BOOLEAN DEFAULT TRUE
);








CREATE TABLE account (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT REFERENCES users(id),
                         account_number VARCHAR(50) UNIQUE NOT NULL,
                         balance DECIMAL(15, 2) DEFAULT 0.00,
                         is_open BOOLEAN DEFAULT TRUE
);




CREATE TABLE transaction (
                             id BIGSERIAL PRIMARY KEY,
                             account_id BIGINT REFERENCES account(id),
                             category_id BIGINT REFERENCES category(id),
                             amount DECIMAL(15, 2) NOT NULL,
                             transaction_type VARCHAR(20),
                             transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             description TEXT,
                             store_name VARCHAR(100)
);




CREATE TABLE budget (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT REFERENCES users(id),
                        category_id BIGINT REFERENCES category(id),
                        limit_amount DECIMAL(15, 2),
                        period_type VARCHAR(20)
);




CREATE TABLE discount_offer (
                                id BIGSERIAL PRIMARY KEY,
                                store_id BIGINT REFERENCES store(id),
                                title VARCHAR(100),
                                description TEXT,
                                discount_type VARCHAR(20),
                                discount_value DECIMAL(15, 2),
                                expiry_date DATE
);


CREATE TABLE user_discount_obtained (
                                        id BIGSERIAL PRIMARY KEY,
                                        user_id BIGINT REFERENCES users(id),
                                        offer_id BIGINT REFERENCES discount_offer(id),
                                        transaction_id BIGINT REFERENCES transaction(id),
                                        usage_code VARCHAR(50) UNIQUE
);