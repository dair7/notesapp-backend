package com.notesapp.enums;

public enum RoleType {
    USER,           // Usuario normal de la app
    ADMIN,          // Administrador del panel web (creado directamente en BD por SUPER_ADMIN)
    SUPER_ADMIN     // Solo existe en BD — gestiona admins directamente desde la base de datos, sin acceso a la API
}
