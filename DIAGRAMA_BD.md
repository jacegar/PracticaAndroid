# 📊 DIAGRAMA DE RELACIONES - Base de Datos

## Entidades y Relaciones

```
┌─────────────┐
│   RUTINA    │
│   id        │
│   nombre    │
│   descripcion│
│   fechaCreacion│
└──────┬──────┘
       │ 1
       │ tiene muchas
       │ M
       ▼
┌─────────────┐        M ┌──────────────────┐ M        ┌──────────────┐
│   SESION    │◄────────►│ SESION_EJERCICIO │◄────────►│  EJERCICIO   │
│   id        │          │ id               │          │  id          │
│   rutinaId  │          │ sesionId         │          │  nombre      │
│   nombre    │          │ ejercicioId      │          │  descripcion │
│diaPlanificado│         │ series          │          │  tipo        │
│fechaRealizada│         │ repeticiones    │          └──────┬───────┘
└─────────────┘          │ peso            │                 │
                         │ duracionSegundos│                 │ M
                         │ distanciaKm     │                 │
                         │ orden           │                 │
                         │ completado      │                 │
                         └──────────────────┘                 │
                                                              │
                         ┌──────────────────┐                │
                         │EJERCICIO_MUSCULO │◄───────────────┘
                    ┌───►│ id               │
                    │    │ ejercicioId      │
                    │    │ musculoId        │
                    │    │ intensidad       │
                    │    └──────────────────┘
                    │
                    │ M
          ┌─────────┴──┐
          │  MUSCULO   │
          │  id        │
          │  nombre    │
          │  grupo     │
          └────────────┘


                         ┌───────────────────┐
                         │EJERCICIO_MATERIAL │
                    ┌───►│ id                │
                    │    │ ejercicioId       │
                    │    │ materialId        │
                    │    │ obligatorio       │
                    │    └───────────────────┘
                    │                ▲
                    │ M              │ M
          ┌─────────┴──┐            │
          │ EJERCICIO  │────────────┘
          └────────────┘
                    ▲
                    │
                    │ M
          ┌─────────┴──┐
          │ MATERIAL   │
          │ id         │
          │ nombre     │
          │ descripcion│
          └────────────┘
```

## Relaciones Implementadas

### ✅ 1 → M (Uno a Muchos)
**Rutina → Sesiones**
- 1 Rutina tiene muchas Sesiones
- 1 Sesión pertenece a 1 Rutina
- Foreign Key: `Sesion.rutinaId`

### ✅ M → M (Muchos a Muchos)

**Sesión ↔ Ejercicio** (con datos de progreso)
- Tabla intermedia: `SesionEjercicio`
- Campos extra: series, reps, peso, duración, distancia

**Ejercicio ↔ Músculo**
- Tabla intermedia: `EjercicioMusculo`
- Campo extra: intensidad (PRINCIPAL/SECUNDARIO)

**Ejercicio ↔ Material**
- Tabla intermedia: `EjercicioMaterial`
- Campo extra: obligatorio (true/false)

## Campos Clave por Funcionalidad

### 🔔 Planificación y Notificaciones
- `Sesion.diaPlanificado` - Timestamp cuando está programada
- Query: `SesionDao.getSesionesEnRango(inicio, fin)`

### 📈 Seguimiento de Progreso
- `SesionEjercicio.peso` - Kg levantados
- `SesionEjercicio.series` - Número de series
- `SesionEjercicio.repeticiones` - Reps por serie
- `SesionEjercicio.duracionSegundos` - Para cardio
- `SesionEjercicio.distanciaKm` - Para cardio
- Queries: `getProgresoPeso()`, `getProgresoCardio()`, `getFrecuenciaEjercicio()`

### ✅ Estado de Sesiones
- `Sesion.fechaRealizada` - 0 = no completada, timestamp = completada
- Query: `SesionDao.getSesionesCompletadas()`

## Total de Tablas: 8

**Principales:** 5
1. rutinas
2. sesiones
3. ejercicios
4. musculos
5. materiales

**Intermedias:** 3
6. sesion_ejercicio
7. ejercicio_musculo
8. ejercicio_material

