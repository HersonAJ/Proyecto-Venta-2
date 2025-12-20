import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';

// Interfaces dentro del mismo archivo
export interface Venta {
  id: number;
  pedido_id: number;
  usuario_id: number;
  cliente_nombre: string;
  trabajador_nombre: string;
  fecha_venta: string;
  total: number;
  metodo_pago: string;
  tipo_venta: string;
  descripcion: string;
}

export interface VentaPorDia {
  fecha: string;
  cantidad_ventas: number;
  total_dia: number;
}

export interface VentaPorMes {
  año: number;
  mes: number;
  cantidad_ventas: number;
  total_mes: number;
}

export interface ReporteVentasResponse {
  success: boolean;
  ventas: Venta[];
  totalRegistros: number;
  totalGeneral: number;
  ventasPorDia: VentaPorDia[];
  ventasPorMes?: VentaPorMes[];
  paginaActual?: number;
  limite?: number;
  totalPaginas?: number;
  hayMasPaginas?: boolean;
  mensaje?: string;  
  message?: string; 
}

export interface EstadisticasVentasResponse {
  success: boolean;
  totalHoy?: number;
  ventasHoy?: number;
  totalMes?: number;
  ventasMes?: number;
  promedioDiario?: number;
  message?: string;
}

@Injectable({
  providedIn: 'root',
})
export class VentasService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'ventas/';
  }

  obtenerReporteVentas(
    fechaInicio?: string,
    fechaFin?: string,
    pagina?: number,
    limite?: number
  ): Observable<ReporteVentasResponse> {
    let params = new HttpParams();
    
    if (fechaInicio) {
      params = params.set('fechaInicio', fechaInicio);
    }
    
    if (fechaFin) {
      params = params.set('fechaFin', fechaFin);
    }
    
    if (pagina) {
      params = params.set('pagina', pagina.toString());
    }
    
    if (limite) {
      params = params.set('limite', limite.toString());
    }
    
    return this.http.get<ReporteVentasResponse>(`${this.apiUrl}reporte`, { params });
  }

  obtenerEstadisticasVentas(): Observable<EstadisticasVentasResponse> {
    return this.http.get<EstadisticasVentasResponse>(`${this.apiUrl}estadisticas`);
  }

  obtenerVentasDelDia(
    pagina?: number,
    limite?: number
  ): Observable<ReporteVentasResponse> {
    let params = new HttpParams();
    
    if (pagina) {
      params = params.set('pagina', pagina.toString());
    }
    
    if (limite) {
      params = params.set('limite', limite.toString());
    }
    
    return this.http.get<ReporteVentasResponse>(`${this.apiUrl}hoy`, { params });
  }

  formatearFecha(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  obtenerVentasDelMesActual(
    pagina?: number,
    limite?: number
  ): Observable<ReporteVentasResponse> {
    const hoy = new Date();
    const primerDiaMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const ultimoDiaMes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    
    const fechaInicio = this.formatearFecha(primerDiaMes);
    const fechaFin = this.formatearFecha(ultimoDiaMes);
    
    return this.obtenerReporteVentas(fechaInicio, fechaFin, pagina, limite);
  }

  obtenerVentasDeLaSemanaActual(
    pagina?: number,
    limite?: number
  ): Observable<ReporteVentasResponse> {
    const hoy = new Date();
    const primerDiaSemana = new Date(hoy);
    primerDiaSemana.setDate(hoy.getDate() - hoy.getDay());
    
    const fechaInicio = this.formatearFecha(primerDiaSemana);
    const fechaFin = this.formatearFecha(hoy);
    
    return this.obtenerReporteVentas(fechaInicio, fechaFin, pagina, limite);
  }
}