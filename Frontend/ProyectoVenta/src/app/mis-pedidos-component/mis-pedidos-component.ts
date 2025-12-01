import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { PedidosService, PedidoActivo, MisPedidosResponse } from '../servicios/pedidos-service';
import { AuthService } from '../servicios/auth-service';

@Component({
  selector: 'app-mis-pedidos-component',
  imports: [CommonModule, RouterLink],
  templateUrl: './mis-pedidos-component.html',
  styleUrl: './mis-pedidos-component.scss',
})
export class MisPedidosComponent implements OnInit, OnDestroy {

  pedidos: PedidoActivo[] = [];
  loading: boolean = true;
  errorMessage: string = '';
  private pedidosSubscription!: Subscription;

  constructor(
    private pedidosService: PedidosService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.cargarMisPedidos();
  }

  cargarMisPedidos() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], { 
        queryParams: { returnUrl: '/mis-pedidos' } 
      });
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.pedidosService.obtenerMisPedidos().subscribe({
      next: (response: MisPedidosResponse) => {
        this.loading = false;
        if (response.success) {
          this.pedidos = response.pedidos;
        } else {
          this.errorMessage = response.message || 'Error al cargar los pedidos';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error de conexión al cargar los pedidos';
        console.error('Error cargando pedidos:', error);
      }
    });
  }

  getNumeroPedido(index: number): number {
    return index + 1;
  }

  getEstadoTexto(estado: string): string {
    const estados: { [key: string]: string } = {
      'pendiente': '🕒 Pendiente',
      'en_preparacion': '👨‍🍳 En Preparación', 
      'listo': '✅ Listo para Recoger',
      'entregado': '📦 Entregado',
      'cancelado': '❌ Cancelado'
    };
    return estados[estado] || estado;
  }

  getEstadoClase(estado: string): string {
    const clases: { [key: string]: string } = {
      'pendiente': 'bg-warning text-dark',
      'en_preparacion': 'bg-info text-white',
      'listo': 'bg-success text-white',
      'entregado': 'bg-secondary text-white',
      'cancelado': 'bg-danger text-white'
    };
    return clases[estado] || 'bg-secondary text-white';
  }

  getMetodoPagoTexto(metodo: string): string {
    const metodos: { [key: string]: string } = {
      'efectivo': '💰 Efectivo',
      'tarjeta': '💳 Tarjeta'
    };
    return metodos[metodo] || metodo;
  }

  formatearFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  recargarPedidos() {
    this.cargarMisPedidos();
  }

  ngOnDestroy() {
    if (this.pedidosSubscription) {
      this.pedidosSubscription.unsubscribe();
    }
  }
}