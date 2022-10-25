DROP TABLE IF EXISTS student_professor;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS professor;


CREATE TABLE student (
    id     SERIAL,
    name     VARCHAR(512) NOT NULL,
    birthdate VARCHAR(512) NOT NULL,
    credits     INTEGER NOT NULL,
    grade     INTEGER NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE professor (
    id     SERIAL,
    name VARCHAR(512) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE student_professor (
    id SERIAL,
    professor_id BIGINT,
    student_id     BIGINT,
    PRIMARY KEY(professor_id,student_id)
);

ALTER TABLE student_professor ADD CONSTRAINT student_professor_fk1 FOREIGN KEY (professor_id) REFERENCES professor(id);
ALTER TABLE student_professor ADD CONSTRAINT student_professor_fk2 FOREIGN KEY (student_id) REFERENCES student(id);