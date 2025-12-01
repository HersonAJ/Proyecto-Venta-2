import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { CarritoService, ItemCarrito, CarritoState } from '../servicios/carrito-service';
import { AuthService } from '../servicios/auth-service';
import { PedidosService } from '../servicios/pedidos-service';

@Component({
  selector: 'app-carrito-component',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './carrito-component.html',
  styleUrl: './carrito-component.scss',
})
export class CarritoComponent implements OnInit, OnDestroy {

  carrito: CarritoState = {
    items: [],
    total: 0,
    cantidadTotal: 0
  };

  loading: boolean = false;
  private carritoSubscription!: Subscription;

  constructor(
    private carritoService: CarritoService,
    private authService: AuthService,
    private router: Router,
    private pedidosService: PedidosService
  ) { }

  ngOnInit() {
    // Suscribirse a cambios del carrito
    this.carritoSubscription = this.carritoService.carrito$.subscribe(
      carrito => this.carrito = carrito
    );
  }

  incrementarCantidad(index: number) {
    const nuevaCantidad = this.carrito.items[index].cantidad + 1;
    this.carritoService.actualizarCantidad(index, nuevaCantidad);
  }

  decrementarCantidad(index: number) {
    const nuevaCantidad = this.carrito.items[index].cantidad - 1;
    this.carritoService.actualizarCantidad(index, nuevaCantidad);
  }

  eliminarItem(index: number) {
    this.carritoService.eliminarItem(index);
  }

  vaciarCarritoConfirmado() {
    this.carritoService.limpiarCarrito();
  }

  confirmarPedido() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: '/carrito' }
      });
      return;
    }

    const usuarioId = this.authService.getUserId();
    if (usuarioId === null) return;

    this.loading = true;

    const pedidoData = this.pedidosService.convertirCarritoAPedido(this.carrito, usuarioId);

    this.pedidosService.crearPedido(pedidoData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.carritoService.limpiarCarrito();
          this.mostrarModalExito();
        } else {
          alert('Error: ' + response.message);
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error creando pedido:', error);
        alert('Error al crear el pedido');
      }
    });
  }

  mostrarModalExito() {
    const modalElement = document.getElementById('pedidoExitoModal');
    if (modalElement) {
      const modal = new (window as any).bootstrap.Modal(modalElement);
      modal.show();
    }
  }
  getIngredientesQuitados(item: ItemCarrito): string {
    if (!item.personalizacion.ingredientesAQuitar.length) {
      return 'Sin personalizaciones';
    }

    const ingredientesNombres = item.personalizacion.ingredientesAQuitar.map(ingId => {
      const ingrediente = item.producto.ingredientes.find(i => i.id === ingId);
      return ingrediente?.nombre || '';
    }).filter(nombre => nombre !== '');

    return ingredientesNombres.join(', ');
  }

  ngOnDestroy() {
    if (this.carritoSubscription) {
      this.carritoSubscription.unsubscribe();
    }
  }
}