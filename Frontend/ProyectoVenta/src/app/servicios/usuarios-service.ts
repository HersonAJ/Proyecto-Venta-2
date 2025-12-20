import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';

// Interfaces dentro del mismo archivo
export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  telefono: string;
  avatar_id: number;
  rol: string;
  fecha_registro: string;
  activo: boolean;
}

export interface EstadisticasRoles {
  cliente?: number;
  trabajador?: number;
  admin?: number;
}

export interface ReporteUsuariosResponse {
  success: boolean;
  usuarios: Usuario[];
  totalRegistros: number;
  estadisticasRoles: EstadisticasRoles;
  paginaActual?: number;
  limite?: number;
  totalPaginas?: number;
  hayMasPaginas?: boolean;
  rolFiltro?: string;
  message?: string;
}

export interface UsuarioPorIdResponse {
  success: boolean;
  usuario: Usuario;
  message?: string;
}

export interface EstadisticasUsuariosResponse {
  success: boolean;
  totalUsuarios: number;
  estadisticasRoles: EstadisticasRoles;
  message?: string;
}

@Injectable({
  providedIn: 'root',
})
export class UsuariosService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'usuarios/';
  }

  obtenerTodosUsuarios(
    pagina?: number,
    limite?: number
  ): Observable<ReporteUsuariosResponse> {
    let params = new HttpParams();
    
    if (pagina) {
      params = params.set('pagina', pagina.toString());
    }
    
    if (limite) {
      params = params.set('limite', limite.toString());
    }
    
    return this.http.get<ReporteUsuariosResponse>(this.apiUrl, { params });
  }

  obtenerUsuariosPorRol(
    rol: string,
    pagina?: number,
    limite?: number
  ): Observable<ReporteUsuariosResponse> {
    let params = new HttpParams();
    
    if (pagina) {
      params = params.set('pagina', pagina.toString());
    }
    
    if (limite) {
      params = params.set('limite', limite.toString());
    }
    
    return this.http.get<ReporteUsuariosResponse>(
      `${this.apiUrl}rol/${rol}`,
      { params }
    );
  }

  obtenerEstadisticasUsuarios(): Observable<EstadisticasUsuariosResponse> {
    return this.http.get<EstadisticasUsuariosResponse>(`${this.apiUrl}estadisticas`);
  }

  obtenerUsuarioPorId(id: number): Observable<UsuarioPorIdResponse> {
    return this.http.get<UsuarioPorIdResponse>(`${this.apiUrl}${id}`);
  }

  obtenerClientes(
    pagina?: number,
    limite?: number
  ): Observable<ReporteUsuariosResponse> {
    return this.obtenerUsuariosPorRol('cliente', pagina, limite);
  }

  obtenerTrabajadores(
    pagina?: number,
    limite?: number
  ): Observable<ReporteUsuariosResponse> {
    return this.obtenerUsuariosPorRol('trabajador', pagina, limite);
  }

  obtenerAdministradores(
    pagina?: number,
    limite?: number
  ): Observable<ReporteUsuariosResponse> {
    return this.obtenerUsuariosPorRol('admin', pagina, limite);
  }

  buscarUsuarios(termino: string, usuarios: Usuario[]): Usuario[] {
    if (!termino.trim()) {
      return usuarios;
    }
    
    const terminoLower = termino.toLowerCase();
    return usuarios.filter(usuario => 
      usuario.nombre.toLowerCase().includes(terminoLower) ||
      usuario.email.toLowerCase().includes(terminoLower)
    );
  }

  formatearFechaRegistro(fechaISO: string): string {
    const fecha = new Date(fechaISO);
    return fecha.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getNombreRol(rol: string): string {
    switch (rol) {
      case 'cliente': return 'Cliente';
      case 'trabajador': return 'Trabajador';
      case 'admin': return 'Administrador';
      default: return rol;
    }
  }

  getColorRol(rol: string): string {
    switch (rol) {
      case 'cliente': return 'primary';
      case 'trabajador': return 'accent';
      case 'admin': return 'warn';
      default: return '';
    }
  }

  getAvatarUrl(avatarId: number): string {
    // Asumiendo que tienes avatars en una carpeta de assets
    return `/assets/avatars/avatar-${avatarId}.png`;
  }
}