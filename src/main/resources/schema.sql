CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'ORGANIZER', 'GUEST')),
    state VARCHAR(20) NOT NULL CHECK (state IN ('CONFIRMED', 'NOT_CONFIRMED', 'DELETED', 'BANNED')),
    photo_uid VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS event (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    date TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    participant_limit INTEGER,
    organizer_id BIGINT REFERENCES account(id),
    category VARCHAR(50) NOT NULL,
    image_uid VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS participation
(
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT REFERENCES event(id) NOT NULL,
    user_id BIGINT REFERENCES account(id) NOT NULL,
    qr_code_uid VARCHAR(255)
);

create table if not exists persistent_logins
(
    username varchar(64) not null,
    series varchar(64) not null,
    token varchar(64) not null,
    last_used varchar(64) not null
);

CREATE TABLE IF NOT EXISTS telegram_user (
    chat_id BIGINT PRIMARY KEY,
    username VARCHAR(255),
    token VARCHAR(255),
    account_id BIGINT,
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES account(id)
);