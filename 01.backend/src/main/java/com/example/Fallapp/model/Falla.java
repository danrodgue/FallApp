package com.example.Fallapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "fallas")
public class Falla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_falla")
    private Long idFalla;
    
    @Column(name = "objectid")
    private Long objectid;
    
    @Column(name = "id_falla", insertable = false, updatable = false)
    private Long id_falla;
    
    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;
    
    @Column(name = "seccion", nullable = false, length = 5)
    private String seccion;
    
    @Column(name = "fallera")
    private String fallera;
    
    @Column(name = "presidente", nullable = false)
    private String presidente;
    
    @Column(name = "artista")
    private String artista;
    
    @Column(name = "lema", columnDefinition = "TEXT")
    private String lema;
    
    @Column(name = "anyo_fundacion", nullable = false)
    private Integer anyo_fundacion;
    
    @Column(name = "distintivo", length = 100)
    private String distintivo;
    
    @Column(name = "url_boceto", length = 500)
    private String boceto;
    
    @Column(name = "experim", nullable = false)
    private Integer experim;
    
    @Transient
    private GeoShape geo_shape;
    
    @Transient
    private Object geo_point_2d;

    // Relación ManyToOne con Artista (por id)
    @Transient
    private String artistaId;

    public Falla() {
    }

    public Falla(Long objectid,
                 Long id_Falla,
                 String nombre,
                 String seccion,
                 String fallera,
                 String presidente,
                 String artista,
                 String lema,
                 Integer anyo_fundacion,
                 String distintivo,
                 String boceto,
                 Integer experim,
                 GeoShape geo_shape,
                 Object geo_location) {
        this.objectid = objectid;
        this.id_falla = id_Falla;
        this.idFalla = id_Falla;
        this.nombre = nombre;
        this.seccion = seccion;
        this.fallera = fallera;
        this.presidente = presidente;
        this.artista = artista;
        this.lema = lema;
        this.anyo_fundacion = anyo_fundacion;
        this.distintivo = distintivo;
        this.boceto = boceto;
        this.experim = experim;
        this.geo_shape = geo_shape;
        this.geo_point_2d = geo_location;
    }

    public Long getIdFalla() {
        return idFalla;
    }

    public void setIdFalla(Long idFalla) {
        this.idFalla = idFalla;
    }

    public String getId() {
        return idFalla != null ? idFalla.toString() : null;
    }

    public void setId(String id) {
        if (id != null) {
            try {
                this.idFalla = Long.parseLong(id);
            } catch (NumberFormatException e) {
                // Ignore if id is not a valid Long
            }
        }
    }

    public Long getObjectid() {
        return objectid;
    }

    public void setObjectid(Long objectid) {
        this.objectid = objectid;
    }

    public Long getId_falla() {
        return id_falla;
    }

    public void setId_falla(Long id_falla) {
        this.id_falla = id_falla;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public String getFallera() {
        return fallera;
    }

    public void setFallera(String fallera) {
        this.fallera = fallera;
    }

    public String getPresidente() {
        return presidente;
    }

    public void setPresidente(String presidente) {
        this.presidente = presidente;
    }

    public String getArtistaNombre() {
        return artista;
    }

    public void setArtistaNombre(String artistaNombre) {
        this.artista = artistaNombre;
    }

    public String getLema() {
        return lema;
    }

    public void setLema(String lema) {
        this.lema = lema;
    }

    public Integer getAnyo_fundacion() {
        return anyo_fundacion;
    }

    public void setAnyo_fundacion(Integer anyo_fundacion) {
        this.anyo_fundacion = anyo_fundacion;
    }

    public String getDistintivo() {
        return distintivo;
    }

    public void setDistintivo(String distintivo) {
        this.distintivo = distintivo;
    }

    public String getBoceto() {
        return boceto;
    }

    public void setBoceto(String boceto) {
        this.boceto = boceto;
    }

    public Integer getExperim() {
        return experim;
    }

    public void setExperim(Integer experim) {
        this.experim = experim;
    }

    @JsonIgnore
    public GeoShape getGeo_shape() {
        return geo_shape;
    }

    public void setGeo_shape(GeoShape geo_shape) {
        this.geo_shape = geo_shape;
    }

    @JsonIgnore
    public Object getGeo_point_2d() {
        return geo_point_2d;
    }

    public void setGeo_point_2d(Object geo_point_2d) {
        this.geo_point_2d = geo_point_2d;
    }

}

