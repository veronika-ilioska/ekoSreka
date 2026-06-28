UPDATE users
SET password = '$2a$10$xzB/wMIReeT5tlbED5jQROn.lMctmHME4eLrkAvOIa.lFjuv1M2aq',
    updated_at = FLOOR(EXTRACT(EPOCH FROM NOW()) * 1000)
WHERE username = 'admin_test';
