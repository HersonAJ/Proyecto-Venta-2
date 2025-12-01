-- ========================================
--   CREACIÓN DE BASE DE DATOS: Tacos
-- ========================================

CREATE DATABASE IF NOT EXISTS Tacos
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE Tacos;


-- ========================================
--   TABLA: usuarios
-- ========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Contraseña encriptada
    telefono VARCHAR(15),
    avatar_id INT DEFAULT 1,
    rol ENUM('cliente', 'trabajador', 'admin') DEFAULT 'cliente',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- ========================================
--   TABLA: productos
-- ========================================
CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL, -- 'Hot Dog Clásico', 'Torta de Jamón', etc.
    tipo VARCHAR(20) NOT NULL, -- CAMBIADO: VARCHAR en lugar de ENUM
    descripcion TEXT,
    precio_base DECIMAL(8,2) NOT NULL,
    imagen_url VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    tiempo_preparacion INT DEFAULT 10 -- minutos estimados
);
-- ========================================
--   TABLA: ingredientes
-- ========================================
CREATE TABLE ingredientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL, -- 'Pan', 'Salchicha', 'Queso', 'Sin mayonesa', etc.
    tipo ENUM('base', 'exclusion') NOT NULL, -- 'base' = ingrediente normal, 'exclusion' = para quitar
    precio_adicional DECIMAL(6,2) DEFAULT 0.00,
    disponible BOOLEAN DEFAULT TRUE
);

-- ========================================
--   TABLA: producto_ingredientes (NUEVA)
-- ========================================
CREATE TABLE producto_ingredientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    ingrediente_id INT NOT NULL,
    es_obligatorio BOOLEAN DEFAULT FALSE, -- Si no se puede quitar (ej: pan, salchicha)
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id),
    UNIQUE KEY unique_producto_ingrediente (producto_id, ingrediente_id)
);

-- ========================================
--   TABLA: pedidos
-- ========================================
CREATE TABLE pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    trabajador_id INT, -- NULL hasta que se asigne
    estado ENUM(
        'pendiente', 
        'en_preparacion', 
        'listo', 
        'entregado', 
        'cancelado'
    ) DEFAULT 'pendiente',
    total DECIMAL(8,2) NOT NULL,
    metodo_pago ENUM('efectivo', 'tarjeta') DEFAULT 'efectivo',
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega TIMESTAMP NULL,
    notas TEXT, -- Notas especiales del cliente

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (trabajador_id) REFERENCES usuarios(id)
);

-- ========================================
--   TABLA: detalles_pedido
-- ========================================
CREATE TABLE detalles_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(8,2) NOT NULL, -- Precio en el momento del pedido

    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- ========================================
--   TABLA: personalizaciones_detalle (MODIFICADA)
-- ========================================
CREATE TABLE personalizaciones_detalle (
    id INT AUTO_INCREMENT PRIMARY KEY,
    detalle_pedido_id INT NOT NULL,
    ingrediente_id INT NOT NULL,
    accion ENUM('agregar', 'quitar') DEFAULT 'quitar', -- 'quitar' = cliente no quiere este ingrediente

    FOREIGN KEY (detalle_pedido_id) REFERENCES detalles_pedido(id) ON DELETE CASCADE,
    FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id)
);

-- ========================================
--   TABLA: fidelidad (VERSIÓN ACTUALIZADA)
-- ========================================
CREATE TABLE fidelidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    hotdogs_comprados INT DEFAULT 0,
    puntos_acumulados INT DEFAULT 0,
    promociones_canjeadas INT DEFAULT 0,
    promocion_actual INT DEFAULT 0, -- Progreso hacia la próxima promoción (0-6)
    promociones_pendientes INT DEFAULT 0, -- NUEVO: Promociones listas para canjear
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    UNIQUE KEY unique_usuario (usuario_id)
);

