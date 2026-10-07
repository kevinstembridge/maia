
insert into maia.user_group (
    authorities,
    created_timestamp,
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
    created_timestamp,
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

insert into maia.user_group (
    authorities,
    created_timestamp,
    description,
    id,
    name,
    system_managed,
    type_discriminator,
    version
) values (
    '{READ, WRITE, MAIA_PROPS_READ, MAIA_PROPS_WRITE, MAIA_TOGGLES_READ, MAIA_TOGGLES_WRITE, MAIA_ELASTICSEARCH_SYS_OPS_READ, MAIA_ELASTICSEARCH_SYS_OPS_WRITE, MAIA_JOB_READ, MAIA_JOB_WRITE}',
    current_timestamp,
    'System operations access group',
    gen_random_uuid(),
    'Sysops',
    false,
    'UG',
    1
);
