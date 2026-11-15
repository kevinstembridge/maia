ALTER TABLE maia.all_field_types ADD COLUMN some_big_decimal numeric(19,4) NOT NULL DEFAULT 0;
ALTER TABLE maia.all_field_types ALTER COLUMN some_big_decimal DROP DEFAULT;

ALTER TABLE maia.all_field_types ADD COLUMN some_big_decimal_modifiable numeric(10,2) NOT NULL DEFAULT 0;
ALTER TABLE maia.all_field_types ALTER COLUMN some_big_decimal_modifiable DROP DEFAULT;

ALTER TABLE maia.all_field_types ADD COLUMN some_big_decimal_nullable numeric(19,4) NULL;
