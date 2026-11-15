CREATE TABLE maia.cached_big_decimal (
    created_timestamp timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_big_decimal numeric(19,4) NOT NULL,
    some_big_decimal_nullable numeric(19,4) NULL,
    some_big_decimal_scaled numeric(10,2) NOT NULL,
    PRIMARY KEY(id)
);
