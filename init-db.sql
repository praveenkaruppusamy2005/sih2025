-- Initialize the terminology database
-- This script sets up the initial database structure and permissions

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create indexes for better performance
-- These will be created by JPA, but we can add some custom ones

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE terminology_db TO terminology_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO terminology_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO terminology_user;

-- Create audit log table (if not exists)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    operation VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    user_id VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSONB,
    ip_address INET,
    user_agent TEXT
);

-- Create index on audit logs for better query performance
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_operation ON audit_logs(operation);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);

-- Create system configuration table
CREATE TABLE IF NOT EXISTS system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default configuration
INSERT INTO system_config (config_key, config_value, description) VALUES
('system.version', '1.0.0', 'System version'),
('system.name', 'NAMASTE-ICD11 Terminology Service', 'System name'),
('fhir.version', 'R4', 'FHIR version'),
('icd11.api.enabled', 'true', 'ICD-11 API integration enabled'),
('namaste.data.version', '1.0', 'NAMASTE data version')
ON CONFLICT (config_key) DO NOTHING;

-- Create trigger for system_config updated_at
CREATE TRIGGER update_system_config_updated_at
    BEFORE UPDATE ON system_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create a view for system statistics
CREATE OR REPLACE VIEW system_stats AS
SELECT 
    (SELECT COUNT(*) FROM namaste_codes) as namaste_code_count,
    (SELECT COUNT(*) FROM icd11_codes) as icd11_code_count,
    (SELECT COUNT(*) FROM concept_mappings) as mapping_count,
    (SELECT COUNT(*) FROM namaste_codes WHERE system = 'AYURVEDA') as ayurveda_count,
    (SELECT COUNT(*) FROM namaste_codes WHERE system = 'SIDDHA') as siddha_count,
    (SELECT COUNT(*) FROM namaste_codes WHERE system = 'UNANI') as unani_count,
    (SELECT COUNT(*) FROM icd11_codes WHERE code_type = 'TM2') as tm2_count,
    (SELECT COUNT(*) FROM icd11_codes WHERE code_type = 'BIOMEDICINE') as biomedicine_count;

-- Grant permissions on the view
GRANT SELECT ON system_stats TO terminology_user;

-- Create a function to get mapping statistics
CREATE OR REPLACE FUNCTION get_mapping_stats()
RETURNS TABLE(
    source_system VARCHAR,
    target_system VARCHAR,
    equivalence VARCHAR,
    count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        cm.source_system,
        cm.target_system,
        cm.equivalence::VARCHAR,
        COUNT(*) as count
    FROM concept_mappings cm
    GROUP BY cm.source_system, cm.target_system, cm.equivalence
    ORDER BY count DESC;
END;
$$ LANGUAGE plpgsql;

-- Grant execute permission on the function
GRANT EXECUTE ON FUNCTION get_mapping_stats() TO terminology_user;

-- Create a function to search across all terminology systems
CREATE OR REPLACE FUNCTION search_all_terminologies(search_term TEXT)
RETURNS TABLE(
    system_type VARCHAR,
    code VARCHAR,
    display VARCHAR,
    definition TEXT,
    system VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        'NAMASTE'::VARCHAR as system_type,
        nc.code,
        nc.display,
        nc.definition,
        nc.system::VARCHAR
    FROM namaste_codes nc
    WHERE 
        nc.display ILIKE '%' || search_term || '%' OR
        nc.code ILIKE '%' || search_term || '%' OR
        nc.definition ILIKE '%' || search_term || '%'
    
    UNION ALL
    
    SELECT 
        'ICD11'::VARCHAR as system_type,
        ic.code,
        ic.title as display,
        ic.definition,
        ic.code_type::VARCHAR
    FROM icd11_codes ic
    WHERE 
        ic.title ILIKE '%' || search_term || '%' OR
        ic.code ILIKE '%' || search_term || '%' OR
        ic.definition ILIKE '%' || search_term || '%'
    
    ORDER BY system_type, display;
END;
$$ LANGUAGE plpgsql;

-- Grant execute permission on the function
GRANT EXECUTE ON FUNCTION search_all_terminologies(TEXT) TO terminology_user;

-- Create indexes for better search performance
CREATE INDEX IF NOT EXISTS idx_namaste_codes_display_gin ON namaste_codes USING gin(display gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_namaste_codes_definition_gin ON namaste_codes USING gin(definition gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_icd11_codes_title_gin ON icd11_codes USING gin(title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_icd11_codes_definition_gin ON icd11_codes USING gin(definition gin_trgm_ops);

-- Create a materialized view for frequently accessed data
CREATE MATERIALIZED VIEW IF NOT EXISTS terminology_summary AS
SELECT 
    'NAMASTE' as system_name,
    system as subsystem,
    COUNT(*) as code_count,
    COUNT(DISTINCT category) as category_count
FROM namaste_codes
GROUP BY system

UNION ALL

SELECT 
    'ICD-11' as system_name,
    code_type as subsystem,
    COUNT(*) as code_count,
    COUNT(DISTINCT chapter) as category_count
FROM icd11_codes
GROUP BY code_type;

-- Create index on the materialized view
CREATE INDEX IF NOT EXISTS idx_terminology_summary_system ON terminology_summary(system_name, subsystem);

-- Create a function to refresh the materialized view
CREATE OR REPLACE FUNCTION refresh_terminology_summary()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW terminology_summary;
END;
$$ LANGUAGE plpgsql;

-- Grant execute permission on the function
GRANT EXECUTE ON FUNCTION refresh_terminology_summary() TO terminology_user;

-- Create a scheduled job to refresh the materialized view (requires pg_cron extension)
-- This is commented out as pg_cron might not be available in all PostgreSQL installations
-- SELECT cron.schedule('refresh-terminology-summary', '0 2 * * *', 'SELECT refresh_terminology_summary();');

-- Insert some sample data for testing (optional)
-- This can be uncommented for development/testing purposes
/*
INSERT INTO namaste_codes (code, display, definition, system, category, version) VALUES
('TEST001', 'Test Ayurveda Condition', 'A test condition for development', 'AYURVEDA', 'Test Category', '1.0')
ON CONFLICT (code) DO NOTHING;

INSERT INTO icd11_codes (code, title, definition, code_type) VALUES
('TEST-001', 'Test ICD-11 Condition', 'A test condition for development', 'TM2')
ON CONFLICT (code) DO NOTHING;
*/

-- Final permissions check
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO terminology_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO terminology_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO terminology_user;
