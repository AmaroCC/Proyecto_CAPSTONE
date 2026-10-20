package cl.rincon.dualista.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol;

    private boolean verificado = false;

    private String tokenConfirmacion;

    public Usuario() {}

    public Usuario(String username, String email, String password, String rol, boolean verificado, String tokenConfirmacion) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.verificado = verificado;
        this.tokenConfirmacion = tokenConfirmacion;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }
    public String getTokenConfirmacion() { return tokenConfirmacion; }
    public void setTokenConfirmacion(String tokenConfirmacion) { this.tokenConfirmacion = tokenConfirmacion; }
}