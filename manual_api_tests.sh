# ===============================================
# PARKSPOT API - MANUAL TESTING COMMANDS
# ===============================================

# 1. LOGIN ADMIN
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'

# 2. LOGIN VIGILANTE  
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "vigilante1", 
    "password": "123456"
  }'

# 3. LISTAR CLIENTES (reemplazar {TOKEN} con el token real)
curl -X GET http://localhost:8080/api/admin/clientes \
  -H "Authorization: Bearer {TOKEN}"

# 4. VER ESPACIOS DISPONIBLES
curl -X GET http://localhost:8080/api/estacionamiento/espacios/disponibles \
  -H "Authorization: Bearer {TOKEN_VIGILANTE}"

# 5. REGISTRAR ENTRADA DE VEHICULO
curl -X POST http://localhost:8080/api/estacionamiento/registrar-entrada \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN_VIGILANTE}" \
  -d '{
    "clienteId": 1,
    "vehiculoId": 1, 
    "espacioId": 1,
    "observaciones": "Test desde cURL"
  }'

# 6. VER REGISTROS ACTIVOS
curl -X GET http://localhost:8080/api/estacionamiento/registros-activos \
  -H "Authorization: Bearer {TOKEN_VIGILANTE}"

# 7. REGISTRAR SALIDA
curl -X POST http://localhost:8080/api/estacionamiento/registrar-salida \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN_VIGILANTE}" \
  -d '{
    "registroId": 1,
    "observaciones": "Salida desde cURL"
  }'

# ===============================================
# TESTS DE ERROR (Para validar excepciones)
# ===============================================

# Test: Cliente inexistente
curl -X POST http://localhost:8080/api/estacionamiento/registrar-entrada \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN_VIGILANTE}" \
  -d '{
    "clienteId": 999,
    "vehiculoId": 1,
    "espacioId": 1,
    "observaciones": "Test error"
  }'

# Test: Espacio ocupado
curl -X POST http://localhost:8080/api/estacionamiento/registrar-entrada \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN_VIGILANTE}" \
  -d '{
    "clienteId": 1,
    "vehiculoId": 1,
    "espacioId": 1,
    "observaciones": "Test espacio ocupado"
  }'

# Test: Sin autorización
curl -X GET http://localhost:8080/api/admin/clientes

# Test: Token inválido
curl -X GET http://localhost:8080/api/admin/clientes \
  -H "Authorization: Bearer token_falso"
