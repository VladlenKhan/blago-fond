-- Схема базы данных "Благотворительный фонд"
-- Две связанные таблицы: donors (доноры) и donations (пожертвования).

DROP TABLE IF EXISTS donations;
DROP TABLE IF EXISTS donors;

CREATE TABLE donors (
    id            SERIAL PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20)  NOT NULL,
    city          VARCHAR(50)  NOT NULL,
    registered_at DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE donations (
    id         SERIAL PRIMARY KEY,
    donor_id   INTEGER        NOT NULL REFERENCES donors (id) ON DELETE CASCADE,
    purpose    VARCHAR(150)   NOT NULL,
    category   VARCHAR(20)    NOT NULL CHECK (category IN ('MEDICINE', 'EDUCATION', 'CHILDREN', 'ANIMALS', 'ECOLOGY')),
    status     VARCHAR(20)    NOT NULL CHECK (status IN ('NEW', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    amount     NUMERIC(12, 2) NOT NULL CHECK (amount > 0 AND amount <= 1000000),
    created_at TIMESTAMP      NOT NULL DEFAULT now()
);

-- Тестовые данные: 6 доноров
INSERT INTO donors (full_name, email, phone, city, registered_at) VALUES
    ('Иванов Иван Иванович',    'ivanov@mail.ru',   '+7-900-111-22-33', 'Москва',          '2024-01-15'),
    ('Петрова Анна Сергеевна',  'petrova@mail.ru',  '+7-900-222-33-44', 'Санкт-Петербург', '2024-02-20'),
    ('Сидоров Пётр Олегович',   'sidorov@gmail.com','+7-900-333-44-55', 'Казань',          '2024-03-05'),
    ('Кузнецова Мария Ильина',  'kuznecova@bk.ru',  '+7-900-444-55-66', 'Москва',          '2024-05-11'),
    ('Смирнов Алексей Петрович','smirnov@yandex.ru','+7-900-555-66-77', 'Новосибирск',     '2024-07-30'),
    ('Орлова Елена Викторовна', 'orlova@mail.ru',   '+7-900-666-77-88', 'Екатеринбург',    '2024-09-02');

-- Тестовые данные: 12 пожертвований с разными статусами и направлениями
INSERT INTO donations (donor_id, purpose, category, status, amount, created_at) VALUES
    (1, 'Лечение Маши Ивановой',       'MEDICINE',  'COMPLETED', 50000.00, '2025-01-10 10:30:00'),
    (1, 'Учебники для интерната №5',   'EDUCATION', 'CONFIRMED', 15000.00, '2025-02-14 12:00:00'),
    (2, 'Приют для собак "Верный"',    'ANIMALS',   'NEW',        7500.50, '2025-02-28 09:15:00'),
    (2, 'Реабилитация после операции', 'MEDICINE',  'COMPLETED', 120000.00,'2025-03-03 16:45:00'),
    (3, 'Посадка деревьев в парке',    'ECOLOGY',   'CANCELLED',  3000.00, '2025-03-19 11:20:00'),
    (3, 'Новогодние подарки детям',    'CHILDREN',  'COMPLETED', 25000.00, '2025-04-01 14:00:00'),
    (4, 'Оборудование для школы',      'EDUCATION', 'CONFIRMED', 80000.00, '2025-04-22 10:05:00'),
    (4, 'Корм для приюта кошек',       'ANIMALS',   'NEW',        4200.00, '2025-05-08 18:30:00'),
    (5, 'Операция ребёнку',            'MEDICINE',  'CONFIRMED', 250000.00,'2025-05-25 08:40:00'),
    (5, 'Очистка берега реки',         'ECOLOGY',   'NEW',       12000.00, '2025-06-11 13:10:00'),
    (6, 'Летний лагерь для сирот',     'CHILDREN',  'COMPLETED', 95000.00, '2025-06-30 17:25:00'),
    (6, 'Стипендия студенту',          'EDUCATION', 'CANCELLED', 30000.00, '2025-07-14 15:50:00');
