
create schema props;

CREATE TABLE props.props (
    comment text NULL,
    c_ts timestamp(3) with time zone NOT NULL,
    last_modified_by text NOT NULL,
    lm_ts timestamp(3) with time zone NOT NULL,
    property_name text NOT NULL,
    property_value text NOT NULL,
    v bigint NOT NULL,
    PRIMARY KEY(property_name)
);


CREATE TABLE props.props_history (
    change_type text NOT NULL,
    comment text NULL,
    c_ts timestamp(3) with time zone NOT NULL,
    last_modified_by text NOT NULL,
    lm_ts timestamp(3) with time zone NOT NULL,
    property_name text NOT NULL,
    property_value text NOT NULL,
    v bigint NOT NULL,
    PRIMARY KEY(property_name, v)
);
