

CREATE TABLE toggles.feature_toggle (
    activation_strategies jsonb NOT NULL,
    attributes jsonb NULL,
    comment text NULL,
    contact_person text NULL,
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    description text NULL,
    enabled boolean NOT NULL,
    feature_name text NOT NULL,
    info_link text NULL,
    last_modified_by_name text NOT NULL,
    last_modified_timestamp_utc timestamp(3) with time zone NOT NULL,
    review_date date NULL,
    ticket_key text NULL,
    version bigint NOT NULL,
    PRIMARY KEY(feature_name)
);


CREATE TABLE toggles.feature_toggle_history (
    activation_strategies jsonb NOT NULL,
    attributes jsonb NULL,
    change_type text NOT NULL,
    comment text NULL,
    contact_person text NULL,
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    description text NULL,
    enabled boolean NOT NULL,
    feature_name text NOT NULL,
    info_link text NULL,
    last_modified_by_name text NOT NULL,
    last_modified_timestamp_utc timestamp(3) with time zone NOT NULL,
    review_date date NULL,
    ticket_key text NULL,
    version bigint NOT NULL,
    PRIMARY KEY(feature_name, version)
);
