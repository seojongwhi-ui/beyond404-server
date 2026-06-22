ALTER TABLE valuations
    ALTER COLUMN currency SET DEFAULT 'KRW';

ALTER TABLE credits
    ALTER COLUMN currency SET DEFAULT 'KRW';

UPDATE valuations
SET currency = 'KRW'
WHERE currency = 'INR';

UPDATE credits
SET currency = 'KRW'
WHERE currency = 'INR';
