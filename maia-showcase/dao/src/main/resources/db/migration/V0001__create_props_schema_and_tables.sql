create schema props;


CREATE TABLE props.props (
    comment text NULL,
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    last_modified_by_name text NOT NULL,
    last_modified_timestamp_utc timestamp(3) with time zone NOT NULL,
    property_name text NOT NULL,
    property_value text NOT NULL,
    version bigint NOT NULL,
    PRIMARY KEY(property_name)
);


CREATE TABLE props.props_history (
    change_type text NOT NULL,
    comment text NULL,
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    last_modified_by_name text NOT NULL,
    last_modified_timestamp_utc timestamp(3) with time zone NOT NULL,
    property_name text NOT NULL,
    property_value text NOT NULL,
    version bigint NOT NULL,
    PRIMARY KEY(property_name, version)
);
