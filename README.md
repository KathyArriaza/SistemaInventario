# Sistema de Gestión de Inventario · Skills y agentes

Backend Java 17 y Spring Boot 3.4.5. Un auditor consulta existencias y un operativo registra movimientos sobre las mismas posiciones que expone el CRUD. Cada posición identifica un producto por su código (SKU) y una sucursal por su ID.

## Demostración en VS Code

Descomprime el ZIP y abre la carpeta que contiene `pom.xml`. Selecciona un JDK 17. El Maven Wrapper viene incluido. La primera compilación descarga dependencias. Ejecuta estos comandos en PowerShell desde la raíz del proyecto:

```powershell
.\mvnw.cmd clean test
$env:SPRING_PROFILES_ACTIVE = "demo"
$env:AUDITOR_KEY = [guid]::NewGuid().ToString()
$env:OPERATOR_KEY = [guid]::NewGuid().ToString()
.\mvnw.cmd spring-boot:run
```

Deja esa terminal abierta. Para tener las mismas variables en la terminal de prueba, antes de ejecutar la última línea puedes abrir otra ventana con `Start-Process powershell` desde la terminal configurada. La API escucha en el puerto 8090. En Linux/macOS usa `./mvnw` y exporta las mismas variables. El perfil **demo** carga tres productos desde JSON en una base H2 en memoria y reconstruye sus datos al reiniciar. Las dos claves deben ser distintas. Un perfil sin clave configurada rechaza las peticiones.

### Guion reproducible sin cuenta de IA

En la segunda terminal de PowerShell:

```powershell
$base = "http://localhost:8090/api/agents"
$a = @{"X-Agent-Key"=$env:AUDITOR_KEY}
$o = @{"X-Agent-Key"=$env:OPERATOR_KEY}
Invoke-RestMethod "$base/auditor/skills" -Headers $a | Select-Object name,description
function Consulta($ruta, $cabecera, $datos) {
    Invoke-RestMethod "$base/$ruta" -Method Post -Headers $cabecera -ContentType "application/json" -Body ($datos | ConvertTo-Json)
}
Consulta "auditor/skills/stock_query" $a @{} | ConvertTo-Json -Depth 6
Consulta "auditor/skills/low_stock" $a @{} | ConvertTo-Json -Depth 6
$m = @{operacion=[guid]::NewGuid().ToString();sku="CAB-01";sucursal=1;tipo="SALIDA";cantidad=3}
Consulta "operativo/skills/inventory_movement" $o $m | ConvertTo-Json
Consulta "operativo/skills/inventory_movement" $o $m | ConvertTo-Json
Consulta "auditor/skills/stock_query" $a @{sku="CAB-01"} | ConvertTo-Json -Depth 6
```

En una demo recién iniciada, la primera consulta suma 18 unidades con valor **286.00** (12 cables × 5.50, 2 puntos de acceso × 80 y 4 teclados × 15). Las alertas muestran AP-02 crítico y sin rotación, y TEC-03 bajo. La primera salida deja 9 cables, valorados en 49.50. Repetir exactamente la misma operación devuelve el recibo original. La valoración global pasa a 269.50.

Respuesta de ejemplo para una salida con el UUID indicado:

```json
{"estado":"ejecutado","skill":"inventory_movement","resultado":{"operacion":"e8541ece-9e5e-40bb-89e1-ac82644121a4","sku":"CAB-01","sucursal":1,"tipo":"SALIDA","cantidad":3,"anterior":12,"saldo":9}}
```

Equivalente cURL para consultar una categoría desde Bash:

```bash
curl -sS -X POST http://localhost:8090/api/agents/auditor/skills/stock_query \
  -H "X-Agent-Key: $AUDITOR_KEY" -H 'Content-Type: application/json' \
  -d '{"categoria":"Oficina"}'
```

### Conversación con LLM

Configura `LLM_API_KEY` antes de arrancar. `LLM_URL` debe apuntar al endpoint completo de chat completions de un proveedor compatible con function calling. Configura `LLM_MODEL` para el modelo disponible en esa cuenta. El servidor envía al proveedor el mensaje del usuario, la plantilla y los esquemas autorizados.