-- ========================================
--   TABLA: historial_promociones (VERSIÓN ACTUALIZADA)
-- ========================================
CREATE TABLE historial_promociones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    pedido_id INT NULL, -- CAMBIO: Ahora puede ser NULL para promociones por acumulación
    tipo ENUM('hotdog_gratis', 'descuento') DEFAULT 'hotdog_gratis',
    descripcion VARCHAR(255),
    fecha_canje TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);

-- ========================================
--   TABLA: cajas
-- ========================================
CREATE TABLE cajas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL, -- Trabajador que abre la caja
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP NULL,
    fondo_inicial DECIMAL(10,2) NOT NULL, -- Dinero inicial en caja
    total_ventas_efectivo DECIMAL(10,2) DEFAULT 0.00,
    total_ventas_tarjeta DECIMAL(10,2) DEFAULT 0.00,
    total_ventas DECIMAL(10,2) DEFAULT 0.00,
    efectivo_final DECIMAL(10,2) DEFAULT 0.00, -- Efectivo físico al cerrar
    diferencia DECIMAL(8,2) DEFAULT 0.00, -- Diferencia entre lo calculado y lo físico
    observaciones TEXT,
    estado ENUM('abierta', 'cerrada') DEFAULT 'abierta',

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ========================================
--   TABLA: ventas (VERSIÓN MEJORADA)
-- ========================================
CREATE TABLE ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NULL, -- CAMBIO: Ahora puede ser NULL para ventas directas
    usuario_id INT NULL, -- CAMBIO: Ahora puede ser NULL para clientes no registrados
    trabajador_id INT NOT NULL, -- CAMBIO: Siempre debe haber un trabajador
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('efectivo', 'tarjeta') DEFAULT 'efectivo',
    tipo_venta ENUM('normal', 'promocion') DEFAULT 'normal',
    descripcion VARCHAR(255) NULL, -- NUEVO: Para describir ventas sin pedido

    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (trabajador_id) REFERENCES usuarios(id)
);


-- ========================================
--   TABLA: gastos
-- ========================================
CREATE TABLE gastos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    caja_id INT NOT NULL,
    usuario_id INT NOT NULL, -- Quien registra el gasto
    tipo_gasto ENUM('insumos', 'servicios', 'nomina', 'otros') NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    monto DECIMAL(8,2) NOT NULL,
    proveedor VARCHAR(100),
    fecha_gasto DATE NOT NULL,
    comprobante_url VARCHAR(255), -- Imagen del ticket/factura
    notas TEXT,

    FOREIGN KEY (caja_id) REFERENCES cajas(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);





para postgres
-- ========================================
--   CREACIÓN DE BASE DE DATOS: Tacos
-- ========================================

CREATE DATABASE Tacos
WITH ENCODING 'UTF8'
LC_COLLATE='en_US.utf8'
LC_CTYPE='en_US.utf8'
TEMPLATE=template0;

\c Tacos;

-- ========================================
--   TABLA: usuarios
-- ========================================
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(15),
    avatar_id INT DEFAULT 1,
    rol VARCHAR(20) CHECK (rol IN ('cliente','trabajador','admin')) DEFAULT 'cliente',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- ========================================
--   TABLA: productos
-- ========================================
CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descripcion TEXT,
    precio_base NUMERIC(8,2) NOT NULL,
    imagen_url VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    tiempo_preparacion INT DEFAULT 10
);

-- ========================================
--   TABLA: ingredientes
-- ========================================
CREATE TABLE ingredientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    tipo VARCHAR(20) CHECK (tipo IN ('base','exclusion')) NOT NULL,
    precio_adicional NUMERIC(6,2) DEFAULT 0.00,
    disponible BOOLEAN DEFAULT TRUE
);

-- ========================================
--   TABLA: producto_ingredientes
-- ========================================
CREATE TABLE producto_ingredientes (
    id SERIAL PRIMARY KEY,
    producto_id INT NOT NULL,
    ingrediente_id INT NOT NULL,
    es_obligatorio BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id),
    CONSTRAINT unique_producto_ingrediente UNIQUE (producto_id, ingrediente_id)
);

