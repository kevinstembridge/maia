create schema props;


CREATE TABLE props.props (
    comment text NULL,
    created_timestamp timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    last_modified_by_name text NOT NULL,
    last_modified_timestamp timestamp(3) with time zone NOT NULL,
    property_name text NOT NULL,
    property_value text NOT NULL,
    review_date date NULL,
    version bigint NOT NULL,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX props_property_name_uidx ON props.props(property_name);


CREATE TABLE props.props_history (
    change_type text NOT NULL,
    comment text NULL,
    created_timestamp timestamp(3) with time zone NOT NULL,
    id uuid NOT NULL,
    last_modified_by_name text NOT NULL,
    last_modified_timestamp timestamp(3) with time zone NOT NULL,
    property_name text NOT NULL,
    property_value text NOT NULL,
    review_date date NULL,
    version bigint NOT NULL,
    PRIMARY KEY(id, version)
);
CREATE INDEX hist_props_property_name_idx ON props.props_history(property_name);