```powershell
Consulta "auditor/chat" $a @{mensaje="Consulta existencias del SKU CAB-01."} | ConvertTo-Json -Depth 6
Consulta "operativo/chat" $o @{mensaje="Propón una salida de 3 unidades del SKU CAB-01 en la sucursal 1 con UUID e8541ece-9e5e-40bb-89e1-ac82644121a4."} | ConvertTo-Json -Depth 6
```

Si el modelo elige una consulta, la respuesta contiene el resultado calculado por Java. Si pide aclaración, devuelve `estado: mensaje`. Para un movimiento válido el chat devuelve el siguiente tipo de propuesta:

```json
{"estado":"pendiente_confirmacion","skill":"inventory_movement","resultado":{"operacion":"e8541ece-9e5e-40bb-89e1-ac82644121a4","sku":"CAB-01","sucursal":1,"tipo":"SALIDA","cantidad":3}}
```

El operativo revisa esos campos y envía el objeto `resultado` al endpoint estructurado de movimientos para confirmarlo. El chat procesa un mensaje por petición. Los endpoints de skills funcionan aunque el proveedor esté desconectado.

## Diagnóstico del proyecto recibido

| Evidencia inicial | Decisión aplicada |
|---|---|
| El ZIP ya tenía controladores, servicios e interfaces Spring Data. InventarioController delegaba en InventarioService, aunque su contrato REST exponía entidades JPA. | Se conserva el CRUD y sus rutas. La API de agentes usa records independientes de las entidades. |
| InventarioServiceImpl duplicaba la resolución de producto y sucursal entre guardar y actualizar. Actualizar sustituía la cantidad. No había un registro de entradas y salidas. | Se centralizó la aplicación de cambios del CRUD. Los movimientos usan diferencias de unidades, bloqueo por posición y un recibo persistente. |
| Producto.codigo ya era único e Inventario relacionaba producto y sucursal. El conjunto inicial no contenía un cliente LLM, prompts ni agentes. | Se reutiliza el código como SKU, las relaciones JPA y el precio original. Se incorpora un planificador HTTP y un catálogo de skills. |
| application.properties fijaba la conexión local y sus credenciales. Los servicios de empleados requerían PasswordEncoder sin un bean declarado en las fuentes. | La conexión se configura por entorno. Se declara el codificador BCrypt y se conserva el mecanismo de acceso del CRUD por separado. |

## Estructura

```text
src/main/java/
  udb/ads/be/inventario/          Aplicación existente, CRUD, entidades y repositorios JPA
  com/proyecto/inventario/
    config/                      InventoryConfig, propiedades tipadas, reloj y seguridad web
    controller/                  AgentController, traducción global de errores de agentes
    agent/                       AgentFactory, AgentService, Planner, HttpPlanner
    skills/                      InventorySkill, SkillContract y tres implementaciones
    inventory/                   InventoryPort, JpaInventory, DemoSeed e InventoryError
src/main/resources/
  application.yml                Perfiles, conexión, modelo, claves, rutas y umbrales
  agents/{auditor,operativo}.json Rol, tono, reglas de decisión y lista de skills
  skills/*.json                  Nombres públicos, descripciones y esquemas de entrada
  data/inventario.json           Productos y saldos iniciales de la demo
src/test/                        Pruebas de contexto, API, reglas y adaptador HTTP
```

## Decisiones de diseño

