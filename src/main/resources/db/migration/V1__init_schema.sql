CREATE TABLE points_of_interest (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200)     NOT NULL,
    category    VARCHAR(30)      NOT NULL,
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    address     VARCHAR(300),
    created_at  TIMESTAMP        NOT NULL DEFAULT now()
);

CREATE INDEX idx_poi_category ON points_of_interest (category);
CREATE INDEX idx_poi_coordinates ON points_of_interest (latitude, longitude);

CREATE TABLE reviews (
    id             BIGSERIAL PRIMARY KEY,
    poi_id         BIGINT           NOT NULL REFERENCES points_of_interest (id) ON DELETE CASCADE,
    reviewer_name  VARCHAR(150)     NOT NULL,
    rating         INTEGER          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment        VARCHAR(2000),
    created_at     TIMESTAMP        NOT NULL DEFAULT now()
);

CREATE INDEX idx_review_poi ON reviews (poi_id);
