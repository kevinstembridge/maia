
CREATE OR REPLACE VIEW maia.v_party as
select p.*,
       CASE p.type_discriminator
           WHEN 'ORG' then p.org_name
           WHEN 'PER' then p.first_name || ' ' || p.last_name
           WHEN 'USR' then p.first_name || ' ' || p.last_name
           END as display_name
from maia.party p;


CREATE OR REPLACE VIEW maia.v_party_history as
select p.*,
       CASE p.type_discriminator
           WHEN 'ORG' then p.org_name
           WHEN 'PER' then p.first_name || ' ' || p.last_name
           WHEN 'USR' then p.first_name || ' ' || p.last_name
           END as display_name
from maia.party_history p;

