package cl.rincon.dualista.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "cartas")
public class RespuestaCartaDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String juego;
    private String rareza;
    private String estado;
    private double precioUSD;
    private double valorDolarActual;
    private int precioCLP;

    @Column(columnDefinition = "TEXT")
    private String imagenUrl;

    public RespuestaCartaDTO() {}

    public RespuestaCartaDTO(String nombre, String juego, String rareza, String estado,
                             double precioUSD, double valorDolarActual, int precioCLP, String imagenUrl) {
        this.nombre = nombre;
        this.juego = juego;
        this.rareza = rareza;
        this.estado = estado;
        this.precioUSD = precioUSD;
        this.valorDolarActual = valorDolarActual;
        this.precioCLP = precioCLP;
        this.imagenUrl = imagenUrl;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getJuego() { return juego; }
    public String getRareza() { return rareza; }
    public String getEstado() { return estado; }
    public double getPrecioUSD() { return precioUSD; }
    public double getValorDolarActual() { return valorDolarActual; }
    public int getPrecioCLP() { return precioCLP; }
    public String getImagenUrl() { return imagenUrl; }
}