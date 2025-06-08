CREATE TABLE IF NOT EXISTS telegram_user (
    chat_id BIGINT PRIMARY KEY,
    username VARCHAR(255),
    token VARCHAR(255),
    account_id BIGINT,
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES account(id)
);

