
insert into maia.party (
    type_discriminator,
    authorities,
    created_by_id,
    created_timestamp_utc,
    encrypted_password,
    first_name,
    id,
    last_modified_by_id,
    last_modified_timestamp_utc,
    last_name,
    lifecycle_state,
    version
) values (
    'USR',
    '{}', -- authorities
    null, -- created_by_id
    current_timestamp,
    '{bcrypt}$2a$10$zI.pQy.gVMVRzsuuBxjc/.7/ZvtXzSWqGw6p4srdJi0FQ6YSn6E1S', -- d0uglas
    'Fyrst',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    null, -- last_modified_by_id
    current_timestamp,
    'Naime',
    'ACTIVE',
    1
);


insert into maia.email_address (
    created_timestamp_utc,
    created_by_id,
    email_address
) values (
    current_timestamp,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'user@maiaframework.org'
);


INSERT INTO maia.email_address_verification(
    created_by_id,
    created_timestamp_utc,
    effective_range,
    email_address,
    id,
    ip_address,
    last_modified_by_id,
    last_modified_timestamp_utc,
    version
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    tstzrange(null, null),
    'user@maiaframework.org',
    gen_random_uuid(),
    null,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    1
);


INSERT INTO maia.party_email_address(
    created_by_id,
    created_timestamp_utc,
    effective_range,
    email_address,
    id,
    is_primary_contact,
    last_modified_by_id,
    last_modified_timestamp_utc,
    party_id,
    purposes,
    version
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    tstzrange(current_timestamp, null),
    'user@maiaframework.org',
    gen_random_uuid(),
    true,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    (select id from maia.party where first_name = 'Fyrst' and last_name = 'Naime'),
    '{USER_LOGIN}',
    1
);


insert into maia.party (
    type_discriminator,
    authorities,
    created_by_id,
    created_timestamp_utc,
    encrypted_password,
    first_name,
    id,
    last_modified_by_id,
    last_modified_timestamp_utc,
    last_name,
    lifecycle_state,
    version
) values (
    'USR',
    '{}', --authorities
    null, -- created_by_id
    current_timestamp,
    '{bcrypt}$2a$10$zI.pQy.gVMVRzsuuBxjc/.7/ZvtXzSWqGw6p4srdJi0FQ6YSn6E1S', -- d0uglas
    'Job',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    null, -- last_modified_by_id
    current_timestamp,
    'Runna',
    'ACTIVE',
    1
);


insert into maia.party (
    type_discriminator,
    created_by_id,
    created_timestamp_utc,
    id,
    last_modified_timestamp_utc,
    version,
    org_name,
    authorities,
    first_name,
    last_name,
    lifecycle_state,
    encrypted_password
) values (
    'USR',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    gen_random_uuid(),
    current_timestamp,
    1,
    null,
    '{WRITE}',
    'Admin',
    'System',
    'ACTIVE',
    '{bcrypt}$2a$10$zI.pQy.gVMVRzsuuBxjc/.7/ZvtXzSWqGw6p4srdJi0FQ6YSn6E1S' -- d0uglas
);


insert into maia.email_address (
    created_timestamp_utc,
    created_by_id,
    email_address
) values (
    current_timestamp,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'admin@maiaframework.org'
);


INSERT INTO maia.email_address_verification(
    created_by_id,
    created_timestamp_utc,
    effective_range,
    email_address,
    id,
    ip_address,
    last_modified_by_id,
    last_modified_timestamp_utc,
    version
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    tstzrange(null, null),
    'admin@maiaframework.org',
    gen_random_uuid(),
    null,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    1
);


INSERT INTO maia.party_email_address(
    created_by_id,
    created_timestamp_utc,
    effective_range,
    email_address,
    id,
    is_primary_contact,
    last_modified_by_id,
    last_modified_timestamp_utc,
    party_id,
    purposes,
    version
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    tstzrange(current_timestamp, null),
    'admin@maiaframework.org',
    gen_random_uuid(),
    true,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    (select id from maia.party where first_name = 'Admin' and last_name = 'System'),
    '{USER_LOGIN}',
    1
);


insert into maia.party (
    type_discriminator,
    created_by_id,
    created_timestamp_utc,
    id,
    last_modified_timestamp_utc,
    version,
    org_name,
    authorities,
    first_name,
    last_name,
    lifecycle_state,
    encrypted_password
) values (
    'USR',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    gen_random_uuid(),
    current_timestamp,
    1,
    null,
    '{WRITE, MAIA_ELASTICSEARCH_SYS_OPS_READ, MAIA_ELASTICSEARCH_SYS_OPS_WRITE, MAIA_JOB_READ, MAIA_JOB_WRITE}',
    'Sysops',
    'System',
    'ACTIVE',
    '{bcrypt}$2a$10$zI.pQy.gVMVRzsuuBxjc/.7/ZvtXzSWqGw6p4srdJi0FQ6YSn6E1S' -- d0uglas
);


insert into maia.email_address (
    created_timestamp_utc,
    created_by_id,
    email_address
) values (
    current_timestamp,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'sysops@maiaframework.org'
);


INSERT INTO maia.email_address_verification(
    created_by_id,
    created_timestamp_utc,
    effective_range,
    email_address,
    id,
    ip_address,
    last_modified_by_id,
    last_modified_timestamp_utc,
    version
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    tstzrange(null, null),
    'sysops@maiaframework.org',
    gen_random_uuid(),
    null,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    1
);


INSERT INTO maia.party_email_address(
    created_by_id,
    created_timestamp_utc,
    effective_range,
    email_address,
    id,
    is_primary_contact,
    last_modified_by_id,
    last_modified_timestamp_utc,
    party_id,
    purposes,
    version
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    tstzrange(current_timestamp, null),
    'sysops@maiaframework.org',
    gen_random_uuid(),
    true,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    current_timestamp,
    (select id from maia.party where first_name = 'Sysops' and last_name = 'System'),
    '{USER_LOGIN}',
    1
);


insert into maia.user_group_membership (
    created_timestamp_utc,
    id,
    user_group_id,
    user_id
) values (
    current_timestamp,
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    (select id from maia.user_group where name = 'Read-Only'),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa' -- Fyrst Naime
);


insert into maia.user_group_membership (
    created_timestamp_utc,
    id,
    user_group_id,
    user_id
) values (
    current_timestamp,
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    (select id from maia.user_group where name = 'Read-Write'),
    (select id from maia.party where first_name = 'Admin' and last_name = 'System')
);

