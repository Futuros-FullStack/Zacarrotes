-- Ejecutar en la base de datos "zacarrotes" (PostgreSQL)
CREATE TABLE IF NOT EXISTS proveedores (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(150) NOT NULL UNIQUE,
    direccion VARCHAR(255) NOT NULL UNIQUE,
    telefono  VARCHAR(10)  NOT NULL
);
