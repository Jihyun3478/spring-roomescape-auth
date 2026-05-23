CREATE TABLE users
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name     VARCHAR(10)  NOT NULL,
    role     VARCHAR(10)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE shop
(
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(30)  NOT NULL,
    description VARCHAR(100) NOT NULL,
    thumbnail   VARCHAR(100) NOT NULL,
    shop_id     BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (shop_id) REFERENCES shop (id)
);

CREATE TABLE manager
(
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (shop_id) REFERENCES shop (id)
);

CREATE TABLE reservation
(
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    name     VARCHAR(10) NOT NULL,
    `date`   DATE        NOT NULL,
    time_id  BIGINT,
    theme_id BIGINT,
    user_id  BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
