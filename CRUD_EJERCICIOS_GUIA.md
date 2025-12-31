# Guía del CRUD de Ejercicios

## 📁 Estructura de Archivos Creados/Modificados

### 1. **Modelo de Datos**

#### `Ejercicio.java` 
**Ubicación:** `app/src/main/java/com/example/practicaandroid/data/ejercicio/Ejercicio.java`
- Entidad Room con campos: `id`, `nombre`, `descripcion`, `tipo`
- Representa un ejercicio en la base de datos

#### `TipoEjercicio.java`
**Ubicación:** `app/src/main/java/com/example/practicaandroid/data/ejercicio/TipoEjercicio.java`
- Enum con los tipos: `FUERZA`, `CARDIO`, `FLEXIBILIDAD`
- Método `fromString()` para convertir texto a enum

#### `EjercicioDao.java`
**Ubicación:** `app/src/main/java/com/example/practicaandroid/data/ejercicio/EjercicioDao.java`
- DAO con operaciones CRUD:
  - `insert()` - Crear ejercicio
  - `update()` - Actualizar ejercicio
  - `delete()` - Eliminar ejercicio
  - `getAll()` - Obtener todos los ejercicios
  - `getById()` - Obtener por ID
  - `getByTipo()` - Filtrar por tipo

---

### 2. **Presentación (UI)**

#### `ExerciseFragment.java` ⭐
**Ubicación:** `app/src/main/java/com/example/practicaandroid/ExerciseFragment.java`
- Fragment principal que gestiona el CRUD de ejercicios
- Implementa `EjercicioAdapter.OnEjercicioClickListener`
- Métodos principales:
  - `cargarEjercicios()` - Carga la lista desde BD
  - `mostrarDialogoCrear()` - Diálogo para nuevo ejercicio
  - `crearEjercicio()` - Inserta en BD
  - `onEditarClick()` - Diálogo para editar
  - `actualizarEjercicio()` - Actualiza en BD
  - `onEliminarClick()` - Confirmación y eliminación
  - `eliminarEjercicio()` - Borra de BD

#### `EjercicioAdapter.java`
**Ubicación:** `app/src/main/java/com/example/practicaandroid/adapter/EjercicioAdapter.java`
- Adapter del RecyclerView
- ViewHolder con binding de datos
- Interface `OnEjercicioClickListener` para callbacks de editar/eliminar

---

### 3. **Layouts XML**

#### `fragment_exercise.xml`
**Ubicación:** `app/src/main/res/layout/fragment_exercise.xml`
- Layout principal del fragment
- Contiene:
  - TextView con título "Mis Ejercicios"
  - RecyclerView (`@+id/recyclerView`)
  - FloatingActionButton (`@+id/fab`)

#### `item_ejercicio.xml` ✨
**Ubicación:** `app/src/main/res/layout/item_ejercicio.xml`
- Layout de cada elemento en la lista
- CardView con:
  - `tvNombre` - Nombre del ejercicio
  - `tvTipo` - Tipo (Fuerza/Cardio/Flexibilidad)
  - `tvDescripcion` - Descripción
  - `btnEditar` - Botón editar
  - `btnEliminar` - Botón eliminar

#### `dialog_ejercicio.xml` 📝
**Ubicación:** `app/src/main/res/layout/dialog_ejercicio.xml`
- Diálogo para crear/editar ejercicios
- Campos:
  - `etNombre` - TextInputEditText para nombre
  - `spinnerTipo` - Spinner con tipos de ejercicio
  - `etDescripcion` - TextInputEditText para descripción

---

### 4. **Activity Alternativa (Opcional)**

#### `EjercicioActivity.java`
**Ubicación:** `app/src/main/java/com/example/practicaandroid/EjercicioActivity.java`
- Activity standalone con la misma funcionalidad
- Usar si prefieres Activity en lugar de Fragment

#### `activity_ejercicio.xml`
**Ubicación:** `app/src/main/res/layout/activity_ejercicio.xml`
- Layout similar al fragment pero como Activity completa

---

## 🔄 Flujo de Funcionamiento

```
Usuario → ExerciseFragment
          ↓
          Ver lista de ejercicios (RecyclerView)
          ↓
          ┌─────────────────────────────────┐
          │ FAB (+) → Crear Ejercicio       │
          │   ↓                             │
          │   Dialog (nombre, tipo, desc)   │
          │   ↓                             │
          │   Guardar → BD → Recargar lista │
          └─────────────────────────────────┘
          ┌─────────────────────────────────┐
          │ Botón Editar → Editar Ejercicio │
          │   ↓                             │
          │   Dialog pre-llenado            │
          │   ↓                             │
          │   Actualizar → BD → Recargar    │
          └─────────────────────────────────┘
          ┌─────────────────────────────────┐
          │ Botón Eliminar → Confirmación   │
          │   ↓                             │
          │   Borrar → BD → Recargar lista  │
          └─────────────────────────────────┘
```

---

## 🎯 Características Implementadas

✅ **Crear** ejercicios con nombre, tipo y descripción
✅ **Leer/Listar** todos los ejercicios en RecyclerView
✅ **Actualizar** ejercicios existentes
✅ **Eliminar** con diálogo de confirmación
✅ **Selector de tipos** (Fuerza, Cardio, Flexibilidad)
✅ **Validación** de campos obligatorios
✅ **UI moderna** con Material Design (CardView, FAB, TextInputLayout)
✅ **Operaciones asíncronas** con Executors
✅ **Integración** con navegación por tabs (BottomNavigationView)

---

## 📱 Navegación

El CRUD de ejercicios está integrado en el **ExerciseFragment** que se muestra al pulsar el botón "Ejercicios" en la barra de navegación inferior de `MainActivity`.

---

## 🔧 Configuración Adicional

### AndroidManifest.xml
Si usas `EjercicioActivity` en lugar del fragment, está registrada como:
```xml
<activity
    android:name=".EjercicioActivity"
    android:exported="false"
    android:label="Gestión de Ejercicios" />
```

### AppDatabase.java
La entidad `Ejercicio` y el DAO `EjercicioDao` ya están registrados en la base de datos.

---

## 💡 Próximos Pasos Sugeridos

1. **Filtros**: Agregar filtrado por tipo de ejercicio
2. **Búsqueda**: Implementar búsqueda por nombre
3. **Ordenamiento**: Permitir ordenar por nombre, tipo, etc.
4. **Detalles**: Activity/Fragment de detalles del ejercicio
5. **Imágenes**: Agregar imágenes ilustrativas
6. **Músculos**: Asociar ejercicios con grupos musculares
7. **Material**: Asociar ejercicios con material necesario

---

## 📝 Notas Importantes

- Todos los métodos de BD se ejecutan en hilos secundarios (Executors)
- Los updates de UI se hacen en el hilo principal (runOnUiThread)
- El pattern sigue el mismo estilo que Rutina y Sesión
- Los IDs de los layouts están correctamente definidos
- El spinner usa el enum TipoEjercicio para consistencia

---

**Autor:** Sistema de Entrenamiento Android
**Fecha:** 31/12/2025
**Versión:** 1.0

