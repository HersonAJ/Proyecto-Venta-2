import { Injectable } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { VentasService } from './ventas-service';
import { UsuariosService } from './usuarios-service';
import { AuthService } from './auth-service'; 

// Interfaces dentro del mismo archivo
export interface DashboardStats {
  ventasHoy: number;
  totalHoy: number;
  ventasMes: number;
  totalMes: number;
  totalUsuarios?: number;
  usuariosClientes?: number;
  usuariosTrabajadores?: number;
  promedioDiario: number;
  success: boolean;
  esAdmin?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  constructor(
    private ventasService: VentasService,
    private usuariosService: UsuariosService,
    private authService: AuthService 
  ) {}

  obtenerEstadisticasDashboard(): Observable<DashboardStats> {
    const esAdmin = this.authService.isAdmin();
    
    if (esAdmin) {
      // ADMIN: Obtiene todas las estadísticas
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
            success: ventasStats.success && usuariosStats.success,
            esAdmin: true
          };
        }),
        catchError(error => {
          console.error('Error obteniendo estadísticas de admin:', error);
          return this.getEstadisticasBasicas();
        })
      );
    } else {
      // TRABAJADOR: Solo estadísticas básicas de ventas
      return this.ventasService.obtenerEstadisticasVentas().pipe(
        map(ventasStats => {
          return {
            ventasHoy: ventasStats.ventasHoy || 0,
            totalHoy: ventasStats.totalHoy || 0,
            ventasMes: ventasStats.ventasMes || 0,
            totalMes: ventasStats.totalMes || 0,
            promedioDiario: ventasStats.promedioDiario || 0,
            success: ventasStats.success || false,
            esAdmin: false
          };
        }),
        catchError(error => {
          console.error('Error obteniendo estadísticas de ventas:', error);
          return this.getEstadisticasBasicas();
        })
      );
    }
  }

  // Método para estadísticas básicas cuando hay errores
  private getEstadisticasBasicas(): Observable<DashboardStats> {
    return of({
      ventasHoy: 0,
      totalHoy: 0,
      ventasMes: 0,
      totalMes: 0,
      promedioDiario: 0,
      success: false,
      esAdmin: this.authService.isAdmin()
    });
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