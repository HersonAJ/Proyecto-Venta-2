import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VentasService, Venta, ReporteVentasResponse, EstadisticasVentasResponse, VentaPorDia } from '../servicios/ventas-service';
import { AuthService } from '../servicios/auth-service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-reporte-ventas-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './reporte-ventas-component.html',
  styleUrl: './reporte-ventas-component.scss',
})
export class ReporteVentasComponent implements OnInit {
  // Lista de ventas
  ventas: Venta[] = [];
  ventasFiltradas: Venta[] = [];
  
  // Estadísticas
  ventasPorDia: VentaPorDia[] = [];
  totalGeneral: number = 0;
  totalRegistros: number = 0;
  
  // Estados y flags
  isLoading: boolean = true;
  isError: boolean = false;
  errorMessage: string = '';
  
  // Filtros
  fechaInicio: string = '';
  fechaFin: string = '';
  modoVista: 'hoy' | 'rango' | 'mes' = 'hoy';
  
  // Paginación
  paginaActual: number = 1;
  limitePorPagina: number = 10;
  totalPaginas: number = 0;
  hayMasPaginas: boolean = false;
  
  // Búsqueda
  terminoBusqueda: string = '';
  
  // Ordenamiento
  ordenCampo: string = 'fecha_venta';
  ordenDireccion: 'asc' | 'desc' = 'desc';
  
  // Información del usuario
  esAdmin: boolean = false;
  esTrabajador: boolean = false;

  constructor(
    private ventasService: VentasService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.verificarPermisos();
    this.inicializarFechas();
    this.cargarVentas();
  }

  /**
   * Verifica los permisos del usuario actual
   */
  private verificarPermisos() {
    this.esAdmin = this.authService.isAdmin();
    this.esTrabajador = this.authService.isTrabajador();
    
    // Por defecto, trabajadores solo ven ventas del día
    if (this.esTrabajador && !this.esAdmin) {
      this.modoVista = 'hoy';
    }
  }

  /**
   * Inicializa las fechas por defecto
   */
  private inicializarFechas() {
    const hoy = new Date();
    const primerDiaMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    
    // Fecha de inicio: primer día del mes
    this.fechaInicio = this.formatearFecha(primerDiaMes);
    // Fecha de fin: hoy
    this.fechaFin = this.formatearFecha(hoy);
  }

