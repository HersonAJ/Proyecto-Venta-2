import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuariosService, Usuario, ReporteUsuariosResponse } from '../servicios/usuarios-service';
import { AuthService } from '../servicios/auth-service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-usuarios-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios-component.html',
  styleUrl: './usuarios-component.scss',
})
export class UsuariosComponent implements OnInit {
  // Lista de usuarios
  usuarios: Usuario[] = [];
  usuariosFiltrados: Usuario[] = [];
  
  // Estados y flags
  isLoading: boolean = true;
  isError: boolean = false;
  errorMessage: string = '';
  
  // Filtros y búsqueda
  filtroRol: string = 'cliente'; // Cambiamos valor por defecto a 'cliente'
  terminoBusqueda: string = '';
  
  // Paginación
  paginaActual: number = 1;
  limitePorPagina: number = 10;
  totalRegistros: number = 0;
  totalPaginas: number = 0;
  hayMasPaginas: boolean = false;
  
  // Estadísticas
  estadisticasRoles: any = {};
  
  // Roles disponibles para filtro (SOLO cliente y trabajador)
  roles = [
    { valor: 'cliente', nombre: '👨‍🍳 Clientes' },
    { valor: 'trabajador', nombre: '👷 Trabajadores' }
  ];
  
  // Ordenamiento
  ordenCampo: string = 'fecha_registro';
  ordenDireccion: 'asc' | 'desc' = 'desc';

  constructor(
    private usuariosService: UsuariosService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.cargarUsuarios();
  }

  /**
   * Carga la lista de usuarios desde el servicio
   */
  cargarUsuarios(pagina: number = 1) {
    this.isLoading = true;
    this.isError = false;
    this.paginaActual = pagina;

    // Siempre cargar por rol específico (eliminada la opción 'todos')
    this.usuariosService.obtenerUsuariosPorRol(this.filtroRol, pagina, this.limitePorPagina)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response: ReporteUsuariosResponse) => this.procesarRespuestaUsuarios(response),
        error: (error) => this.manejarError('Error al cargar usuarios', error)
      });
  }

  /**
   * Procesa la respuesta del servicio de usuarios
   */
  private procesarRespuestaUsuarios(response: ReporteUsuariosResponse) {
    if (response.success) {
      this.usuarios = response.usuarios;
      this.usuariosFiltrados = [...this.usuarios];
      this.totalRegistros = response.totalRegistros;
      this.estadisticasRoles = response.estadisticasRoles;
      
      // Información de paginación
      this.paginaActual = response.paginaActual || 1;
      this.totalPaginas = response.totalPaginas || 1;
      this.hayMasPaginas = response.hayMasPaginas || false;
      
      // Aplicar filtro de búsqueda si existe
      if (this.terminoBusqueda) {
        this.aplicarFiltroBusqueda();
      }
      
      // Aplicar ordenamiento
      this.ordenarUsuarios();
    } else {
      this.manejarError('No se pudieron cargar los usuarios: ' + (response.message || 'Error desconocido'));
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
   * Aplica el filtro de búsqueda
   */
  aplicarFiltroBusqueda() {
    if (!this.terminoBusqueda.trim()) {
      this.usuariosFiltrados = [...this.usuarios];
    } else {
      this.usuariosFiltrados = this.usuariosService.buscarUsuarios(this.terminoBusqueda, this.usuarios);
    }
  }

  /**
   * Cambia el filtro de rol y recarga los usuarios
   */
  cambiarFiltroRol() {
    this.paginaActual = 1;
    this.cargarUsuarios(1);
  }

  /**
   * Cambia la cantidad de usuarios por página
   */
  cambiarLimitePorPagina() {
    this.paginaActual = 1;
    this.cargarUsuarios(1);
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
    this.ordenarUsuarios();
  }

  /**
   * Ordena los usuarios según el campo y dirección seleccionados
   */
  ordenarUsuarios() {
    this.usuariosFiltrados.sort((a, b) => {
      let valorA: any, valorB: any;
      
      switch (this.ordenCampo) {
        case 'nombre':
          valorA = a.nombre.toLowerCase();
          valorB = b.nombre.toLowerCase();
          break;
        case 'email':
          valorA = a.email.toLowerCase();
          valorB = b.email.toLowerCase();
          break;
        case 'rol':
          valorA = a.rol;
          valorB = b.rol;
          break;
        case 'fecha_registro':
          valorA = new Date(a.fecha_registro).getTime();
          valorB = new Date(b.fecha_registro).getTime();
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
   * Formatea la fecha de registro para mostrar
   */
  formatearFecha(fechaISO: string): string {
    return this.usuariosService.formatearFechaRegistro(fechaISO);
  }

  /**
   * Obtiene el nombre traducido del rol
   */
  getNombreRol(rol: string): string {
    return this.usuariosService.getNombreRol(rol);
  }

  /**
   * Obtiene la clase CSS para el badge del rol
   */
  getClaseRol(rol: string): string {
    switch (rol) {
      case 'cliente': return 'badge-cliente';
      case 'trabajador': return 'badge-trabajador';
      case 'admin': return 'badge-admin';
      default: return 'badge-default';
    }
  }

  /**
   * Obtiene el avatar del usuario
   */
  getAvatarUrl(avatarId: number): string {
    return `https://ui-avatars.com/api/?name=Usuario${avatarId}&background=random&size=40`;
  }

  /**
   * Navega a la página anterior
   */
  paginaAnterior() {
    if (this.paginaActual > 1) {
      this.cargarUsuarios(this.paginaActual - 1);
    }
  }

  /**
   * Navega a la página siguiente
   */
  paginaSiguiente() {
    if (this.hayMasPaginas) {
      this.cargarUsuarios(this.paginaActual + 1);
    }
  }

  /**
   * Recarga los usuarios
   */
  recargar() {
    this.cargarUsuarios(this.paginaActual);
  }

  /**
   * Obtiene el texto de información de paginación
   */
  getTextoPaginacion(): string {
    const inicio = ((this.paginaActual - 1) * this.limitePorPagina) + 1;
    const fin = Math.min(this.paginaActual * this.limitePorPagina, this.totalRegistros);
    return `Mostrando ${inicio} - ${fin} de ${this.totalRegistros} usuarios`;
  }

  /**
   * Verifica si el usuario actual tiene permisos de admin
   */
  esAdmin(): boolean {
    return this.authService.isAdmin();
  }

  /**
   * Verifica si el usuario actual es staff (admin o trabajador)
   */
  esStaff(): boolean {
    return this.authService.isStaff();
  }
}