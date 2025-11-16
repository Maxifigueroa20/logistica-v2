package ar.edu.unju.fi.logistica.dto.vehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoSearchCriteria {
    private String patente;
    private Boolean refrigerado;
    private Double capacidadPesoMin;
    private Double capacidadPesoMax;
    private Double capacidadVolumenMin;
    private Double capacidadVolumenMax;
    private Double tempMin;
    private Double tempMax;
}
