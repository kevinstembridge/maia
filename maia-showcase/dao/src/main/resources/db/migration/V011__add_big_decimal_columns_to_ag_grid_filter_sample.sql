ALTER TABLE maia.ag_grid_filter_sample ADD COLUMN amount_required numeric(19,4) NOT NULL DEFAULT 0;
ALTER TABLE maia.ag_grid_filter_sample ALTER COLUMN amount_required DROP DEFAULT;

ALTER TABLE maia.ag_grid_filter_sample ADD COLUMN amount_nullable numeric(19,4) NULL;
