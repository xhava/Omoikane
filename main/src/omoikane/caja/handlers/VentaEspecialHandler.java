package omoikane.caja.handlers;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import omoikane.caja.business.plugins.PluginManager;
import omoikane.caja.business.plugins.VentaEspecialPlugin;
import omoikane.caja.presentation.CajaController;
import omoikane.caja.presentation.ProductoModel;
import omoikane.nadesicoiLegacy.Ventas;
import omoikane.sistema.Usuarios;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Agrega el Plugin de Venta Especial cuando es necesario
 * Created with IntelliJ IDEA.
 * User: octavioruizcastillo
 * Date: 09/12/12
 * Time: 20:49
 * To change this template use File | Settings | File Templates.
 */
public class VentaEspecialHandler extends ICajaEventHandler {
    public static Logger logger = Logger.getLogger(CancelarVenta.class);
    VentaKBHandler ventaKBHandler;

    public VentaEspecialHandler(CajaController controller) {
        super(controller);
        ventaKBHandler = new VentaKBHandler();
        getController().getPrecioVentaColumn().setOnEditCommit( new PrecioCellEditHandler() );
        Callback<TableColumn, TableCell> cellFactory =
                new Callback<TableColumn, TableCell>() {
                    public TableCell call(TableColumn p) {
                        return new EditingCell();
                    }
                };
        getController().getPrecioVentaColumn().setCellFactory( cellFactory );
    }

    @Override
    public void handle(Event event) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                modoEspecial();
            }
        });
    }

    public void modoEspecial() {
        try {
            if(Usuarios.autentifica(Usuarios.GERENTE)) {

                getController().getVentaTableView().setEditable(true);
                getController().setCapturaPaneDisable(true);
                getController().setMainToolBarDisable(true);
                getController().showHud("Ahora puede cambiar los precios\n[Esc] para terminar modificaciones");
                getController().getVentaTableView().requestFocus();
                getController().getVentaTableView().onKeyReleasedProperty().set(ventaKBHandler);

                // ** Agrega plugin para registrar ventaEspecial al finalizar venta **
                PluginManager pm = getController().getCajaLogic().getPluginManager();
                // Revisa si el plugin ya fue agregado a ésta venta, si no, lo agrega
                if(!pm.exists(VentaEspecialPlugin.class)) pm.registerPlugin(new VentaEspecialPlugin(getController()));

            } else {
                logger.info("Acceso denegado");
                getController().getVentaTableView().requestFocus();
                getController().getCapturaTextField().requestFocus();

            }
        } catch (Exception e) {
            logger.error("Error al habilitar venta especial", e);
        }


    }

    public void modoNormal() {
        getController().getVentaTableView().setEditable(false);
        getController().setCapturaPaneDisable(false);
        getController().setMainToolBarDisable(false);
        getController().hideHud();
        getController().getVentaTableView().onKeyReleasedProperty().set(null);
        getController().getCapturaTextField().requestFocus();
    }

    /** Cambia el precio de un producto en un solo renglón (fila) y solo durante la venta actual, el usuario captura el precio que
     *  desea que tenga el producto, esto incluye impuestos y descuentos, por lo que es necesario recalcular el precioBase, impuestos y descuentos.
     *
     *  Aún no soporta más de un impuesto en cada producto
     *
     */
    public class PrecioCellEditHandler implements EventHandler<TableColumn.CellEditEvent<ProductoModel, String>> {
        public void handle(TableColumn.CellEditEvent<ProductoModel, String> t) {

            DecimalFormat df = (DecimalFormat) NumberFormat.getCurrencyInstance();
            df.setParseBigDecimal(true);
            BigDecimal nuevoPrecio = new BigDecimal(0);

            try {
                nuevoPrecio = (BigDecimal) df.parse(t.getNewValue());
                ProductoModel pm = t.getTableView().getSelectionModel().getSelectedItem();

                //Determina el porcentaje de impuestos, aún no soporta más de un impuesto en cada producto
                BigDecimal impuestosPorc;
                switch( pm.getProductoData().getImpuestos().size() ) {
                    case 0:
                        impuestosPorc = new BigDecimal(0d); break;
                    case 1:
                        impuestosPorc = pm.getProductoData().getImpuestos().iterator().next().getPorcentaje(); break;
                    default:
                        throw new UnsupportedOperationException("No está soportado modificar el precio de artículos con más de un impuesto");
                }

                BigDecimal descuentoPorc = pm.getPorcentajeDescuentoTotal();
                BigDecimal cien         = new BigDecimal("100");
                BigDecimal uno          = new BigDecimal("1");

                BigDecimal nuevoPrecioBase    = calcularNuevoPrecioBase(nuevoPrecio, t.getRowValue());
                BigDecimal nuevoDescuentoBase = nuevoPrecioBase.multiply ( descuentoPorc.divide( cien, 4, BigDecimal.ROUND_HALF_EVEN ) );
                BigDecimal nuevoImpuestoBase  = nuevoPrecioBase.subtract(nuevoDescuentoBase).multiply ( impuestosPorc.divide( cien, 4, BigDecimal.ROUND_HALF_EVEN ) );

                if(pm.impuestosProperty().size() > 0) pm.impuestosProperty().get(0).setImpuestoBase( nuevoImpuestoBase );
                pm.descuentosBaseProperty().set( nuevoDescuentoBase );
                pm.precioBaseProperty()    .set( nuevoPrecioBase );
                pm.precioProperty()        .set( nuevoPrecio );

                getController().getCajaLogic().onVentaListChanged(getController().getModel());
            } catch (UnsupportedOperationException e) {
                logger.info(e.getMessage());
            } catch (ParseException e) {
                logger.error("Precio mal escrito", e);
            }

        }

        /** Calcula el nuevo -precio base- de un producto en base a un -precio- dado.<br>
         * factorX = ((impuestos - descuentos) * (100 / precioTotalOriginal))/100 <br>
         * nuevoPrecioBase = nuevoPrecio - (nuevoPrecio * factorX)<br>
         *
         * <p>FactorX representa una proporción constante de impuestos y descuentos en el producto sin importar el precio</p>
         *
         * <p>Éste método podría quedar mejor en OmoikanPrecioLogic</p>
         * @param nuevoPrecio Nuevo precio que incluye impuestos y descuentos
         * @param producto Producto al que se le cambió el precio
         */
        private BigDecimal calcularNuevoPrecioBase(BigDecimal nuevoPrecio, ProductoModel producto) {
            BigDecimal cien         = new BigDecimal("100");
            BigDecimal uno          = new BigDecimal("1");
            BigDecimal impuestoBase = producto.impuestosProperty().size() > 0 ? producto.impuestosProperty().get(0).getImpuestoBase() : new BigDecimal("0") ;
            BigDecimal factorX_A       = impuestoBase.subtract(producto.getDescuentosBase());
            BigDecimal factorX_B       = cien.divide(producto.precioProperty().get(), 4, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal factorX         = factorX_A .multiply( factorX_B );
            factorX                    = factorX.divide(cien, 4, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal nuevoPrecioBase = nuevoPrecio.subtract( nuevoPrecio.multiply( factorX ) );

            return nuevoPrecioBase;

        }
    }

    public class VentaKBHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            CajaController controller = VentaEspecialHandler.this.getController();

            if(event.getCode() == KeyCode.ESCAPE) {
                VentaEspecialHandler.this.modoNormal();
            }
        }
    }

    class EditingCell extends TableCell<ProductoModel, String> {

        private TextField textField;

        public EditingCell() {}

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }

            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getString());
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

}
