
CREATE ROLE maia_read_only IN ROLE pg_read_all_data;

CREATE ROLE maia_read_write IN ROLE pg_write_all_data, maia_read_only;

CREATE ROLE maia_owner_role IN ROLE maia_read_write;
GRANT ALL ON SCHEMA public TO maia_owner_role;

CREATE ROLE maia_owner LOGIN PASSWORD 'maia_owner_password' IN ROLE maia_owner_role;

CREATE ROLE maia_app LOGIN PASSWORD 'maia_app_password' IN ROLE maia_read_write;

CREATE DATABASE maia_db OWNER maia_owner;

CREATE ROLE maia_quartz_read_only;

CREATE ROLE maia_quartz_read_write;

CREATE ROLE maia_quartz_owner_role IN ROLE maia_quartz_read_write;

CREATE ROLE maia_quartz_owner LOGIN PASSWORD 'maia_quartz_owner_password' IN ROLE maia_quartz_owner_role;

CREATE ROLE maia_quartz LOGIN PASSWORD 'maia_quartz_password' IN ROLE maia_quartz_read_write;

CREATE SCHEMA maia_quartz;

GRANT ALL ON SCHEMA maia_quartz TO maia_quartz_read_write;
