INSERT INTO companies (company_name, company_address, creation_date, last_update)
VALUES ('Vibe Retail MMC', 'Baku city, Nizami street', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO warehouses (warehouse_name, warehouse_address, company_id, creation_date, last_update)
VALUES ('Main Central Warehouse', 'Baku city, Darnagul', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);