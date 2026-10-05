-- Grant the feature toggles authorities to the precanned Sysops user, alongside its other
-- ops authorities. The party is versioned, so bump the version and record a matching
-- party_history row.

update maia.party
set authorities = authorities || array['MAIA_TOGGLES_READ', 'MAIA_TOGGLES_WRITE'],
    version = version + 1,
    last_modified_timestamp = current_timestamp
where type_discriminator = 'USR'
  and first_name = 'Sysops'
  and last_name = 'System'
  and not (authorities @> array['MAIA_TOGGLES_READ', 'MAIA_TOGGLES_WRITE']);

insert into maia.party_history (
    type_discriminator, authorities, change_type, created_by_id, created_timestamp,
    encrypted_password, first_name, id, last_modified_by_id, last_modified_timestamp,
    last_name, lifecycle_state, org_name, version
)
select
    type_discriminator, authorities, 'UPDATE', created_by_id, created_timestamp,
    encrypted_password, first_name, id, last_modified_by_id, last_modified_timestamp,
    last_name, lifecycle_state, org_name, version
from maia.party p
where p.type_discriminator = 'USR'
  and p.first_name = 'Sysops'
  and p.last_name = 'System'
  and not exists (
      select 1 from maia.party_history h where h.id = p.id and h.version = p.version
  );
