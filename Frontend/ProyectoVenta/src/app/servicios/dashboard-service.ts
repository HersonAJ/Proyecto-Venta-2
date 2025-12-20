import { Injectable } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { VentasService } from './ventas-service';
import { UsuariosService } from './usuarios-service';

// Interfaces dentro del mismo archivo
export interface DashboardStats {
  ventasHoy: number;
  totalHoy: number;
  ventasMes: number;
  totalMes: number;
  totalUsuarios: number;
  usuariosClientes: number;
  usuariosTrabajadores: number;
  promedioDiario: number;
  success: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  constructor(
    private ventasService: VentasService,
    private usuariosService: UsuariosService
  ) {}

  obtenerEstadisticasDashboard(): Observable<DashboardStats> {
    return forkJoin({
      ventasStats: this.ventasService.obtenerEstadisticasVentas(),
      usuariosStats: this.usuariosService.obtenerEstadisticasUsuarios()
    }).pipe(
      map(({ ventasStats, usuariosStats }) => {
        return {
          ventasHoy: ventasStats.ventasHoy || 0,
          totalHoy: ventasStats.totalHoy || 0,
          ventasMes: ventasStats.ventasMes || 0,
          totalMes: ventasStats.totalMes || 0,
          totalUsuarios: usuariosStats.totalUsuarios || 0,
          usuariosClientes: usuariosStats.estadisticasRoles.cliente || 0,
          usuariosTrabajadores: usuariosStats.estadisticasRoles.trabajador || 0,
          promedioDiario: ventasStats.promedioDiario || 0,
          success: ventasStats.success && usuariosStats.success
        };
      })
    );
  }

  formatearMoneda(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0
    }).format(monto);
  }

  formatearNumero(numero: number): string {
    return new Intl.NumberFormat('es-CO').format(numero);
  }
}