- **Inversión de dependencias.** Las skills reciben InventoryPort por constructor. JpaInventory resuelve ese contrato sobre las tablas existentes. AgentService recibe Planner, cuyo adaptador usa RestClient. Spring descubre las implementaciones de InventorySkill e inyecta la colección en AgentFactory.
- **Configuración y comportamiento.** @ConfigurationProperties agrupa opciones de aplicación. AgentFactory carga plantillas al iniciar, enlaza nombres a beans y rechaza referencias desconocidas o perfiles duplicados. El servidor comprueba la autorización tanto para la ejecución directa como para las propuestas del LLM.
- **Contrato de una skill.** InventorySkill publica su descriptor y ejecuta una operación. SkillContract usa el mismo recurso para descubrimiento y validación de los tipos, campos obligatorios, enumeraciones y rangos utilizados en este catálogo. Para añadir una capacidad, crea un bean, su descriptor JSON y agrega el nombre a las plantillas autorizadas.
- **Consistencia.** El registro de movimiento bloquea la posición, comprueba cantidad positiva y saldo representable, modifica existencias y persiste el recibo dentro de una transacción. La clave primaria del recibo impide reutilizar un UUID para dos órdenes distintas. El PUT del CRUD también bloquea la posición al ajustar el saldo absoluto.
- **Rotación.** Las entradas, salidas y ajustes efectivos del CRUD actualizan ultimoMovimiento. Para inventario anterior a la migración, una fecha nula se informa como DESCONOCIDA. Un saldo reciente en una sucursal no oculta la inactividad en otra.
- **Alcance de permisos.** La clave por perfil autoriza toda su lista de capacidades. No identifica empleados individuales. La auditoría de movimientos cubre operaciones de la skill. El CRUD histórico permite ajustes absolutos por su propia vía de acceso.

## Catálogo y matriz de permisos

| Skill | Entrada | Resultado | Auditor | Operativo |
|---|---|---|---|---|
| stock_query | sku y categoria opcionales, combinables | Posiciones por sucursal, unidades totales y suma de cantidad × precio | Sí | Sí |
| low_stock | Objeto vacío | Nivel crítico/bajo e inactividad por posición. Incluye posiciones normales con actividad desconocida o antigua | Sí | Sí |
| inventory_movement | operacion (UUID), sku, sucursal, tipo (ENTRADA/SALIDA), cantidad entera positiva | Recibo con saldo anterior y posterior | No | Sí |

Los umbrales son inclusivos: crítico hasta 2 unidades, bajo hasta 5 y sin rotación desde 90 días. Son valores iniciales editables en application.yml. La valoración usa el precio del catálogo y presupone una sola moneda.

## MySQL y configuración

El perfil predeterminado es mysql. Configura DB_USER y DB_PASSWORD, y DB_URL si cambia la ubicación. La base debe existir. Se conserva ddl-auto=update del proyecto recibido: Hibernate añade movimientos_agente y la columna nullable de último movimiento. Antes de usar una base compartida, revisa el cambio de esquema en una copia. La semilla JSON se lee exclusivamente en demo. El CRUD y los agentes comparten la misma fuente de datos en ambos perfiles.

| Propiedad | Uso |
|---|---|
| inventory.seed / inventory.templates | Recurso de semilla y patrón de recursos de agentes |
| inventory.critical / low / inactive-days | Umbrales de clasificación y días de inactividad |
| inventory.model / temperature / llm-url / llm-key | Opciones del planificador |
| inventory.profile-keys | Clave por ID de plantilla. AUDITOR_KEY y OPERATOR_KEY configuran los perfiles incluidos |
| PORT | Puerto HTTP, 8090 por defecto |

## Validación y errores

Ejecuta `./mvnw clean test` (o `.\mvnw.cmd clean test`). Las pruebas usan H2, peticiones MockMvc y un proveedor HTTP local simulado. Cubren catálogo, permisos por ruta y por herramienta, formato de entrada, consulta por categoría, umbrales, inactividad, propuesta sin escritura, saldo insuficiente, reintento de salida y conflicto de UUID. La prueba HTTP inspecciona el catálogo enviado y la llamada de herramienta recibida.

| Caso reproducible | HTTP esperado |
|---|---|
| Cantidad cero, decimal o campo desconocido | 400 |
| Auditor intenta ejecutar inventory_movement o usa su clave en la ruta operativa | 403 |
| Perfil desconocido o movimiento sobre una posición inexistente | 404 |
| Salida mayor que el saldo o UUID repetido con datos distintos | 409 |
| Fallo del proveedor o respuesta incompatible | 502 |
| Chat sin LLM_API_KEY | 503 |
