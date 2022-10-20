DROP TABLE IF EXISTS professor_student;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS professor;


CREATE TABLE student (
    id     SERIAL,
    name     VARCHAR(512) NOT NULL,
    birthdate VARCHAR(512) NOT NULL,
    credits     INTEGER NOT NULL,
    grade     FLOAT(2) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE professor (
    id     SERIAL,
    name VARCHAR(512) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE professor_student (
    professor_id INTEGER,
    student_id     INTEGER,
    PRIMARY KEY(professor_id,student_id)
);

ALTER TABLE professor_student ADD CONSTRAINT professor_student_fk1 FOREIGN KEY (professor_id) REFERENCES professor(id);
ALTER TABLE professor_student ADD CONSTRAINT professor_student_fk2 FOREIGN KEY (student_id) REFERENCES student(id);