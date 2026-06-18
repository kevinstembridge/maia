
insert into maia.user_group (
    authorities,
    created_timestamp_utc,
    description,
    id,
    name,
    system_managed,
    type_discriminator,
    version
) values (
    '{READ}',
    current_timestamp,
    'Read-only access group',
    gen_random_uuid(),
    'Read-Only',
    false,
    'UG',
    1
);


insert into maia.user_group (
    authorities,
    created_timestamp_utc,
    description,
    id,
    name,
    system_managed,
    type_discriminator,
    version
) values (
    '{READ, WRITE}',
    current_timestamp,
    'Read-write access group',
    gen_random_uuid(),
    'Read-Write',
    false,
    'UG',
    1
);
