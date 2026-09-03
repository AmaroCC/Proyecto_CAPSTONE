package cl.rincon.dualista.modelo;

public class RespuestaCartaDTO {
    private String nombreCarta;
    private String estadoCarta;
    private double precioUsd;
    private double dolarObservado;
    private int precioClp;
    private String imagenBase64;

    public RespuestaCartaDTO() {}

    public RespuestaCartaDTO(String nombreCarta, String estadoCarta, double precioUsd, double dolarObservado, int precioClp, String imagenBase64) {
        this.nombreCarta = nombreCarta;
        this.estadoCarta = estadoCarta;
        this.precioUsd = precioUsd;
        this.dolarObservado = dolarObservado;
        this.precioClp = precioClp;
        this.imagenBase64 = imagenBase64;
    }

    // Getters y Setters
    public String getNombreCarta() { return nombreCarta; }
    public void setNombreCarta(String nombreCarta) { this.nombreCarta = nombreCarta; }

    public String getEstadoCarta() { return estadoCarta; }
    public void setEstadoCarta(String estadoCarta) { this.estadoCarta = estadoCarta; }

    public double getPrecioUsd() { return precioUsd; }
    public void setPrecioUsd(double precioUsd) { this.precioUsd = precioUsd; }

    public double getDolarObservado() { return dolarObservado; }
    public void setDolarObservado(double dolarObservado) { this.dolarObservado = dolarObservado; }

    public int getPrecioClp() { return precioClp; }
    public void setPrecioClp(int precioClp) { this.precioClp = precioClp; }

    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
}