-- ========================================
--   TABLA: pedidos
-- ========================================
CREATE TABLE pedidos (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL,
    trabajador_id INT,
    estado VARCHAR(20) CHECK (estado IN ('pendiente','en_preparacion','listo','entregado','cancelado')) DEFAULT 'pendiente',
    total NUMERIC(8,2) NOT NULL,
    metodo_pago VARCHAR(20) CHECK (metodo_pago IN ('efectivo','tarjeta')) DEFAULT 'efectivo',
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega TIMESTAMP NULL,
    notas TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (trabajador_id) REFERENCES usuarios(id)
);

-- ========================================
--   TABLA: detalles_pedido
-- ========================================
CREATE TABLE detalles_pedido (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario NUMERIC(8,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- ========================================
--   TABLA: personalizaciones_detalle
-- ========================================
CREATE TABLE personalizaciones_detalle (
    id SERIAL PRIMARY KEY,
    detalle_pedido_id INT NOT NULL,
    ingrediente_id INT NOT NULL,
    accion VARCHAR(20) CHECK (accion IN ('agregar','quitar')) DEFAULT 'quitar',
    FOREIGN KEY (detalle_pedido_id) REFERENCES detalles_pedido(id) ON DELETE CASCADE,
    FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id)
);

-- ========================================
--   TABLA: fidelidad
-- ========================================
CREATE TABLE fidelidad (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL,
    hotdogs_comprados INT DEFAULT 0,
    puntos_acumulados INT DEFAULT 0,
    promociones_canjeadas INT DEFAULT 0,
    promocion_actual INT DEFAULT 0,
    promociones_pendientes INT DEFAULT 0,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT unique_usuario UNIQUE (usuario_id)
);

-- ========================================
--   TABLA: historial_promociones
-- ========================================
CREATE TABLE historial_promociones (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL,
    pedido_id INT NULL,
    tipo VARCHAR(20) CHECK (tipo IN ('hotdog_gratis','descuento')) DEFAULT 'hotdog_gratis',
    descripcion VARCHAR(255),
    fecha_canje TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);

-- ========================================
--   TABLA: cajas
-- ========================================
CREATE TABLE cajas (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL,
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP NULL,
    fondo_inicial NUMERIC(10,2) NOT NULL,
    total_ventas_efectivo NUMERIC(10,2) DEFAULT 0.00,
    total_ventas_tarjeta NUMERIC(10,2) DEFAULT 0.00,
    total_ventas NUMERIC(10,2) DEFAULT 0.00,
    efectivo_final NUMERIC(10,2) DEFAULT 0.00,
    diferencia NUMERIC(8,2) DEFAULT 0.00,
    observaciones TEXT,
    estado VARCHAR(20) CHECK (estado IN ('abierta','cerrada')) DEFAULT 'abierta',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ========================================
--   TABLA: ventas
-- ========================================
CREATE TABLE ventas (
    id SERIAL PRIMARY KEY,
    pedido_id INT NULL,
    usuario_id INT NULL,
    trabajador_id INT NOT NULL,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total NUMERIC(10,2) NOT NULL,
    metodo_pago VARCHAR(20) CHECK (metodo_pago IN ('efectivo','tarjeta')) DEFAULT 'efectivo',
    tipo_venta VARCHAR(20) CHECK (tipo_venta IN ('normal','promocion')) DEFAULT 'normal',
    descripcion VARCHAR(255) NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (trabajador_id) REFERENCES usuarios(id)
);

-- ========================================
--   TABLA: gastos
-- ========================================
CREATE TABLE gastos (
    id SERIAL PRIMARY KEY,
    caja_id INT NOT NULL,
    usuario_id INT NOT NULL,
    tipo_gasto VARCHAR(20) CHECK (tipo_gasto IN ('insumos','servicios','nomina','otros')) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    monto NUMERIC(8,2) NOT NULL,
    proveedor VARCHAR(100),
    fecha_gasto DATE NOT NULL,
    comprobante_url VARCHAR(255),
    notas TEXT,
    FOREIGN KEY (caja_id) REFERENCES cajas(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

