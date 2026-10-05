-- OrgRole: replace the natural primary key (key) with a surrogate id. key remains unique.
-- The OrgToOrgRole join now references the role by id (column role -> role_id).

ALTER TABLE maia.org_role ADD COLUMN id uuid;
UPDATE maia.org_role SET id = gen_random_uuid();
ALTER TABLE maia.org_role ALTER COLUMN id SET NOT NULL;

ALTER TABLE maia.org_to_org_role ADD COLUMN role_id uuid;
UPDATE maia.org_to_org_role j SET role_id = r.id FROM maia.org_role r WHERE r.key = j.role;
ALTER TABLE maia.org_to_org_role ALTER COLUMN role_id SET NOT NULL;
DROP INDEX maia.org_to_org_role_role_idx;
ALTER TABLE maia.org_to_org_role DROP COLUMN role;

ALTER TABLE maia.org_role DROP CONSTRAINT org_role_pkey;
ALTER TABLE maia.org_role ADD PRIMARY KEY (id);
CREATE UNIQUE INDEX org_role_key_uidx ON maia.org_role(key);

ALTER TABLE maia.org_to_org_role ADD CONSTRAINT org_to_org_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES maia.org_role(id);
CREATE INDEX org_to_org_role_role_id_idx ON maia.org_to_org_role(role_id);

ALTER TABLE maia.org_role_history ADD COLUMN id uuid;
UPDATE maia.org_role_history h SET id = r.id FROM maia.org_role r WHERE r.key = h.key;
-- History for roles that no longer exist: one new id per key
UPDATE maia.org_role_history h
SET id = orphan.id
FROM (
    SELECT key, gen_random_uuid() AS id
    FROM maia.org_role_history
    WHERE id IS NULL
    GROUP BY key
) orphan
WHERE h.id IS NULL AND h.key = orphan.key;
ALTER TABLE maia.org_role_history ALTER COLUMN id SET NOT NULL;
ALTER TABLE maia.org_role_history DROP CONSTRAINT org_role_history_pkey;
ALTER TABLE maia.org_role_history ADD PRIMARY KEY (id, version);
CREATE INDEX hist_org_role_key_idx ON maia.org_role_history(key);


-- These showcase entities have non-surrogate primary keys, which can no longer record history.
DROP TABLE maia.composite_primary_key_history;
DROP TABLE maia.non_surrogate_primary_key_history;
DROP TABLE maia.non_surrogate_id_primary_key_history;
