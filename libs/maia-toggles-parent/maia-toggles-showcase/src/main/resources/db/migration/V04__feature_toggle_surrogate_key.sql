-- Replace the natural primary key (feature_name) of feature_toggle with a surrogate id,
-- and key the history table on (id, version). feature_name remains unique.

ALTER TABLE toggles.feature_toggle ADD COLUMN id uuid;
UPDATE toggles.feature_toggle SET id = gen_random_uuid();
ALTER TABLE toggles.feature_toggle ALTER COLUMN id SET NOT NULL;
ALTER TABLE toggles.feature_toggle DROP CONSTRAINT feature_toggle_pkey;
ALTER TABLE toggles.feature_toggle ADD PRIMARY KEY (id);
CREATE UNIQUE INDEX feature_toggle_feature_name_uidx ON toggles.feature_toggle(feature_name);


ALTER TABLE toggles.feature_toggle_history ADD COLUMN id uuid;

UPDATE toggles.feature_toggle_history h
SET id = t.id
FROM toggles.feature_toggle t
WHERE t.feature_name = h.feature_name;

-- History rows for toggles that no longer exist: one new id per feature name
UPDATE toggles.feature_toggle_history h
SET id = orphan.id
FROM (
    SELECT feature_name, gen_random_uuid() AS id
    FROM toggles.feature_toggle_history
    WHERE id IS NULL
    GROUP BY feature_name
) orphan
WHERE h.id IS NULL AND h.feature_name = orphan.feature_name;

ALTER TABLE toggles.feature_toggle_history ALTER COLUMN id SET NOT NULL;
ALTER TABLE toggles.feature_toggle_history DROP CONSTRAINT feature_toggle_history_pkey;
ALTER TABLE toggles.feature_toggle_history ADD PRIMARY KEY (id, version);
CREATE INDEX hist_feature_toggle_feature_name_idx ON toggles.feature_toggle_history(feature_name);
