package com.example.Fallapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("fallas")
public class Falla {

    @Id
    private String id;
    private Long objectid;
    private Long id_falla;
    private String nombre;
    private String seccion;
    private String fallera;
    private String presidente;
    private String artista;
    private String lema;
    private Integer anyo_fundacion;
    private String distintivo;
    private String boceto;
    private Integer experim;
    private GeoShape geo_shape;
    private Object geo_point_2d;

    // Relación ManyToOne con Artista (por id)
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public GeoShape getGeo_shape() {
        return geo_shape;
    }

    public void setGeo_shape(GeoShape geo_shape) {
        this.geo_shape = geo_shape;
    }

    public Object getGeo_point_2d() {
        return geo_point_2d;
    }

    public void setGeo_point_2d(Object geo_point_2d) {
        this.geo_point_2d = geo_point_2d;
    }

}

