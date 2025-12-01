import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CanjearPromoService, BuscarUsuariosResponse, UsuarioFidelidad } from '../servicios/canjear-promo-service';
import { AuthService } from '../servicios/auth-service';

@Component({
  selector: 'app-canjear-promo-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './canjear-promo-component.html',
  styleUrl: './canjear-promo-component.scss'
})
export class CanjearPromoComponent implements OnInit {

  criterioBusqueda: string = '';
  buscando: boolean = false;
  
  // 🟡 VOLVER A usuarioEncontrado para mantener compatibilidad con HTML
  usuariosEncontrados: UsuarioFidelidad[] = [];
  usuarioEncontrado: UsuarioFidelidad | null = null;
  errorBusqueda: string = '';
  
  cantidadCanjear: number = 1;
  canjeando: boolean = false;
  mensajeExito: string = '';
  errorCanje: string = '';

  constructor(
    private canjearPromoService: CanjearPromoService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    if (!this.authService.isTrabajador()) {
      this.router.navigate(['/inicio']);
      return;
    }
  }

  buscarUsuarios() {
    if (!this.criterioBusqueda.trim()) {
      this.errorBusqueda = 'Ingresa un nombre para buscar';
      return;
    }

    this.buscando = true;
    this.errorBusqueda = '';
    this.usuariosEncontrados = [];
    this.usuarioEncontrado = null;

    this.canjearPromoService.buscarUsuariosPorNombre(this.criterioBusqueda).subscribe({
      next: (response: BuscarUsuariosResponse) => {
        this.buscando = false;
        
        if (response.success && response.usuarios) {
          this.usuariosEncontrados = response.usuarios;
          
          if (response.usuarios.length === 1) {
            this.usuarioEncontrado = response.usuarios[0];
          }
        } else {
          this.errorBusqueda = response.message || 'No se encontraron usuarios';
        }
      },
      error: (error) => {
        this.buscando = false;
        this.errorBusqueda = 'Error de conexión al buscar usuarios';
        console.error('Error buscando usuarios:', error);
      }
    });
  }

  seleccionarUsuario(usuario: UsuarioFidelidad) {
    this.usuarioEncontrado = usuario;
    this.cantidadCanjear = 1;
    this.mensajeExito = '';
    this.errorCanje = '';
  }

  canjearPromocion() {
    if (!this.usuarioEncontrado) return;

    this.canjeando = true;
    this.mensajeExito = '';
    this.errorCanje = '';

    this.canjearPromoService.canjearPromocion(
      this.usuarioEncontrado.id, 
      this.cantidadCanjear
    ).subscribe({
      next: (response) => {
        this.canjeando = false;
        
        if (response.success) {
          this.mensajeExito = response.message;
          
          if (this.usuarioEncontrado) {
            this.usuarioEncontrado.promocionesPendientes = response.promocionesRestantes;
            this.usuarioEncontrado.promocionesCanjeadas = 
              (this.usuarioEncontrado.promocionesCanjeadas || 0) + this.cantidadCanjear;
          }
          
          this.cantidadCanjear = 1;
          
          setTimeout(() => {
            this.mensajeExito = '';
          }, 5000);
        } else {
          this.errorCanje = response.message;
        }
      },
      error: (error) => {
        this.canjeando = false;
        this.errorCanje = 'Error de conexión al canjear promoción';
        console.error('Error canjeando promoción:', error);
      }
    });
  }

  limpiarBusqueda() {
    this.criterioBusqueda = '';
    this.usuariosEncontrados = [];
    this.usuarioEncontrado = null;
    this.errorBusqueda = '';
    this.mensajeExito = '';
    this.errorCanje = '';
  }

  get puedeCanjear(): boolean {
    return this.usuarioEncontrado !== null && 
           this.usuarioEncontrado.promocionesPendientes >= this.cantidadCanjear &&
           this.cantidadCanjear > 0;
  }

  get opcionesCantidad(): number[] {
    if (!this.usuarioEncontrado) return [1];
    
    const max = this.usuarioEncontrado.promocionesPendientes;
    return Array.from({ length: max }, (_, i) => i + 1);
  }
}