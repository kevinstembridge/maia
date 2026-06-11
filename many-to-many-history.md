
# Many-to-many history with UUIDs



**The table**

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE left_to_right_many_to_many_join (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    left_id uuid NOT NULL REFERENCES left(id),
    right_id uuid NOT NULL REFERENCES right(id),
    effective_range tstzrange NOT NULL DEFAULT tstzrange(now(), NULL),

    -- audit columns
    created_by uuid NOT NULL REFERENCES party(id),
    ended_by uuid REFERENCES party(id),
    end_reason text,

    CONSTRAINT no_overlap EXCLUDE USING gist (
        left_id WITH =,
        right_id WITH =,
        effective_range WITH &&
    ),
    CONSTRAINT ended_has_actor CHECK (
        upper(effective_range) IS NULL OR ended_by IS NOT NULL
    )
);
```

**Indexes**

```sql
CREATE INDEX idx_membership_current
    ON left_to_right_many_to_many_join (left_id, right_id)
    WHERE upper_inf(effective_range);

CREATE INDEX idx_membership_current_by_right
    ON left_to_right_many_to_many_join (right_id)
    WHERE upper_inf(effective_range);

CREATE INDEX idx_membership_time_travel
    ON left_to_right_many_to_many_join USING gist (right_id, effective_range);
```

**Operations**

```sql
-- add
INSERT INTO left_to_right_many_to_many_join (left_id, right_id, created_by)
VALUES (:left_id::uuid, :right_id::uuid, :actor::uuid);

-- remove
UPDATE left_to_right_many_to_many_join
SET effective_range   = tstzrange(lower(effective_range), now()),
    ended_by   = :actor::uuid,
    end_reason = :reason
WHERE left_id = :left_id::uuid
  AND right_id = :right_id::uuid
  AND upper_inf(effective_range);
```

**Queries**

```sql
-- current members of a group
SELECT left_id FROM left_to_right_many_to_many_join
WHERE right_id = :g::uuid AND upper_inf(effective_range);

-- members as of a point in time
SELECT left_id FROM left_to_right_many_to_many_join
WHERE right_id = :g::uuid AND effective_range @> :ts::timestamptz;

-- audit history for one pair
SELECT 
    lower(effective_range) AS joined,
    upper(effective_range) AS left,
    created_by,
    ended_by,
    end_reason
FROM left_to_right_many_to_many_join
WHERE left_id = :u::uuid AND right_id = :g::uuid
ORDER BY lower(effective_range);
```
