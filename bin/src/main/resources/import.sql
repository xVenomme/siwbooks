INSERT INTO credentials (id, username, password, user_id) VALUES (1, 'admin', '$2a$10$U.xhiVP.Du/xlv32XVsLi.ile25rBoFZ33Ey9dNkC1KjUXotKNvZu', null);
INSERT INTO users (id, username, name, surname, role, credentials_id) VALUES (1, 'admin', 'Admin', 'Admin', 'ADMIN', null);
UPDATE credentials SET user_id = 1 WHERE id = 1;
UPDATE users SET credentials_id = 1 WHERE id = 1;

INSERT INTO credentials (id, username, password, user_id) VALUES (2, 'user', '$2a$10$U.xhiVP.Du/xlv32XVsLi.ile25rBoFZ33Ey9dNkC1KjUXotKNvZu', null);
INSERT INTO users (id, username, name, surname, role, credentials_id) VALUES (2, 'user', 'user', 'user', 'USER', null);
UPDATE credentials SET user_id = 2 WHERE id = 2;
UPDATE users SET credentials_id = 2 WHERE id = 2;

INSERT INTO credentials (id, username, password, user_id) VALUES (3, 'jane.doe', '$2a$10$U.xhiVP.Du/xlv32XVsLi.ile25rBoFZ33Ey9dNkC1KjUXotKNvZu', null);
INSERT INTO users (id, username, name, surname, role, credentials_id) VALUES (3, 'jane.doe', 'Jane', 'Doe', 'USER', null);
UPDATE credentials SET user_id = 3 WHERE id = 3;
UPDATE users SET credentials_id = 3 WHERE id = 3;

INSERT INTO credentials (id, username, password, user_id) VALUES (4, 'john.smith', '$2a$10$U.xhiVP.Du/xlv32XVsLi.ile25rBoFZ33Ey9dNkC1KjUXotKNvZu', null);
INSERT INTO users (id, username, name, surname, role, credentials_id) VALUES (4, 'john.smith', 'John', 'Smith', 'USER', null);
UPDATE credentials SET user_id = 4 WHERE id = 4;
UPDATE users SET credentials_id = 4 WHERE id = 4;


-- Autori (J.K. Rowling, Dante, King)
INSERT INTO author (id, name, surname, birth_date, death_date, nationality, photo) VALUES (1, 'J.K.', 'Rowling', '1965-07-31', NULL, 'Britannica', 'JkRowling.jpg');
INSERT INTO author (id, name, surname, birth_date, death_date, nationality, photo) VALUES (2, 'Dante', 'Alighieri', '1265-06-01', '1321-09-14', 'Italiana', 'Dante.jpg');
INSERT INTO author (id, name, surname, birth_date, death_date, nationality, photo) VALUES (3, 'Stephen', 'King', '1947-09-21', NULL, 'Americana', 'StephenKing.jpg');

-- Libri (id spostati a 1001..1006)
INSERT INTO book (id, title, publication_year) VALUES (1001, 'Harry Potter e la Pietra Filosofale', 1997);
INSERT INTO book (id, title, publication_year) VALUES (1002, 'Harry Potter e la Camera dei Segreti', 1998);
INSERT INTO book (id, title, publication_year) VALUES (1003, 'Divina Commedia', 1321);
INSERT INTO book (id, title, publication_year) VALUES (1004, 'Inferno', 1320);
INSERT INTO book (id, title, publication_year) VALUES (1005, 'Shining', 1977);
INSERT INTO book (id, title, publication_year) VALUES (1006, 'IT', 1986);

-- book_authors (aggiornati gli id libro)
INSERT INTO book_authors (book_id, authors_id) VALUES (1001, 1); -- Rowling
INSERT INTO book_authors (book_id, authors_id) VALUES (1002, 1);
INSERT INTO book_authors (book_id, authors_id) VALUES (1003, 2); -- Dante
INSERT INTO book_authors (book_id, authors_id) VALUES (1004, 2);
INSERT INTO book_authors (book_id, authors_id) VALUES (1005, 3); -- King
INSERT INTO book_authors (book_id, authors_id) VALUES (1006, 3);

-- Immagini libri (aggiornati gli id libro)
INSERT INTO book_images (book_id, images) VALUES (1001, 'hp1.jpg');
INSERT INTO book_images (book_id, images) VALUES (1002, 'hp2.jpg');
INSERT INTO book_images (book_id, images) VALUES (1003, 'divina.jpg');
INSERT INTO book_images (book_id, images) VALUES (1004, 'inferno.jpg');
INSERT INTO book_images (book_id, images) VALUES (1005, 'shining.jpg');
INSERT INTO book_images (book_id, images) VALUES (1006, 'it.jpg');

INSERT INTO book_images (book_id, images) VALUES (1001, 'hp1.jpg');
INSERT INTO book_images (book_id, images) VALUES (1002, 'hp2.jpg');  
INSERT INTO book_images (book_id, images) VALUES (1003, 'divina.jpg'); 
INSERT INTO book_images (book_id, images) VALUES (1004, 'inferno.jpg');  
INSERT INTO book_images (book_id, images) VALUES (1005, 'shining.jpg'); 
INSERT INTO book_images (book_id, images) VALUES (1006, 'it.jpg');  

SELECT setval('author_id_seq', (SELECT MAX(id) FROM author));

