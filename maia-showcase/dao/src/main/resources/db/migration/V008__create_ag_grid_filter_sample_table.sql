CREATE TABLE maia.ag_grid_filter_sample (
    created_timestamp timestamp(3) with time zone NOT NULL,
    date_nullable date NULL,
    date_required date NOT NULL,
    decimal_nullable decimal NULL,
    decimal_required decimal NOT NULL,
    flag_nullable boolean NULL,
    flag_required boolean NOT NULL,
    id uuid NOT NULL,
    instant_nullable timestamp(3) with time zone NULL,
    instant_required timestamp(3) with time zone NOT NULL,
    int_nullable integer NULL,
    int_required integer NOT NULL,
    tags text[] NOT NULL,
    text_nullable text NULL,
    text_required text NOT NULL,
    PRIMARY KEY(id)
);
