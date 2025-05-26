-- Вставка аккаунтов с проверкой на существование
INSERT INTO account (email, password, role, state, photo_uid)
SELECT 'student@example.com', '$2a$10$Jmzm0cm58VLm0yo/dYdcKu6Gqm6UJzZqeBieIH/DkGgZ3jcAQHw6a', 'STUDENT', 'CONFIRMED', 'ProfilePhoto%2Favatar1.png'
WHERE NOT EXISTS (SELECT 1 FROM account WHERE email = 'student@example.com');

INSERT INTO account (email, password, role, state, photo_uid)
SELECT 'not_confirmed@example.com', '$2a$10$Jmzm0cm58VLm0yo/dYdcKu6Gqm6UJzZqeBieIH/DkGgZ3jcAQHw6a', 'STUDENT', 'NOT_CONFIRMED', 'ProfilePhoto%2Favatar3.jpg'
WHERE NOT EXISTS (SELECT 1 FROM account WHERE email = 'not_confirmed@example.com');


INSERT INTO account (email, password, role, state, photo_uid)
SELECT 'organizer@example.com', '$2a$10$Jmzm0cm58VLm0yo/dYdcKu6Gqm6UJzZqeBieIH/DkGgZ3jcAQHw6a', 'ORGANIZER', 'CONFIRMED', 'ProfilePhoto%2Favatar3.jpg'
WHERE NOT EXISTS (SELECT 1 FROM account WHERE email = 'organizer@example.com');

INSERT INTO account (email, password, role, state, photo_uid)
SELECT 'banned@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MH/qj6W1mA6bYSUBBtFQ7Ug2QbSbaRe', 'GUEST', 'BANNED', 'ProfilePhoto%2Favatar1.png'
WHERE NOT EXISTS (SELECT 1 FROM account WHERE email = 'banned@example.com');

-- Вставка событий с проверкой на существование
INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Научная конференция', 'Конференция по искусственному интеллекту', '2023-12-15 10:00:00', 'Москва, Университет ИТ', 150, 1, 'SCIENCE', 'events/images/event1.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Научная конференция');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Футбольный матч', 'Финал чемпионата города', '2023-11-20 18:30:00', 'Казань, Стадион «Ак Барс»', 1000, 2, 'SPORT', 'events/images/event2.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Футбольный матч');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Выставка современного искусства', 'Работы молодых художников', '2024-01-10 12:00:00', 'Санкт-Петербург, Эрмитаж', 200, 3, 'CULTURE', 'events/images/event1.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Выставка современного искусства');

-- Продолжаем для всех остальных событий аналогичным образом
INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Лекция по биохимии', 'Новые открытия в медицине', '2023-12-01 14:00:00', 'Новосибирск, НГУ', 30, 4, 'SCIENCE', 'events/images/event2.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Лекция по биохимии');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Хакатон по разработке', 'Соревнования для программистов', '2024-02-05 09:00:00', 'Иннополис', 300, 4, 'SCIENCE', 'events/images/event1.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Хакатон по разработке');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Беговой марафон', 'Городской марафон на 10 км', '2023-11-25 08:00:00', 'Сочи, Набережная', 1000, 2, 'SPORT', 'events/images/event2.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Беговой марафон');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Книжная ярмарка', 'Продажа редких книг', '2024-03-15 11:00:00', 'Екатеринбург, Дом Книги', 140, 3, 'CULTURE', 'events/images/event1.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Книжная ярмарка');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Семинар по робототехнике', 'Практические мастер-классы', '2023-12-10 16:30:00', 'Томск, ТПУ', 50, 3, 'SCIENCE', 'events/images/event2.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Семинар по робототехнике');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Баскетбольный турнир', 'Школьные команды', '2024-04-20 17:00:00', 'Краснодар, Спорткомплекс', 200, 1, 'SPORT', 'events/images/event1.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Баскетбольный турнир');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Опера "Евгений Онегин"', 'Классическая постановка', '2024-05-01 19:00:00', 'Москва, Большой театр', 500, 2, 'CULTURE', 'events/images/event2.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Опера "Евгений Онегин"');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Лекция по астрофизике', 'Исследование черных дыр', '2024-06-12 15:45:00', 'Самара, СГАУ', 60, 3, 'SCIENCE', 'events/images/event1.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Лекция по астрофизике');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Велокросс', 'Гонки по пересеченной местности', '2024-07-07 10:00:00', 'Уфа, Парк Победы', 200, 1, 'SPORT', 'events/images/event2.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Велокросс');

INSERT INTO event (title, description, date, location, participant_limit, organizer_id, category, image_uid)
SELECT 'Фестиваль уличного искусства', 'Граффити и перформансы', '2024-08-22 13:00:00', 'Нижний Новгород, Кремль', 1500, 2, 'CULTURE', 'events/images/event1.jpg'
    WHERE NOT EXISTS (SELECT 1 FROM event WHERE title = 'Фестиваль уличного искусства');

INSERT INTO participation (event_id, user_id, qr_code_uid)
SELECT e.id, a.id, 'subscriptions/qrcodes/' || e.id || '.png'
FROM event e, account a
WHERE
    (e.title = 'Научная конференция' AND a.email = 'organizer@example.com') OR
    (e.title = 'Футбольный матч' AND a.email = 'organizer@example.com') OR
    (e.title = 'Выставка современного искусства' AND a.email = 'organizer@example.com')
        AND NOT EXISTS (
        SELECT 1 FROM participation
        WHERE event_id = e.id AND user_id = a.id
    );
