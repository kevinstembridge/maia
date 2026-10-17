# Entities with version history use surrogate primary keys

Rows in a History entity's table are unique by the audited Entity's primary key plus the record version. If an audited record with a given natural key were deleted and later recreated, its version would restart at 1 and collide with the earlier history rows, violating the unique constraint. An Entity declared with version history therefore must have a surrogate primary key, so a recreated record gets a new identity. The Generator rejects the Spec otherwise.
