-- Props: replace the natural primary key (property_name) with a surrogate id, and key the
-- history table on (id, version). property_name remains unique.

ALTER TABLE props.props ADD COLUMN id uuid;
UPDATE props.props SET id = gen_random_uuid();
ALTER TABLE props.props ALTER COLUMN id SET NOT NULL;
ALTER TABLE props.props DROP CONSTRAINT props_pkey;
ALTER TABLE props.props ADD PRIMARY KEY (id);
CREATE UNIQUE INDEX props_property_name_uidx ON props.props(property_name);


ALTER TABLE props.props_history ADD COLUMN id uuid;

UPDATE props.props_history h
SET id = p.id
FROM props.props p
WHERE p.property_name = h.property_name;

-- History for properties that no longer exist: one new id per property name
UPDATE props.props_history h
SET id = orphan.id
FROM (
    SELECT property_name, gen_random_uuid() AS id
    FROM props.props_history
    WHERE id IS NULL
    GROUP BY property_name
) orphan
WHERE h.id IS NULL AND h.property_name = orphan.property_name;

ALTER TABLE props.props_history ALTER COLUMN id SET NOT NULL;
ALTER TABLE props.props_history DROP CONSTRAINT props_history_pkey;
ALTER TABLE props.props_history ADD PRIMARY KEY (id, version);
CREATE INDEX hist_props_property_name_idx ON props.props_history(property_name);
