package omoikane.inventarios.traspasoSaliente;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import omoikane.producto.Articulo;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: octavioruizcastillo
 * Date: 05/04/13
 * Time: 03:29
 * To change this template use File | Settings | File Templates.
 */
public class ItemTraspasoPropWrapper {

    private JavaBeanStringProperty codigo;
    private JavaBeanStringProperty nombre;
    private JavaBeanObjectProperty<BigDecimal> cantidad;
    private JavaBeanObjectProperty<Articulo> articulo;
    private JavaBeanObjectProperty<BigDecimal> stockDB;
    private JavaBeanObjectProperty<BigDecimal> precioPublico;
    private JavaBeanObjectProperty<BigDecimal> costoUnitario;

    private ItemTraspasoSaliente _itemConteo;
    static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ItemTraspasoPropWrapper.class);

    public ItemTraspasoPropWrapper(ItemTraspasoSaliente itemConteo) {
        _itemConteo = itemConteo;

        try {
            JavaBeanStringPropertyBuilder builder = JavaBeanStringPropertyBuilder.create();
            builder.bean(itemConteo);
            builder.name("codigo");
            codigo = builder.build();

            builder = JavaBeanStringPropertyBuilder.create();
            builder.bean(itemConteo);
            builder.name("nombre");
            nombre = builder.build();

            JavaBeanObjectPropertyBuilder<BigDecimal> builder1 = JavaBeanObjectPropertyBuilder.create();
            builder1.bean(itemConteo);
            builder1.name("cantidad");
            cantidad = builder1.build();

            builder1 = JavaBeanObjectPropertyBuilder.create();
            builder1.bean(itemConteo);
            builder1.name("stockDB");
            stockDB = builder1.build();

            builder1 = JavaBeanObjectPropertyBuilder.create();
            builder1.bean(itemConteo);
            builder1.name("costoUnitario");
            costoUnitario = builder1.build();

            builder1 = JavaBeanObjectPropertyBuilder.create();
            builder1.bean(itemConteo);
            builder1.name("precioPublico");
            precioPublico = builder1.build();

            JavaBeanObjectPropertyBuilder<Articulo> builder2 = JavaBeanObjectPropertyBuilder.create();
            builder2.bean(itemConteo);
            builder2.name("articulo");
            articulo = builder2.build();
        } catch (NoSuchMethodException e) {
            logger.error("Invalid method to wrap", e);
        }
    }

    public ItemTraspasoSaliente getBean() {
        return _itemConteo;
    }

    public StringProperty codigoProperty() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo.set(codigo);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public ObjectProperty<BigDecimal> cantidadProperty() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad.set(cantidad);
    }

    public ObjectProperty<BigDecimal> stockDBProperty() {
        return stockDB;
    }

    public void setStockDB(BigDecimal stockDB) {
        this.stockDB.set( stockDB );
    }

    public ObjectProperty<BigDecimal> precioPublicoProperty() {
        return precioPublico;
    }

    public void setPrecioPublico(BigDecimal precioPublico) {
        this.precioPublico.set( precioPublico );
    }

    public ObjectProperty<Articulo> articuloProperty() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo.set(articulo);
    }

    public ObjectProperty<BigDecimal> costoUnitarioProperty() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario.set( costoUnitario );
    }

}
