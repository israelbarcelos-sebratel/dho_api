-- SQL script to create document related tables

CREATE TABLE IF NOT EXISTS document_type (
    id INT AUTO_INCREMENT PRIMARY KEY,
    document_type_name VARCHAR(255) NOT NULL,
    document_type_description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_path VARCHAR(500) NOT NULL,
    people_id INT,
    document_type_id INT,
    upload_date DATETIME,
    CONSTRAINT fk_documents_people FOREIGN KEY (people_id) REFERENCES people(id),
    CONSTRAINT fk_documents_type FOREIGN KEY (document_type_id) REFERENCES document_type(id)
);

-- Initial Document Types
INSERT INTO document_type (document_type_name, document_type_description) VALUES 
('RG', 'Registro Geral'),
('CPF', 'Cadastro de Pessoa Física'),
('PIS', 'Programa de Integração Social'),
('CTPS', 'Carteira de Trabalho e Previdência Social'),
('Comprovante de Residência', 'Comprovante de endereço atualizado'),
('Título de Eleitor', 'Título de Eleitor'),
('Certificado de Reservista', 'Certificado de Reservista (para homens)'),
('Certidão de Nascimento/Casamento', 'Certidão de Nascimento ou Casamento'),
('Comprovante de Escolaridade', 'Diploma ou histórico escolar'),
('Foto 3x4', 'Foto colorida recente');
