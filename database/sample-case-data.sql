-- Run after the application has started once with the development profile.
-- The application uses spring.jpa.hibernate.ddl-auto=update in development.

INSERT INTO cases (id, case_number, title, description, status, created_at, updated_at, version)
VALUES (
    '6f7a4c0e-1c5b-4e8e-8b91-2f3c4d5e6a70',
    'CASE-100001',
    'Customer service case',
    'Customer requested investigation of a failed transaction.',
    'IN_PROGRESS',
    '2026-08-31T10:00:00Z',
    '2026-08-31T11:30:00Z',
    0
)
ON CONFLICT (case_number) DO NOTHING;

INSERT INTO case_activities
    (id, case_id, activity_type, description, status, performed_by, activity_at)
VALUES
(
    '1a2b3c4d-5e6f-4789-9012-345678901234',
    '6f7a4c0e-1c5b-4e8e-8b91-2f3c4d5e6a70',
    'CASE_CREATED',
    'Case created from customer request',
    'COMPLETED',
    'system',
    '2026-08-31T10:00:00Z'
),
(
    '2b3c4d5e-6f70-4890-a123-456789012345',
    '6f7a4c0e-1c5b-4e8e-8b91-2f3c4d5e6a70',
    'DOCUMENT_REVIEW',
    'Supporting documents reviewed',
    'IN_PROGRESS',
    'agent01',
    '2026-08-31T11:30:00Z'
)
ON CONFLICT (id) DO NOTHING;
