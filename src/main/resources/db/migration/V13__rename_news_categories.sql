UPDATE news
SET category = CASE lower(trim(category))
    WHEN 'рециклирање' THEN 'Светот и животната средина'
    WHEN 'енергија' THEN 'Македонија и екологијата'
    WHEN 'природа' THEN 'Учиме за екологија'
END
WHERE lower(trim(category)) IN ('рециклирање', 'енергија', 'природа');
