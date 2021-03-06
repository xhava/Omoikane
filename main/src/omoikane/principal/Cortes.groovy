
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.ppppzzzzpp
 */

package omoikane.principal

import omoikane.formularios.CatalogoSucursal
import omoikane.principal.*
import omoikane.sistema.*
import groovy.sql.*;
import groovy.swing.*;
import javax.swing.*;
import java.awt.event.WindowListener;
import javax.swing.event.*;
import groovy.inspect.swingui.*;
import javax.swing.table.TableColumn
import java.awt.event.*
import java.text.NumberFormat;
import java.text.SimpleDateFormat
import groovy.swing.*
import omoikane.sistema.cortes.*;
import java.util.Calendar

import static omoikane.sistema.Usuarios.*;
import static omoikane.sistema.Permisos.*;

/**
 *
 * @author Adan
 */
class Cortes {

    static def lastMovID  = -1
    static def lastMovID2  = -1
    static def IDAlmacen = Principal.IDAlmacen
    static def escritorio = omoikane.principal.Principal.escritorio
    //static def getVenta(where) { new Venta(where)}

    static def lanzarCatalogo()
    {
        if(cerrojo(PMA_ABRIRCORTES)){
            def cat = (new omoikane.formularios.CatalogoCortesCaja())
            cat.setVisible(true);
            escritorio.getPanelEscritorio().add(cat)
            Herramientas.setColumnsWidth(cat.jTable1, [0.2,0.1,0.25,0.25,0.1,0.1]);
            Herramientas.panelCatalogo(cat)
            Herramientas.In2ActionX(cat, KeyEvent.VK_ESCAPE, "cerrar"   ) { cat.btnCerrar.doClick()   }
            Herramientas.In2ActionX(cat, KeyEvent.VK_F4    , "detalles" ) { cat.btnDetalles.doClick() }
            Herramientas.In2ActionX(cat, KeyEvent.VK_F8    , "imprimir" ) { cat.btnImprimir.doClick() }
            Herramientas.In2ActionX(cat, KeyEvent.VK_F7    , "corte" ) { cat.btnCorteDia.doClick() }
            Herramientas.In2ActionX(cat, KeyEvent.VK_F1    , "filtrar" ) { cat.btnFiltrar.doClick() }
            cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
            Herramientas.iconificable(cat)
            cat.toFront()
            try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo ventas", Herramientas.getStackTraceString(e)) }
            cat.txtBusqueda.requestFocus()
            return cat
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarCatalogoSuc()
    {
        if(cerrojo(PMA_ABRIRCORTES)){
            CatalogoSucursal cat = (new omoikane.formularios.CatalogoSucursal())
            cat.setVisible(true);
            escritorio.getPanelEscritorio().add(cat)
            Herramientas.setColumnsWidth(cat.jTable1, [0.2,0.8]);
            Herramientas.panelCatalogo(cat)

            cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
            Herramientas.iconificable(cat)
            cat.toFront()
            try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo ventas", Herramientas.getStackTraceString(e)) }
            cat.txtBusqueda.requestFocus()
            return cat
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarCatalogoMovimientos()
    {
        if(cerrojo(PMA_ABRIRCORTES)){
            def cat = (new omoikane.formularios.CatalogoMovimientosCaja())
            cat.setVisible(true);
            escritorio.getPanelEscritorio().add(cat)
            Herramientas.setColumnsWidth(cat.jTable1, [0.2,0.1,0.1,0.4,0.2]);
            Herramientas.panelCatalogo(cat)
            Herramientas.In2ActionX(cat, KeyEvent.VK_ESCAPE, "cerrar"   ) { cat.btnCerrar.doClick()   }
            Herramientas.In2ActionX(cat, KeyEvent.VK_F4    , "detalles" ) { cat.btnDetalles.doClick() }
            Herramientas.In2ActionX(cat, KeyEvent.VK_F3    , "filtrar" ) { cat.btnFiltrar.doClick() }
            cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
            Herramientas.iconificable(cat)
            cat.toFront()
            try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo ventas", Herramientas.getStackTraceString(e)) }
            cat.txtBusqueda.requestFocus()
            return cat
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarVentanaDetalles() {
        if(cerrojo(PMA_DETALLESVENTAS)){
            def form = (new omoikane.formularios.CorteCajaDetalles())
            form.setVisible(true);
            Herramientas.panelFormulario(form)
            form.btnImprimir.actionPerformed = {lanzarImprimirCorte(form.ID as Long)}
            escritorio.getPanelEscritorio().add(form)
            Herramientas.iconificable(form)
            form.toFront()
            try { form.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario nuevo movimiento de almacén", Herramientas.getStackTraceString(e)) }
            return form
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarDetalles(IDE)
    {
        if(cerrojo(PMA_DETALLESCORTES)){
            lastMovID= IDE
            def form  = lanzarVentanaDetalles()
            def puerto = Nadesico.conectar()
            def mov  = puerto.getCorteWhere(" cortes.id_corte=$IDE")
            NumberFormat mf = NumberFormat.getCurrencyInstance();
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            nf.setGroupingUsed(true);
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            String where
            where = " folio_inicial = '${mov.folio_inicial}' AND folio_final = '${mov.folio_final}' AND id_caja =  ${mov.id_caja} "
            if(corteDualActivo()) {
                def mov2  = puerto.getCorteWhereFrom(where," cortes_dual ")
                lastMovID2=mov2.id_corte
            }
            form.ID = IDE
            form.setTxtDescuento     nf.format ( mov.descuentos )
            form.setTxtDesde         (sdf.format(mov.desde) as String)
            form.setTxtHasta         (sdf.format(mov.hasta) as String)
            form.setTxtIDAlmacen     (mov.id_almacen as String)
            form.setTxtIDCaja        (mov.id_caja as String)
            form.setTxtIDCorte       (mov.id_corte as String)
            form.setTxtImpuesto      nf.format ( mov.impuestos )
            form.setTxtNumeroVenta   (mov.n_ventas as String)
            form.setTxtSubtotal      nf.format ( mov.subtotal )
            form.setTxtTotal         mf.format ( mov.total )
            form.setTxtDeposito      nf.format ( mov.depositos )
            form.setTxtRetiro        nf.format ( mov.retiros )
            form.setTxtEfectivo      mf.format ( mov.total + mov.depositos - mov.retiros )
            return form
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarImprimir(queryMovs)
    {
        def reporte = new Reporte('omoikane/reportes/ReporteCortesCaja.jasper', [QueryTxt:queryMovs]);
        reporte.lanzarPreview()
    }

    static def lanzarImprimirCorte(ID)
    {
        def comprobante = new Comprobantes()
        comprobante.Corte(ID as Long)//imprimir ticket
        comprobante.probar()//imprimir ticket
        if(corteDualActivo()) {
            Thread.sleep(6000)
            comprobante.CorteLegacy(lastMovID2 as Long, "cortes_dual") //imprimir corte
            comprobante.probar()//imprimir ticket
        }
    }

    static def corteDualActivo() {
        return (Principal.tipoCorte == cortes.ContextoCorte.TIPO_DUAL);
    }

    static def lanzarVentanaCorteSucursal(resultadoCorte,IDAlmacen, IDCorte) {
        if(cerrojo(PMA_TOTALVENTASUCURSAL)) {
            NumberFormat mf = NumberFormat.getCurrencyInstance();
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            nf.setGroupingUsed(true);
            
            def form = (new omoikane.formularios.CorteSucursalDetalles())
            def rc   = resultadoCorte
            Herramientas.panelFormulario(form)
            form.setVisible(true);
            escritorio.getPanelEscritorio().add(form)
            Herramientas.iconificable(form)
            form.toFront()
            try { form.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario nuevo movimiento de almacén", Herramientas.getStackTraceString(e)) }
            form.txtNVentas.text   = rc.n_ventas
            form.txtImpuestos.text = nf.format ( rc.impuestos  )
            form.txtDescuentos.text= nf.format ( rc.descuentos )
            form.txtSubtotal.text  = nf.format ( rc.subtotal   )
            form.txtTotal.text     = mf.format ( rc.total      )
            form.txtRetiros.text             = nf.format ( rc.retiros   )
            form.txtDepositos.text           = nf.format ( rc.depositos )
            form.btnAceptar.actionPerformed  = { form.dispose() }
            form.btnImprimir.actionPerformed = {

                //def comprobante = new Comprobantes()
                //(comprobante.CorteSucursal(IDAlmacen, IDCorte))//imprimir ticket
                //(comprobante.probar())//* Aqui tambien mandar a imprimir*/

                def cortes = ContextoCorte.instanciar();
                cortes.imprimirCorteSucursal(IDAlmacen, IDCorte)
                }
            
            return form
            
        } else { Dialogos.lanzarAlerta("Acceso Denegado") }

    }
    static def lanzarCorteSucursal(IDAlmacen, cortar=false)
    {
        if(cerrojo(PMA_TOTALVENTASUCURSAL)) {
            def paso = 1
            Sucursales sucursalesHelper = new Sucursales();
            switch(paso) {
                case 1:
                    //if(sucursalesHelper.existe(IDAlmacen)) { paso = 2 } else { Dialogos.lanzarAlerta("Sucursal inválida, probablemente sea error de configuración!"); break; }
                    //break;
                case 2:
                    def abierta = sucursalesHelper.abierta()
                    if(abierta==1) { paso = 3 } else { Dialogos.lanzarAlerta("Sucursal inhabilitada, no se han iniciado ventas o hay un corte pendiente."); break }
                case 3:
                    def cajasCerradas = sucursalesHelper.cajasSucursalCerradas(IDAlmacen)
                    if(cajasCerradas) { paso = 4 } else { Dialogos.lanzarAlerta("No se puede continuar. Hay cajas abiertas, debe cerrarlas para continuar."); break }
                case 4:
                    sucursalesHelper.cerrar(IDAlmacen)

                    def cortes = ContextoCorte.instanciar()
                    def IDCorte        = cortes.hacerCorteSucursal(IDAlmacen)
                    def resultadoCorte = cortes.obtenerSumaSucursal(IDAlmacen, IDCorte)
                    lanzarVentanaCorteSucursal(resultadoCorte,IDAlmacen, IDCorte)
                    // Aquí mandar a imprimir resultadoCorte (también agregar imprimir en la función anterior (lanzarVentanaCorteSucursal))
                break
            }
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarDetallesCorteSucursal(IDAlmacen, id)
    {
        if(cerrojo(PMA_TOTALVENTASUCURSAL)) {
                    def cortes = ContextoCorte.instanciar()
                    def resultadoCorte = cortes.obtenerSumaSucursal(IDAlmacen, id)
                    lanzarVentanaCorteSucursal(resultadoCorte,IDAlmacen, id)
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }


}

