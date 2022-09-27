CREATE TABLE IF NOT EXISTS transaction
(
    id                 SERIAL      PRIMARY KEY,
    buy_order_id       character varying(128) NOT NULL,
    sell_order_id      character varying(128) NOT NULL,
    price              bigint       NOT NULL,
    quantity           bigint       NOT NULL,
    created_timestamp  timestamp with time zone DEFAULT now()
);

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.created_timestamp = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp
BEFORE INSERT ON transaction
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

