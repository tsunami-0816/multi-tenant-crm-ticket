-- 多租户客户工单 SaaS 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS multi_tenant_saas DEFAULT CHARSET utf8mb4;
USE multi_tenant_saas;

CREATE TABLE IF NOT EXISTS tenant (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_name VARCHAR(100) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL COMMENT 'SALES / SUPPORT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tenant_username (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS customer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  contact VARCHAR(50),
  phone VARCHAR(20),
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0潜在 1成交 2流失',
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_tenant_name (tenant_id, name)
);

CREATE TABLE IF NOT EXISTS customer_follow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  content TEXT,
  follow_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_tenant_customer (tenant_id, customer_id)
);

CREATE TABLE IF NOT EXISTS ticket (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/PROCESSING/RESOLVED/CLOSED',
  assignee_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_tenant_customer (tenant_id, customer_id)
);

CREATE TABLE IF NOT EXISTS local_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  topic VARCHAR(100) NOT NULL,
  payload JSON NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待发 1已发 2失败',
  retry_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sent_at DATETIME NULL,
  KEY idx_status (status)
);

CREATE TABLE IF NOT EXISTS customer_timeline (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  event_id VARCHAR(64) NOT NULL,
  type VARCHAR(20) NOT NULL COMMENT 'TICKET_CREATED/TICKET_STATUS_CHANGED',
  title VARCHAR(200),
  occurred_at DATETIME NOT NULL,
  UNIQUE KEY uk_event (event_id),
  KEY idx_tenant_customer (tenant_id, customer_id)
);
