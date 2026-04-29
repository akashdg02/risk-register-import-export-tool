CREATE TABLE risk_registers (
    id BIGSERIAL PRIMARY KEY,
    risk_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    impact_level VARCHAR(30) NOT NULL,
    likelihood_level VARCHAR(30) NOT NULL,
    owner_name VARCHAR(120) NOT NULL,
    owner_email VARCHAR(150) NOT NULL,
    mitigation_plan VARCHAR(2000),
    target_resolution_date DATE,
    source_system VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_risk_registers_status ON risk_registers (status);
CREATE INDEX idx_risk_registers_category ON risk_registers (category);
CREATE INDEX idx_risk_registers_owner_email ON risk_registers (owner_email);
