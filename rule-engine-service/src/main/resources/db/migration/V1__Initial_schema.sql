CREATE TABLE rule (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    expression VARCHAR(255) NOT NULL,
    priority INT NOT NULL,
    weight INT NOT NULL,
    enabled BOOLEAN NOT NULL,
    version INT NOT NULL
);

INSERT INTO rule (id, name, description, expression, priority, weight, enabled, version) VALUES
(1, 'High Amount', 'Flags transactions with high amount', 'amount > 50000', 1, 50, true, 1),
(2, 'High Risk Score', 'Flags transactions with high risk score', 'riskScore > 70', 1, 50, true, 1);