--
-- Database schema
--
DROP TABLE IF EXISTS messages;
CREATE TABLE messages (
    id bigint,
    content character varying(100),
    timestamp timestamp
);
