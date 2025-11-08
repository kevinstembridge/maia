

CREATE TABLE toggles.feature_toggle (
    activation_strategies jsonb not null,
    attributes jsonb not null,
    comment text null,
    contact_person text null,
    c_ts timestamp(3) with time zone not null,
    description text null,
    enabled boolean not null,
    feature_name text not null,
    id uuid primary key not null,
    info_link text null,
    last_modified_by text not null,
    lm_ts timestamp(3) with time zone not null,
    review_date date null,
    ticket_key text null,
    v bigint not null
);
CREATE UNIQUE INDEX feature_toggle_feature_name_uidx ON toggles.feature_toggle(feature_name);


CREATE TABLE toggles.feature_toggle_history (
    activation_strategies jsonb not null,
    attributes jsonb not null,
    change_type text null,
    comment text null,
    contact_person text null,
    c_ts timestamp(3) with time zone not null,
    description text null,
    enabled boolean not null,
    entity_id uuid null,
    feature_name text not null,
    id uuid primary key not null,
    info_link text null,
    last_modified_by text not null,
    lm_ts timestamp(3) with time zone not null,
    review_date date null,
    ticket_key text null,
    v bigint not null
);
CREATE UNIQUE INDEX feature_toggle_entity_id_v_uidx ON toggles.feature_toggle_history(entity_id, v);
CREATE INDEX feature_toggle_entity_id_idx ON toggles.feature_toggle_history(entity_id);
CREATE INDEX hist_feature_toggle_feature_name_uidx ON toggles.feature_toggle_history(feature_name);
