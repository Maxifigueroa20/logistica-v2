# 🧱 Convenciones de diseño de DTOs – Proyecto Logística

Este documento describe las normas y criterios aplicados en el diseño de los DTOs (`Data Transfer Objects`) del sistema logístico, para asegurar legibilidad, consistencia y facilidad de mantenimiento.

---

## 🎯 Objetivo
Los DTOs son estructuras simples usadas para transferir datos entre capas (Controller ⇄ Service ⇄ Repository) y entre la aplicación y clientes externos (API REST).  
**No deben contener lógica de negocio** ni dependencias hacia entidades JPA.

---

## 🧩 Estructura general

| Tipo de DTO | Nombre | Uso | Mutabilidad | Anotaciones principales |
|--------------|--------|-----|--------------|--------------------------|
| **CreateDTO** | `ClienteCreateDTO`, `EnvioCreateDTO`, `VehiculoCreateDTO`, etc. | Entrada de datos (alta o edición) | ✅ Mutable | `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor` |
| **DTO** | `ClienteDTO`, `EnvioDTO`, `RutaDTO`, etc. | Salida de datos (lectura) | ❌ Inmutable | `@Value`, `@Builder(toBuilder = true)` |
| **Subtipo** | `PaqueteFragilDTO`, `PaqueteRefrigeradoDTO` | Herencia de DTO base | ❌ Inmutable | `@SuperBuilder`, `@EqualsAndHashCode(callSuper = true)` |

---

## ⚙️ Principios de diseño

1. **Inmutabilidad por defecto**
   - Los DTOs de salida son inmutables para evitar efectos colaterales.
   - Lombok `@Value` convierte todos los campos en `private final` y genera solo *getters*.

2. **DTOs de creación son mutables**
   - Los CreateDTOs permiten deserialización desde JSON (`@RequestBody`) gracias a los setters.

3. **Validaciones**
   - Se aplican validaciones declarativas con **Jakarta Validation** (`@NotBlank`, `@Positive`, `@Email`, etc.).
   - Las reglas cruzadas o complejas (por ejemplo, “si es refrigerado, validar rango de temperatura”) se validan en la capa de servicio.

4. **Builders**
   - Todos los DTOs implementan `@Builder` o `@SuperBuilder` para mejorar la legibilidad en tests, seeds y mappers MapStruct.

5. **Herencia**
   - En jerarquías como `Paquete → PaqueteFragil / PaqueteRefrigerado`, se usa `@SuperBuilder` y `@EqualsAndHashCode(callSuper = true)` para mantener consistencia con la herencia JOINED en el dominio.

6. **Campos calculados**
   - Algunos DTOs poseen campos derivados o no persistidos (por ejemplo, `trackingCode` en `PaqueteDTO` o `requiereFrio` en `EnvioDTO`).

7. **MapStruct**
   - Los mappers convierten entre entidades y DTOs automáticamente.
   - Los DTOs nunca deben tener dependencias hacia entidades (`domain`).

---

## 🧠 Ejemplo

### Entrada (CreateDTO)
```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoCreateDTO {
    @NotBlank
    private String patente;
    @Positive
    private double capacidadPesoKg;
    @Positive
    private double capacidadVolumenDm3;
    @NotNull
    private Boolean refrigerado;
    private Double rangoTempMin;
    private Double rangoTempMax;
}
