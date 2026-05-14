CREATE TABLE IF NOT EXISTS talent_pool (
    id INT AUTO_INCREMENT PRIMARY KEY,
    people_id INT NOT NULL,
    observations TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_talent_pool_people FOREIGN KEY (people_id) REFERENCES people(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS talent_pool_positions (
    talent_pool_id INT NOT NULL,
    position_id INT NOT NULL,
    PRIMARY KEY (talent_pool_id, position_id),
    CONSTRAINT fk_tpp_talent_pool FOREIGN KEY (talent_pool_id) REFERENCES talent_pool(id) ON DELETE CASCADE,
    CONSTRAINT fk_tpp_position FOREIGN KEY (position_id) REFERENCES position(id) ON DELETE CASCADE
);