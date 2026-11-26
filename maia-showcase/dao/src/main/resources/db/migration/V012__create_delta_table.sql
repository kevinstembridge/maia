CREATE TABLE maia.delta (
    alpha_id uuid REFERENCES maia.alpha(id),
    charlie_id uuid NOT NULL REFERENCES maia.charlie(id),
    created_timestamp timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);
