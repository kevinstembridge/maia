
CREATE TABLE testing.nullable_fields (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_boolean boolean NULL,
    some_boolean_type boolean NULL,
    some_enum text NULL,
    some_instant timestamp(3) with time zone NULL,
    some_int integer NULL,
    some_int_type integer NULL,
    some_local_date date NULL,
    some_long_type bigint NULL,
    some_period text NULL,
    some_provided_boolean_type boolean NULL,
    some_provided_int_type integer NULL,
    some_provided_long_type bigint NULL,
    some_provided_string_type text NULL,
    some_string text NULL,
    some_string_type text NULL,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX nullable_fields_some_string_uidx ON testing.nullable_fields(some_string);


CREATE TABLE testing.effective_timestamp (
    c_ts timestamp(3) with time zone NOT NULL,
    effective_from timestamp(3) with time zone NULL,
    effective_to timestamp(3) with time zone NULL,
    id uuid NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);
CREATE INDEX effective_timestamp_some_string_idx ON testing.effective_timestamp(some_string);


CREATE TABLE testing.party (
    type_discriminator text not null,
    c_ts timestamp(3) with time zone not null,
    email_address text not null,
    encrypted_password text null,
    first_name text null,
    id uuid primary key not null,
    lm_ts timestamp(3) with time zone not null,
    last_name text null,
    org_name text null,
    some_strings text[] null
);


CREATE OR REPLACE VIEW testing.v_party as
    select
        p.*,
        CASE p.type_discriminator
            WHEN 'ORG' then p.org_name
            WHEN 'PE' then p.first_name || ' ' || p.last_name
            WHEN 'USR' then p.first_name || ' ' || p.last_name
        END as display_name
    from testing.party p;

CREATE TABLE testing.user_group (
    type_discriminator text not null,
    authorities text[] not null,
    c_ts timestamp(3) with time zone not null,
    description text not null,
    id uuid primary key not null,
    name text not null,
    org_id uuid null references testing.party(id),
    system_managed boolean not null,
    v bigint not null
);


CREATE TABLE testing.user_group_history (
    type_discriminator text not null,
    authorities text[] not null,
    change_type text null,
    c_ts timestamp(3) with time zone not null,
    description text not null,
    entity_id uuid null,
    id uuid primary key not null,
    name text not null,
    org_id uuid null references testing.party(id),
    system_managed boolean not null,
    v bigint not null
);
CREATE UNIQUE INDEX user_group_entity_id_v_uidx ON testing.user_group_history(entity_id, v, type_discriminator);
CREATE INDEX user_group_entity_id_idx ON testing.user_group_history(entity_id, type_discriminator);


CREATE TABLE testing.org_user_group_membership (
    c_ts timestamp(3) with time zone not null,
    id uuid primary key not null,
    org_user_group_id uuid not null,
    user_id uuid not null,
    v bigint not null
);
CREATE UNIQUE INDEX org_user_group_membership_org_user_group_id_user_id_uidx ON testing.org_user_group_membership(org_user_group_id, user_id);
CREATE INDEX org_user_group_membership_user_id_idx ON testing.org_user_group_membership(user_id);


CREATE TABLE testing.org_user_group_membership_history (
    change_type text null,
    c_ts timestamp(3) with time zone not null,
    entity_id uuid null,
    id uuid primary key not null,
    org_user_group_id uuid not null,
    user_id uuid not null,
    v bigint not null
);
CREATE UNIQUE INDEX org_user_group_membership_entity_id_v_uidx ON testing.org_user_group_membership_history(entity_id, v);
CREATE INDEX org_user_group_membership_entity_id_idx ON testing.org_user_group_membership_history(entity_id);
CREATE INDEX hist_org_user_group_membership_org_user_group_id_user_id_uidx ON testing.org_user_group_membership_history(org_user_group_id, user_id);
CREATE INDEX hist_org_user_group_membership_user_id_idx ON testing.org_user_group_membership_history(user_id);


CREATE TABLE testing.simple (
    created_by_id uuid NOT NULL REFERENCES testing.party(id),
    created_by_name text NOT NULL,
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    lm_by_id uuid NOT NULL REFERENCES testing.party(id),
    lm_by_name text NOT NULL,
    lm_ts timestamp(3) with time zone NOT NULL,
    some_boolean boolean NOT NULL,
    some_boolean_nullable boolean NULL,
    some_boolean_type boolean NOT NULL,
    some_boolean_type_nullable boolean NULL,
    some_boolean_type_provided boolean NOT NULL,
    some_boolean_type_provided_nullable boolean NULL,
    some_dto jsonb NOT NULL,
    some_dto_nullable jsonb NULL,
    some_enum text NOT NULL,
    some_enum_nullable text NULL,
    some_instant timestamp(3) with time zone NOT NULL,
    some_instant_modifiable timestamp(3) with time zone NOT NULL,
    some_instant_modifiable_nullable timestamp(3) with time zone NULL,
    some_instant_nullable timestamp(3) with time zone NULL,
    some_int integer NOT NULL,
    some_int_modifiable integer NOT NULL,
    some_int_nullable integer NULL,
    some_int_type integer NOT NULL,
    some_int_type_nullable integer NULL,
    some_int_type_provided integer NOT NULL,
    some_int_type_provided_nullable integer NULL,
    some_list_of_enums text[] NOT NULL,
    some_list_of_instants timestamp(3) with time zone[] NOT NULL,
    some_list_of_local_dates date[] NOT NULL,
    some_list_of_periods text[] NOT NULL,
    some_list_of_string_types text[] NOT NULL,
    some_list_of_strings text[] NOT NULL,
    some_local_date_modifiable date NOT NULL,
    some_long_type bigint NOT NULL,
    some_long_type_nullable bigint NULL,
    some_long_type_provided bigint NOT NULL,
    some_long_type_provided_nullable bigint NULL,
    some_map_of_string_to_integer jsonb NOT NULL,
    some_map_of_string_type_to_string_type jsonb NOT NULL,
    some_period_modifiable text NOT NULL,
    some_period_nullable text NULL,
    some_provided_string_type text NOT NULL,
    some_provided_string_type_nullable text NULL,
    some_string text NOT NULL,
    some_string_modifiable text NOT NULL,
    some_string_nullable text NULL,
    some_string_type text NOT NULL,
    some_string_type_nullable text NULL,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX simple_some_int_type_uidx ON testing.simple(some_int_type);
CREATE UNIQUE INDEX simple_some_long_type_uidx ON testing.simple(some_long_type);
CREATE UNIQUE INDEX simple_some_string_uidx ON testing.simple(some_string);
CREATE UNIQUE INDEX simple_some_string_nullable_uidx ON testing.simple(some_string_nullable);
CREATE UNIQUE INDEX simple_some_string_type_uidx ON testing.simple(some_string_type);
CREATE INDEX simple_some_boolean_some_string_modifiable_idx ON testing.simple(some_boolean, some_string_modifiable);


CREATE TABLE testing.ttl (
    created_at timestamp(3) with time zone NOT NULL,
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    PRIMARY KEY(id)
);
CREATE INDEX ttl_c_ts_idx ON testing.ttl(c_ts);


CREATE TABLE testing.history_sample (
    created_by_id uuid NOT NULL REFERENCES testing.party(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    lm_by_id uuid NOT NULL REFERENCES testing.party(id),
    lm_ts timestamp(3) with time zone NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    v bigint NOT NULL,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX history_sample_some_string_uidx ON testing.history_sample(some_string);


CREATE TABLE testing.history_sample_history (
    change_type text NOT NULL,
    created_by_id uuid NOT NULL REFERENCES testing.party(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    lm_by_id uuid NOT NULL REFERENCES testing.party(id),
    lm_ts timestamp(3) with time zone NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    v bigint NOT NULL,
    PRIMARY KEY(id, v)
);
CREATE UNIQUE INDEX history_sample_id_v_uidx ON testing.history_sample_history(id, v);
CREATE INDEX hist_history_sample_some_string_uidx ON testing.history_sample_history(some_string);


CREATE TABLE testing.super (
    type_discriminator text not null,
    created_by_id uuid NOT NULL REFERENCES testing.party(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    lm_by_id uuid NOT NULL REFERENCES testing.party(id),
    lm_ts timestamp(3) with time zone NOT NULL,
    some_int integer NULL,
    some_string text NULL,
    some_unique_string text NULL,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX sub_two_some_unique_string_uidx ON testing.super(some_unique_string, type_discriminator);


CREATE TABLE testing.history_super (
    type_discriminator text not null,
    created_by_id uuid NOT NULL REFERENCES testing.party(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    lm_by_id uuid NOT NULL REFERENCES testing.party(id),
    lm_ts timestamp(3) with time zone NOT NULL,
    some_int integer NULL,
    some_string text NULL,
    v bigint NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.history_super_history (
    type_discriminator text not null,
    change_type text NOT NULL,
    created_by_id uuid NOT NULL REFERENCES testing.party(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    lm_by_id uuid NOT NULL REFERENCES testing.party(id),
    lm_ts timestamp(3) with time zone NOT NULL,
    some_int integer NULL,
    some_string text NULL,
    v bigint NOT NULL,
    PRIMARY KEY(id, v)
);
CREATE UNIQUE INDEX history_super_id_v_uidx ON testing.history_super_history(id, v, type_discriminator);


CREATE TABLE testing.some_versioned (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    v bigint NOT NULL,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX some_versioned_some_int_uidx ON testing.some_versioned(some_int);


CREATE TABLE testing.with_optional_index_field (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_optional_string1 text NULL,
    some_optional_string2 text NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);
CREATE INDEX with_optional_index_field_some_optional_string1_idx ON testing.with_optional_index_field(some_optional_string1);
CREATE INDEX with_optional_index_field_some_optional_string2_some_string_idx ON testing.with_optional_index_field(some_optional_string2, some_string);


CREATE TABLE testing.very_simple (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.foreign_key_parent (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.foreign_key_child (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    parent_id uuid NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.alpha (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.bravo (
    alpha_id uuid NOT NULL REFERENCES testing.alpha(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.charlie (
    bravo_id uuid NOT NULL REFERENCES testing.bravo(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.alpha_ag_grid (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.bravo_ag_grid (
    alpha_id uuid NOT NULL REFERENCES testing.alpha_ag_grid(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.charlie_ag_grid (
    bravo_id uuid NOT NULL REFERENCES testing.bravo_ag_grid(id),
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.left (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.right (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_int integer NOT NULL,
    some_string text NOT NULL,
    PRIMARY KEY(id)
);


CREATE TABLE testing.many_to_many_join (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    lm_ts timestamp(3) with time zone NOT NULL,
    left_id uuid NOT NULL REFERENCES testing.left(id),
    right_id uuid NOT NULL REFERENCES testing.right(id),
    PRIMARY KEY(id)
);


CREATE TABLE testing.unmodifiable (
    c_ts timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    some_unique_int integer NOT NULL,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX unmodifiable_some_unique_int_uidx ON testing.unmodifiable(some_unique_int);