  /**
   * Carga las ventas según el modo de vista
   */
  cargarVentas(pagina: number = 1) {
    this.isLoading = true;
    this.isError = false;
    this.paginaActual = pagina;

    let peticion;

    switch (this.modoVista) {
      case 'hoy':
        // Trabajadores y admin: ventas del día actual
        peticion = this.ventasService.obtenerVentasDelDia(pagina, this.limitePorPagina);
        break;
        
      case 'mes':
        // Solo admin: ventas del mes actual
        if (this.esAdmin) {
          peticion = this.ventasService.obtenerVentasDelMesActual(pagina, this.limitePorPagina);
        } else {
          this.manejarError('No tiene permisos para ver ventas del mes');
          return;
        }
        break;
        
      case 'rango':
        // Solo admin: ventas por rango de fechas
        if (this.esAdmin) {
          // Validar fechas
          if (!this.fechaInicio || !this.fechaFin) {
            this.manejarError('Debe seleccionar un rango de fechas válido');
            return;
          }
          
          if (new Date(this.fechaInicio) > new Date(this.fechaFin)) {
            this.manejarError('La fecha de inicio no puede ser mayor a la fecha de fin');
            return;
          }
          
          peticion = this.ventasService.obtenerReporteVentas(
            this.fechaInicio, 
            this.fechaFin, 
            pagina, 
            this.limitePorPagina
          );
        } else {
          this.manejarError('No tiene permisos para filtrar por fechas');
          return;
        }
        break;
    }

    if (peticion) {
      peticion
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: (response: ReporteVentasResponse) => this.procesarRespuestaVentas(response),
          error: (error) => this.manejarError('Error al cargar ventas', error)
        });
    }
  }

  /**
   * Procesa la respuesta del servicio de ventas
   */
  private procesarRespuestaVentas(response: ReporteVentasResponse) {
    if (response.success) {
      this.ventas = response.ventas;
      this.ventasFiltradas = [...this.ventas];
      this.totalRegistros = response.totalRegistros;
      this.totalGeneral = response.totalGeneral;
      this.ventasPorDia = response.ventasPorDia || [];
      
      // Información de paginación
      this.paginaActual = response.paginaActual || 1;
      this.totalPaginas = response.totalPaginas || 1;
      this.hayMasPaginas = response.hayMasPaginas || false;
      
      // Aplicar filtro de búsqueda si existe
      if (this.terminoBusqueda) {
        this.aplicarFiltroBusqueda();
      }
      
      // Aplicar ordenamiento
      this.ordenarVentas();
    } else {
      this.manejarError('No se pudieron cargar las ventas: ' + (response.message || 'Error desconocido'));
    }
  }

  /**
   * Maneja errores de las peticiones
   */
  private manejarError(mensaje: string, error?: any) {
    this.isError = true;
    this.errorMessage = mensaje;
    console.error(mensaje, error);
    this.isLoading = false;
  }

  /**
   * Cambia el modo de vista y recarga las ventas
   */
  cambiarModoVista(modo: 'hoy' | 'rango' | 'mes') {
    this.modoVista = modo;
    this.paginaActual = 1;
    this.cargarVentas(1);
  }

  /**
   * Aplica el filtro por rango de fechas
   */
  aplicarFiltroFechas() {
    if (this.modoVista === 'rango') {
      this.paginaActual = 1;
      this.cargarVentas(1);
    }
  }

  /**
   * Aplica el filtro de búsqueda en tiempo real
   */
  aplicarFiltroBusqueda() {
    if (!this.terminoBusqueda.trim()) {
      this.ventasFiltradas = [...this.ventas];
    } else {
      const terminoLower = this.terminoBusqueda.toLowerCase();
      this.ventasFiltradas = this.ventas.filter(venta =>
        venta.cliente_nombre.toLowerCase().includes(terminoLower) ||
        venta.trabajador_nombre.toLowerCase().includes(terminoLower) ||
        venta.descripcion.toLowerCase().includes(terminoLower) ||
        venta.pedido_id.toString().includes(terminoLower)
      );
    }
  }

  /**
   * Cambia la cantidad de ventas por página
   */
  cambiarLimitePorPagina() {
    this.paginaActual = 1;
    this.cargarVentas(1);
  }

  /**
   * Cambia el campo de ordenamiento
   */
  cambiarOrden(campo: string) {
    if (this.ordenCampo === campo) {
      this.ordenDireccion = this.ordenDireccion === 'asc' ? 'desc' : 'asc';
    } else {
      this.ordenCampo = campo;
      this.ordenDireccion = 'desc';
    }
    this.ordenarVentas();
  }

  /**
   * Ordena las ventas según el campo y dirección seleccionados
   */
  ordenarVentas() {
    this.ventasFiltradas.sort((a, b) => {
      let valorA: any, valorB: any;
      
      switch (this.ordenCampo) {
        case 'fecha_venta':
          valorA = new Date(a.fecha_venta).getTime();
          valorB = new Date(b.fecha_venta).getTime();
          break;
        case 'cliente_nombre':
          valorA = a.cliente_nombre.toLowerCase();
          valorB = b.cliente_nombre.toLowerCase();
          break;
        case 'trabajador_nombre':
          valorA = a.trabajador_nombre.toLowerCase();
          valorB = b.trabajador_nombre.toLowerCase();
          break;
        case 'total':
          valorA = a.total;
          valorB = b.total;
          break;
        case 'pedido_id':
          valorA = a.pedido_id;
          valorB = b.pedido_id;
          break;
        default:
          return 0;
      }
      
      if (valorA < valorB) {
        return this.ordenDireccion === 'asc' ? -1 : 1;
      }
      if (valorA > valorB) {
        return this.ordenDireccion === 'asc' ? 1 : -1;
      }
      return 0;
    });
  }

  /**
   * Obtiene el icono de ordenamiento para una columna
   */
  getIconoOrden(campo: string): string {
    if (this.ordenCampo !== campo) return '↕️';
    return this.ordenDireccion === 'asc' ? '↑' : '↓';
  }

  /**
   * Formatea una fecha a YYYY-MM-DD
   */
  formatearFecha(date: Date): string {
    return this.ventasService.formatearFecha(date);
  }

  /**
   * Formatea una fecha para mostrar de manera amigable
   */
  formatearFechaMostrar(fechaISO: string): string {
    const fecha = new Date(fechaISO);
    return fecha.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Formatea un monto como moneda
   */
formatearMoneda(monto: number): string {
  const formatter = new Intl.NumberFormat('es-GT', {
    style: 'currency',
    currency: 'GTQ',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
  
  return formatter.format(monto)
    .replace('GTQ', 'Q')
    .replace('$', 'Q');
}

  /**
   * Obtiene la clase CSS para el método de pago
   */
  getClaseMetodoPago(metodo: string): string {
    switch (metodo) {
      case 'efectivo': return 'metodo-efectivo';
      case 'tarjeta': return 'metodo-tarjeta';
      default: return 'metodo-default';
    }
  }

  /**
   * Obtiene el ícono para el método de pago
   */
  getIconoMetodoPago(metodo: string): string {
    switch (metodo) {
      case 'efectivo': return '💰';
      case 'tarjeta': return '💳';
      default: return '❓';
    }
  }

  /**
   * Navega a la página anterior
   */
  paginaAnterior() {
    if (this.paginaActual > 1) {
      this.cargarVentas(this.paginaActual - 1);
    }
  }

  /**
   * Navega a la página siguiente
   */
  paginaSiguiente() {
    if (this.hayMasPaginas) {
      this.cargarVentas(this.paginaActual + 1);
    }
  }

  /**
   * Recarga las ventas
   */
  recargar() {
    this.cargarVentas(this.paginaActual);
  }

  /**
   * Obtiene el texto de información de paginación
   */
  getTextoPaginacion(): string {
    const inicio = ((this.paginaActual - 1) * this.limitePorPagina) + 1;
    const fin = Math.min(this.paginaActual * this.limitePorPagina, this.totalRegistros);
    return `Mostrando ${inicio} - ${fin} de ${this.totalRegistros} ventas`;
  }

  /**
   * Obtiene el texto del modo de vista actual
   */
  getTextoModoVista(): string {
    switch (this.modoVista) {
      case 'hoy': return 'Ventas de hoy';
      case 'mes': return 'Ventas del mes';
      case 'rango': return `Ventas del ${this.fechaInicio} al ${this.fechaFin}`;
      default: return 'Ventas';
    }
  }

  /**
   * Obtiene el total de ventas del día actual (para estadísticas rápidas)
   */
  getTotalHoy(): number {
    if (this.ventasPorDia.length > 0) {
      const hoy = this.formatearFecha(new Date());
      const ventaHoy = this.ventasPorDia.find(v => v.fecha === hoy);
      return ventaHoy ? ventaHoy.total_dia : 0;
    }
    return 0;
  }

  /**
   * Obtiene la cantidad de ventas del día actual
   */
  getCantidadVentasHoy(): number {
    if (this.ventasPorDia.length > 0) {
      const hoy = this.formatearFecha(new Date());
      const ventaHoy = this.ventasPorDia.find(v => v.fecha === hoy);
      return ventaHoy ? ventaHoy.cantidad_ventas : 0;
    }
    return 0;
  }
}